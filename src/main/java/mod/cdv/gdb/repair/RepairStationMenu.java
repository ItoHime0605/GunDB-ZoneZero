package mod.cdv.gdb.repair;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.resource.index.CommonGunIndex;
import mod.cdv.gdb.DataLookup;
import mod.cdv.gdb.resource.GunModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Three-slot GunDB repair station; independent of AnvilMenu. Server owns all costs and inventory. */
public final class RepairStationMenu extends AbstractContainerMenu {
    public static final int GUN = 0, MATERIAL = 1, OUTPUT = 2;
    private static final int PLAYER_START = 3, PLAYER_END = 39;
    private final ContainerLevelAccess access;
    private final Player owner;
    private final SimpleContainer inputs = new SimpleContainer(2) {
        @Override public void setChanged() { super.setChanged(); RepairStationMenu.this.slotsChanged(this); }
    };
    private final SimpleContainer result = new SimpleContainer(1);
    // 0 required screwdrivers; 1 preview consumption; 2 level cost;
    // 3 gunsmith level; 4 cumulative gunsmith experience.
    private final ContainerData info;

    public RepairStationMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL);
    }
    public RepairStationMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(RepairStationRegistry.REPAIR_MENU.get(), id);
        this.access = access;
        this.owner = inventory.player;
        this.info = new SimpleContainerData(5);
        this.addDataSlots(info);

        addSlot(new Slot(inputs, GUN, 70, 64) {
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ModernKineticGunItem && stack.getCount() == 1;
            }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(inputs, MATERIAL, 160, 64) {
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.is(RepairStationRegistry.SCREWDRIVER.get());
            }
        });
        addSlot(new Slot(result, 0, 260, 64) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) { return canTake(player); }
            @Override public void onTake(Player player, ItemStack taken) {
                if (!player.level().isClientSide) {
                    Preview p = preview();
                    if (p != null && (player.getAbilities().instabuild || player.experienceLevel >= p.levelCost)) {
                        ItemStack gun = inputs.getItem(GUN);
                        int beforeDamage = gun.getDamageValue();
                        int max = gun.getMaxDamage();
                        if (!player.getAbilities().instabuild) player.giveExperienceLevels(-p.levelCost);
                        inputs.removeItem(GUN, 1);
                        inputs.removeItem(MATERIAL, p.toolsUsed);
                        if (player instanceof ServerPlayer server) {
                            RepairStationSkill.awardCompletedRepair(server, max, beforeDamage);
                        }
                        updatePreview();
                    } else {
                        result.setItem(0, ItemStack.EMPTY);
                    }
                    broadcastChanges();
                }
                super.onTake(player, taken);
            }
        });
        for (int row=0; row<3; row++) for (int col=0; col<9; col++)
            addSlot(new Slot(inventory, col + row * 9 + 9, 80 + col * 18, 151 + row * 18));
        for (int col=0; col<9; col++) addSlot(new Slot(inventory, col, 80 + col * 18, 209));
        if (!owner.level().isClientSide) updatePreview();
    }

    public ItemStack displayedGun() { return inputs.getItem(GUN); }
    public ItemStack displayedMaterial() { return inputs.getItem(MATERIAL); }
    public int fullRequiredMaterials() { return info.get(0); }
    public int materialsUsedInPreview() { return info.get(1); }
    public int previewLevelCost() { return info.get(2); }
    public int gunsmithLevel() { return info.get(3); }
    public int gunsmithExperience() { return info.get(4); }

    private static GunModifier modifier(ItemStack gun) {
        if (gun.isEmpty() || !(gun.getItem() instanceof ModernKineticGunItem)) return null;
        return DataLookup.getModifiers(gun);
    }
    private static int requiredTools(ItemStack gun, int gunsmithLevel) {
        IGun item = IGun.getIGunOrNull(gun);
        if (item == null) return 0;
        ResourceLocation id = item.getGunId(gun);
        if (id == null) return 0;
        String category = TimelessAPI.getCommonGunIndex(id)
            .map(CommonGunIndex::getType).orElse("");
        return RepairStationRules.screwdrivers(id.toString(), category, gunsmithLevel);
    }
    private Preview preview() {
        ItemStack gun=inputs.getItem(GUN), tools=inputs.getItem(MATERIAL);
        GunModifier mod=modifier(gun);
        if (mod == null || !mod.enabled() || !gun.isDamageableItem() || !gun.isDamaged()) return null;
        int required = requiredTools(gun, RepairStationSkill.level(owner));
        if (required <= 0 || !tools.is(RepairStationRegistry.SCREWDRIVER.get()) || tools.getCount() < required) return null;
        ItemStack repaired = gun.copyWithCount(1);
        repaired.setDamageValue(0);
        int originalItems = RepairStationMath.requiredForFullRepair(
            gun.getMaxDamage(), gun.getDamageValue(), mod.repairCost());
        int levelCost = RepairStationRules.levelCost(mod.xpCost(), originalItems);
        return new Preview(repaired, required, levelCost);
    }
    private boolean canTake(Player player) {
        if (result.getItem(0).isEmpty()) return false;
        if (player.level().isClientSide)
            return player.getAbilities().instabuild || player.experienceLevel >= info.get(2);
        Preview p=preview();
        return p!=null && (player.getAbilities().instabuild || player.experienceLevel >= p.levelCost);
    }
    private void updatePreview() {
        if (owner.level().isClientSide) return;
        info.set(3,RepairStationSkill.level(owner));
        info.set(4,RepairStationSkill.experience(owner));
        ItemStack gun=inputs.getItem(GUN);
        GunModifier mod=modifier(gun);
        int cost = mod!=null && mod.enabled() && gun.isDamaged() ? requiredTools(gun, info.get(3)) : 0;
        info.set(0,cost);
        Preview p=preview();
        result.setItem(0,p==null?ItemStack.EMPTY:p.result.copy());
        info.set(1,p==null?0:p.toolsUsed);
        info.set(2,p==null?0:p.levelCost);
        broadcastChanges();
    }
    @Override public void slotsChanged(Container changed) { updatePreview(); }
    @Override public boolean stillValid(Player player) {
        return stillValid(access,player,RepairStationRegistry.REPAIR_STATION.get());
    }
    @Override public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            for (int i=0;i<2;i++) {
                ItemStack remaining=inputs.removeItemNoUpdate(i);
                if (!remaining.isEmpty()) player.getInventory().placeItemBackInInventory(remaining);
            }
            result.setItem(0, ItemStack.EMPTY);
        }
    }
    @Override public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex<0 || slotIndex>=slots.size()) return ItemStack.EMPTY;
        Slot slot=slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(), copy=stack.copy();
        if (slotIndex==OUTPUT) {
            if (!slot.mayPickup(player)) return ItemStack.EMPTY;
            if (!moveItemStackTo(stack,PLAYER_START,PLAYER_END,true)) return ItemStack.EMPTY;
            slot.onTake(player,copy);
            return copy;
        }
        if (slotIndex==GUN || slotIndex==MATERIAL) {
            if (!moveItemStackTo(stack,PLAYER_START,PLAYER_END,false)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof ModernKineticGunItem) {
            if (!moveItemStackTo(stack,GUN,GUN+1,false)) return ItemStack.EMPTY;
        } else if (stack.is(RepairStationRegistry.SCREWDRIVER.get())) {
            if (!moveItemStackTo(stack,MATERIAL,MATERIAL+1,false)) return ItemStack.EMPTY;
        } else if (slotIndex<30) {
            if (!moveItemStackTo(stack,30,39,false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack,3,30,false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
    private record Preview(ItemStack result,int toolsUsed,int levelCost) {}
}

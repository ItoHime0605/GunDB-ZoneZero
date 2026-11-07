package mod.cdv.gdb.repair;

import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.item.AttachmentItem;
import mod.cdv.gdb.DataLookup;
import mod.cdv.gdb.resource.GunModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

/**
 * Ordinary Minecraft anvils remain usable by normal items.
 * GunDB guns and attachments are repaired only at the independent station.
 * This event handler does not subclass AnvilMenu or install an AnvilMenu Mixin.
 */
public final class RepairStationAnvilBlocker {
    private RepairStationAnvilBlocker() {}

    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (left.isEmpty() || right.isEmpty()) return;
        boolean gun = left.getItem() instanceof ModernKineticGunItem;
        boolean attachment = left.getItem() instanceof AttachmentItem;
        if (!gun && !attachment) return;

        GunModifier modifier = DataLookup.getModifiers(left);
        if (modifier == null || !modifier.enabled()) return;
        boolean material = modifier.repairItem() != null && modifier.repairItem().test(right);
        boolean combine = gun && right.getItem() instanceof ModernKineticGunItem
            || attachment && right.getItem() instanceof AttachmentItem;
        if (material || combine) event.setCanceled(true);
    }
}

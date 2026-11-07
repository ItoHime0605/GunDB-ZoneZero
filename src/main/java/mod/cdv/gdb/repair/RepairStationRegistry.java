package mod.cdv.gdb.repair;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/** Registers workbench into GunDB's existing mod ID rather than a second mod. */
public final class RepairStationRegistry {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("gundb");
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("gundb");
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "gundb");
    public static final DeferredHolder<MenuType<?>, MenuType<RepairStationMenu>> REPAIR_MENU =
        MENUS.register("gun_repair_station", () -> new MenuType<>(RepairStationMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final DeferredBlock<RepairStationBlock> REPAIR_STATION = BLOCKS.register(
        "gun_repair_station",
        () -> new RepairStationBlock(BlockBehaviour.Properties.of()
            .strength(3.5F, 6.0F)
            .requiresCorrectToolForDrops()
            .sound(SoundType.METAL)
            .noOcclusion()
            .lightLevel(state -> state.getValue(RepairStationBlock.HALF) == DoubleBlockHalf.UPPER ? 6 : 0))
    );
    public static final DeferredItem<BlockItem> REPAIR_STATION_ITEM =
        ITEMS.registerSimpleBlockItem("gun_repair_station", REPAIR_STATION);

    public static final DeferredItem<Item> SCREWDRIVER =
        ITEMS.registerItem("screwdriver", Item::new, new Item.Properties().stacksTo(64));

    private RepairStationRegistry() {}

    /** Called from GunDurability's constructor via the integration Mixin. */
    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        MENUS.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(RepairStationAnvilBlocker::onAnvilUpdate);
        NeoForge.EVENT_BUS.addListener(RepairStationSkill::onClone);
        if (FMLEnvironment.dist.isClient()) RepairStationClient.register(modEventBus);
        modEventBus.addListener(RepairStationRegistry::populateCreativeTab);
    }

    private static void populateCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            ((CreativeModeTab.Output) event).accept(REPAIR_STATION_ITEM);
            ((CreativeModeTab.Output) event).accept(SCREWDRIVER);
        }
    }
}

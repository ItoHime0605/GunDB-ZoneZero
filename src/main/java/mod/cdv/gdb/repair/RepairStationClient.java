package mod.cdv.gdb.repair;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Loaded only on a physical client; never initialized on a dedicated server. */
public final class RepairStationClient {
    private RepairStationClient() {}
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RepairStationClient::screens);
    }
    private static void screens(RegisterMenuScreensEvent event) {
        event.register(RepairStationRegistry.REPAIR_MENU.get(), RepairStationScreen::new);
    }
}

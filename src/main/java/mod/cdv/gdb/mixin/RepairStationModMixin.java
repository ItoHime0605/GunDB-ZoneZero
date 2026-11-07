package mod.cdv.gdb.mixin;

import mod.cdv.gdb.repair.RepairStationRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Enables registrations without replacing the existing compiled GunDB entrypoint. */
@Mixin(targets = "mod.cdv.gdb.GunDurability", remap = false)
public abstract class RepairStationModMixin {
    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void gundb$addRepairStation(IEventBus modBus, ModContainer container, CallbackInfo ci) {
        RepairStationRegistry.register(modBus);
    }
}

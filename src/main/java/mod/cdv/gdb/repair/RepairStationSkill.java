package mod.cdv.gdb.repair;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Player-specific, server-owned cumulative Gunsmith XP in NeoForge persistent player NBT. */
public final class RepairStationSkill {
    private static final String XP_KEY = "gundb_gunsmith_xp_v1";
    private RepairStationSkill() {}

    public static int experience(Player player) {
        return RepairStationRules.capExperience(player.getPersistentData().getInt(XP_KEY));
    }
    public static int level(Player player) {
        return RepairStationRules.level(experience(player));
    }
    public static void awardCompletedRepair(ServerPlayer player, int maxDamage, int damageBefore) {
        if (player.getAbilities().instabuild || !RepairStationRules.grantsExperience(maxDamage, damageBefore)) return;
        int current = experience(player);
        int next = RepairStationRules.capExperience(current + RepairStationRules.XP_PER_QUALIFYING_REPAIR);
        if (next != current) player.getPersistentData().putInt(XP_KEY, next);
    }
    /** Keep Gunsmith XP when a player respawns, including death and End returns. */
    public static void onClone(PlayerEvent.Clone event) {
        int original = experience(event.getOriginal());
        event.getEntity().getPersistentData().putInt(XP_KEY, original);
    }
}

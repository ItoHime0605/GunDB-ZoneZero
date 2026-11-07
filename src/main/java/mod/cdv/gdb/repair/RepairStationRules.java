package mod.cdv.gdb.repair;

import java.util.Locale;

/** Single source of truth for screwdriver costs and skill thresholds. No Minecraft dependency. */
public final class RepairStationRules {
    private RepairStationRules() {}
    public static final int MAX_LEVEL = 3;
    public static final int XP_PER_QUALIFYING_REPAIR = 5;
    // Cumulative XP: 0->1:100; 1->2:+150; 2->3:+200.
    public static final int[] LEVEL_START = {0, 100, 250, 450};

    public static int level(int experience) {
        int xp = Math.max(0, experience);
        if (xp >= LEVEL_START[3]) return 3;
        if (xp >= LEVEL_START[2]) return 2;
        if (xp >= LEVEL_START[1]) return 1;
        return 0;
    }
    public static int capExperience(int xp) {
        return Math.max(0, Math.min(LEVEL_START[3], xp));
    }
    public static int nextLevelExperience(int currentLevel) {
        return currentLevel >= MAX_LEVEL ? LEVEL_START[MAX_LEVEL] : LEVEL_START[Math.max(0,currentLevel)+1];
    }
    public static boolean grantsExperience(int maxDamage, int damageBeforeRepair) {
        // Less than 50% durability REMAINING, strictly below threshold.
        return maxDamage > 0 && damageBeforeRepair > 0 && (long)damageBeforeRepair * 2 > maxDamage;
    }

    public static int baseScrewdrivers(String id, String category) {
        String gun = id == null ? "" : id.toLowerCase(Locale.ROOT);
        int colon = gun.indexOf(':');
        String path = colon >= 0 ? gun.substring(colon+1) : gun;
        // Exact named overrides take priority over the gun's TaCZ tab classification.
        return switch (path) {
            case "rhino357", "rhino_357", "taurus943", "taurus_943", "taurus500", "taurus_500" -> 4;
            case "springfield1873", "springfield_1873" -> 5;
            case "fn_evolys", "fnevolys", "minigun" -> 6;
            case "lonetrail", "lone_trail" -> 7;
            default -> switch (category == null ? "" : category.toLowerCase(Locale.ROOT)) {
                case "mg", "machine_gun", "machinegun", "lmg" -> 6;
                case "rpg", "rocket_launcher", "launcher", "rocket" -> 7;
                case "pistol" -> 4;
                case "smg", "rifle", "shotgun", "sniper", "dmr", "marksman" -> 5;
                default -> 5;
            };
        };
    }
    public static int screwdrivers(String id, String category, int level) {
        return Math.max(1, baseScrewdrivers(id, category) - Math.max(0, Math.min(MAX_LEVEL, level)));
    }
    public static int levelCost(float originalXpCost, int toolsRequired) {
        if (toolsRequired <= 0) return 0;
        if (!Float.isFinite(originalXpCost) || originalXpCost < 0f) return 1;
        return Math.max(1, Math.round(originalXpCost * toolsRequired));
    }
}

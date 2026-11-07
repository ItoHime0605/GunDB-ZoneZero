package mod.cdv.gdb.repair;

/**
 * Pure deterministic repair calculation shared by the server-authoritative menu.
 * The repaired points per material match GunDB 2.2.2's original anvil algorithm:
 * gun.getMaxDamage() / modifier.repairCost().
 *
 * No client/menu dependencies: this class has standalone regression tests.
 */
public final class RepairStationMath {
    private RepairStationMath() {}

    public record Quote(int materialsRequired, int materialsConsumed,
                        int repairedDamage, int experienceLevels) {
        public boolean isPossible() { return materialsConsumed > 0; }
    }

    public static Quote quote(int maxDamage, int currentDamage, int repairCost,
                              float xpPerMaterial, int availableMaterials) {
        if (maxDamage <= 0 || currentDamage <= 0 || repairCost <= 0 || availableMaterials <= 0)
            return new Quote(0, 0, Math.max(0, currentDamage), 0);
        int pointsPerMaterial = maxDamage / repairCost;
        if (pointsPerMaterial <= 0)
            return new Quote(0, 0, currentDamage, 0);
        int damage = Math.min(currentDamage, maxDamage);
        int required = (int) Math.min(Integer.MAX_VALUE,
            ((long) damage + pointsPerMaterial - 1L) / pointsPerMaterial);
        int used = Math.min(required, availableMaterials);
        int repaired = (int) Math.max(0L, (long) damage - (long) used * pointsPerMaterial);
        int levels = Math.max(1, Math.round(xpPerMaterial * used));
        return new Quote(required, used, repaired, levels);
    }

    public static int requiredForFullRepair(int maxDamage, int currentDamage,
                                            int repairCost) {
        return quote(maxDamage, currentDamage, repairCost, 0f, Integer.MAX_VALUE)
                .materialsRequired();
    }
}

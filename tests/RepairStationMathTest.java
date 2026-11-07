import mod.cdv.gdb.repair.RepairStationMath;

public final class RepairStationMathTest {
    private static int cases = 0;
    private static void eq(String description, int expected, int actual) {
        cases++;
        if (expected != actual) throw new AssertionError(description + " expected=" + expected + " actual=" + actual);
    }
    private static void inspect(int max, int damage, int repairCost, float xp, int provided,
                                int required, int consumed, int remaining, int levels) {
        var q = RepairStationMath.quote(max, damage, repairCost, xp, provided);
        eq("required", required, q.materialsRequired());
        eq("used", consumed, q.materialsConsumed());
        eq("remaining", remaining, q.repairedDamage());
        eq("levels", levels, q.experienceLevels());
    }
    public static void main(String[] args) {
        inspect(100, 0, 5, 2f, 5, 0, 0, 0, 0);
        inspect(100, 45, 5, 2f, 0, 0, 0, 45, 0);
        inspect(100, 45, 5, 2f, 1, 3, 1, 25, 2);
        inspect(100, 45, 5, 2f, 9, 3, 3, 0, 6);
        inspect(100, 100, 3, 0.4f, 3, 4, 3, 1, 1);
        inspect(100, 100, 3, 0.4f, 4, 4, 4, 0, 2);
        inspect(100, 100, 0, 1f, 2, 0, 0, 100, 0);
        inspect(100, 100, 101, 1f, 2, 0, 0, 100, 0);
        inspect(100, 500, 4, 1f, 1, 4, 1, 75, 1);
        inspect(Integer.MAX_VALUE, Integer.MAX_VALUE, 1, 1f, 2, 1, 1, 0, 1);
        java.util.Random random = new java.util.Random(20261107);
        for (int i=0; i<2000; i++) {
            int max=1+random.nextInt(5000), damage=random.nextInt(max+1);
            int repairCost=1+random.nextInt(Math.max(1,max));
            int available=random.nextInt(65);
            int per=max/repairCost;
            int oldRemaining=damage, oldUsed=0;
            while (oldRemaining>0 && per>0 && oldUsed<available) {
                oldRemaining-=Math.min(oldRemaining,per);
                oldUsed++;
            }
            var q=RepairStationMath.quote(max,damage,repairCost,0.7f,available);
            eq("old GunDB repair damage equivalence",oldRemaining,q.repairedDamage());
            eq("old GunDB material equivalence",oldUsed,q.materialsConsumed());
        }
        eq("display full repair", 3, RepairStationMath.requiredForFullRepair(100,45,5));
        eq("display independent of materials", 4, RepairStationMath.requiredForFullRepair(100,100,3));
        System.out.println("PASS: " + cases + " repair formula assertions");
    }
}

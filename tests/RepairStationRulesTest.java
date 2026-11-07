import mod.cdv.gdb.repair.RepairStationRules;
import java.util.Random;

/** Offline Java 21 regression test: no game libraries necessary. */
public final class RepairStationRulesTest {
    private static long asserts = 0;
    private static void eq(String label, int expected, int actual) {
        asserts++;
        if (actual != expected) throw new AssertionError(label + ": expected "+expected+", got "+actual);
    }
    private static void yes(String label, boolean actual) {
        asserts++;
        if (!actual) throw new AssertionError(label);
    }
    public static void main(String[] args) {
        for (int level = 0; level <= 3; level++) {
            eq("pistol "+level, 4-level, RepairStationRules.screwdrivers("tacz:p320", "pistol", level));
            for (String kind: new String[]{"smg","rifle","shotgun","sniper","dmr"})
                eq(kind+" level "+level,5-level,RepairStationRules.screwdrivers("tacz:unknown",kind,level));
            eq("mg",6-level,RepairStationRules.screwdrivers("tacz:pkm","mg",level));
            eq("rpg",7-level,RepairStationRules.screwdrivers("tacz:rpg7","rpg",level));
            for (String gun: new String[]{"rhino357","taurus943","taurus500"})
                eq(gun,4-level,RepairStationRules.screwdrivers("tacz:"+gun,"pistol",level));
            eq("springfield",5-level,RepairStationRules.screwdrivers("tacz:springfield1873","sniper",level));
            eq("fn_evolys",6-level,RepairStationRules.screwdrivers("tacz:fn_evolys","mg",level));
            eq("minigun",6-level,RepairStationRules.screwdrivers("tacz:minigun","mg",level));
            eq("lonetrail",7-level,RepairStationRules.screwdrivers("tacz:lonetrail","pistol",level));
        }
        eq("initial level",0,RepairStationRules.level(0));
        eq("99",0,RepairStationRules.level(99));
        eq("100",1,RepairStationRules.level(100));
        eq("249",1,RepairStationRules.level(249));
        eq("250",2,RepairStationRules.level(250));
        eq("449",2,RepairStationRules.level(449));
        eq("450",3,RepairStationRules.level(450));
        eq("over-cap",3,RepairStationRules.level(5000));
        eq("negative",0,RepairStationRules.level(-5));
        eq("cap-negative",0,RepairStationRules.capExperience(-5));
        eq("cap-overflow",450,RepairStationRules.capExperience(70000));
        for (int i=0;i<20;i++) eq("before lvl1",0,RepairStationRules.level(i*5));
        for (int i=0;i<30;i++) eq("before lvl2",1,RepairStationRules.level(100+i*5));
        for (int i=0;i<40;i++) eq("before lvl3",2,RepairStationRules.level(250+i*5));
        eq("lv1 transition",1,RepairStationRules.level(20*5));
        eq("lv2 transition",2,RepairStationRules.level(50*5));
        eq("lv3 transition",3,RepairStationRules.level(90*5));
        yes("50% exactly NOT eligible",!RepairStationRules.grantsExperience(1000,500));
        yes("50% minus 1 eligible",RepairStationRules.grantsExperience(1000,501));
        yes("51% durability NOT eligible",!RepairStationRules.grantsExperience(1000,490));
        yes("odd 50% boundary eligible",RepairStationRules.grantsExperience(99,50));
        yes("odd 50% boundary ineligible",!RepairStationRules.grantsExperience(99,49));
        yes("broken eligible",RepairStationRules.grantsExperience(500,500));
        yes("zero max NOT eligible",!RepairStationRules.grantsExperience(0,10));
        yes("zero damage NOT eligible",!RepairStationRules.grantsExperience(100,0));
        eq("all repairs full cost",5,RepairStationRules.screwdrivers("tacz:hk416","rifle",0));
        eq("no fewer than 1",1,RepairStationRules.screwdrivers("tacz:taurus943","pistol",99));
        eq("XP floor",1,RepairStationRules.levelCost(0f,3));
        eq("XP quantity",10,RepairStationRules.levelCost(2f,5));
        Random r=new Random(20261107);
        for (int i=0;i<2000;i++) {
            int xp=r.nextInt(5000)-200;
            int level=RepairStationRules.level(xp);
            yes("xp monotonic/level clamp",level>=0&&level<=3);
            int base=RepairStationRules.baseScrewdrivers("tacz:test",new String[]{"pistol","rpg","mg","dmr"}[r.nextInt(4)]);
            int c=RepairStationRules.screwdrivers("tacz:test","rifle",level);
            yes("normal costs bounded",c>=2&&c<=5);
            yes("base category cost bounded",base>=4&&base<=7);
        }
        System.out.println("PASS: "+asserts+" Gunsmith / screwdriver policy assertions");
    }
}

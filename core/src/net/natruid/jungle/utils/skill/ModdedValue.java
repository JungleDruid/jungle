package net.natruid.jungle.utils.skill;

public class ModdedValue {
    public static final ModdedValue ZERO = new ModdedValue();
    public static final ModdedValue ONE = new ModdedValue(null, 0, 1);

    public final String proficiency;
    public final float magnitude;
    public final float base;

    public ModdedValue(String proficiency, float magnitude, float base) {
        this.proficiency = proficiency;
        this.magnitude = magnitude;
        this.base = base;
    }

    public ModdedValue() {
        this(null, 0, 0);
    }
}

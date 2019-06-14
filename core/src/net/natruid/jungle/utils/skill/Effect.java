package net.natruid.jungle.utils.skill;

public class Effect {
    public static final Effect EMPTY = new Effect();
    public static final Effect[] EMPTY_ARRAY = new Effect[0];

    public final String type;
    public final ModdedValue amount;
    public final ModdedValue duration;
    public final ModdedValue chance;

    public Effect(String type, ModdedValue amount, ModdedValue duration, ModdedValue chance) {
        this.type = type;
        this.amount = amount;
        this.duration = duration;
        this.chance = chance;
    }

    public Effect() {
        this("", ModdedValue.ZERO, ModdedValue.ZERO, ModdedValue.ONE);
    }
}

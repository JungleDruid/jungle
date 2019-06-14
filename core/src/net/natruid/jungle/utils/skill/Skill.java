package net.natruid.jungle.utils.skill;

public class Skill {
    public final String name;
    public final Effect[] effects;
    public final ModdedValue range;
    public final ModdedValue area;
    public final ModdedValue cooldown;
    public final ModdedValue accuracy;
    public final int cost;

    public Skill(String name, Effect[] effects, ModdedValue range, ModdedValue area, ModdedValue cooldown, ModdedValue accuracy, int cost) {
        this.name = name;
        this.effects = effects;
        this.range = range;
        this.area = area;
        this.cooldown = cooldown;
        this.accuracy = accuracy;
        this.cost = cost;
    }

    @SuppressWarnings("unused")
    public Skill() {
        this(
            "",
            Effect.EMPTY_ARRAY,
            ModdedValue.ZERO,
            ModdedValue.ZERO,
            ModdedValue.ZERO,
            ModdedValue.ZERO,
            0);
    }
}

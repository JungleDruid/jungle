package net.natruid.jungle.utils.skill

class Skill(
    val name: String = "",
    val effects: Array<Effect> = Effect.EMPTY_ARRAY,
    val range: ModdedValue = ModdedValue.ZERO,
    val area: ModdedValue = ModdedValue.ZERO,
    val cooldown: ModdedValue = ModdedValue.ZERO,
    val accuracy: ModdedValue = ModdedValue.ZERO,
    val cost: Int = 0
)

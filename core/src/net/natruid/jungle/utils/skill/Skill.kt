package net.natruid.jungle.utils.skill

class Skill(
    val name: String = "",
    val effect: Effect = Effect.EMPTY,
    val range: ModdedValue = ModdedValue.ZERO,
    val area: ModdedValue = ModdedValue.ZERO,
    val cooldown: ModdedValue = ModdedValue.ZERO
)


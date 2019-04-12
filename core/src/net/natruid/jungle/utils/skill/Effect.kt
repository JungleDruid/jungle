package net.natruid.jungle.utils.skill

class Effect(
    val type: String = "",
    val amount: ModdedValue = ModdedValue.ZERO,
    val duration: ModdedValue = ModdedValue.ZERO,
    val chance: ModdedValue = ModdedValue.ONE
) {
    companion object {
        val EMPTY = Effect()
        val EMPTY_ARRAY = Array(0) { EMPTY }
    }
}

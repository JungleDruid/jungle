package net.natruid.jungle.utils.skill

class Effect(
    val type: String = "",
    val amount: ModdedValue = ModdedValue.ZERO,
    val duration: ModdedValue = ModdedValue.ZERO,
    val nextEffect: Effect? = null,
    val parallel: Boolean = false
) {
    companion object {
        val EMPTY = Effect()
    }
}

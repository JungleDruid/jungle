package net.natruid.jungle.utils.skill

data class ModdedValue(
    val proficiency: String? = null,
    val magnitude: Float = 0f,
    val base: Float = 0f
) {
    companion object {
        val ZERO = ModdedValue()
    }
}

package net.natruid.jungle.components

import com.artemis.Component
import net.natruid.jungle.utils.StatType

class StatsComponent : Component() {
    inline val hp get() = values[StatType.HP.ordinal]
    inline val speed get() = values[StatType.SPEED.ordinal] / 100f
    inline val damage get() = values[StatType.DAMAGE.ordinal] / 100f + 1f
    inline val heal get() = values[StatType.HEAL.ordinal] / 100f + 1f
    inline val accuracy get() = values[StatType.ACCURACY.ordinal] / 100f + 1f
    inline val dodge get() = values[StatType.DODGE.ordinal] / 100f + 1f
    inline val area get() = values[StatType.AREA.ordinal] / 100f + 1f
    inline val duration get() = values[StatType.DURATION.ordinal] / 100f + 1f
    inline val range get() = values[StatType.RANGE.ordinal]
    inline val ap get() = values[StatType.AP.ordinal]
    inline val stun get() = values[StatType.STUN.ordinal] > 0

    val values = IntArray(StatType.size)

    var dirty = true
}

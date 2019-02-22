package net.natruid.jungle.utils

import com.artemis.ComponentMapper
import net.natruid.jungle.components.UnitComponent

class FactionComparator : Comparator<Int> {
    private lateinit var mUnit: ComponentMapper<UnitComponent>

    override fun compare(p0: Int, p1: Int): Int {
        val f0 = mUnit[p0].faction.ordinal
        val f1 = mUnit[p1].faction.ordinal
        return when {
            f0 > f1 -> 1
            f0 < f1 -> -1
            else -> 0
        }
    }
}

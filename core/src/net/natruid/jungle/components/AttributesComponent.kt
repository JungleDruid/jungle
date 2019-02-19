package net.natruid.jungle.components

import com.artemis.Component
import net.natruid.jungle.utils.AttributeType

class AttributesComponent : Component() {
    inline val intelligence get() = modified[AttributeType.INTELLIGENCE.ordinal]
    inline val determination get() = modified[AttributeType.DETERMINATION.ordinal]
    inline val endurance get() = modified[AttributeType.ENDURANCE.ordinal]
    inline val awareness get() = modified[AttributeType.AWARENESS.ordinal]
    inline val luck get() = modified[AttributeType.LUCK.ordinal]

    val base = Array(AttributeType.size) { 10 }
    val modified = Array(AttributeType.size) { 10 }

    var dirty = true
}

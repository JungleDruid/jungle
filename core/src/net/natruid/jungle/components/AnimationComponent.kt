package net.natruid.jungle.components

import com.artemis.Component
import com.artemis.annotations.EntityId
import net.natruid.jungle.utils.AnimationType

class AnimationComponent : Component() {
    @EntityId
    var target = -1
    var type: AnimationType? = null
    var callback: (() -> Unit)? = null
    var time = 0f
}

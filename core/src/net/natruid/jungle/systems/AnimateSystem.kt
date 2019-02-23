package net.natruid.jungle.systems

import com.artemis.Aspect
import com.artemis.ComponentMapper
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import ktx.math.minusAssign
import ktx.math.plusAssign
import ktx.math.timesAssign
import net.natruid.jungle.components.AnimationComponent
import net.natruid.jungle.components.TransformComponent
import net.natruid.jungle.components.UnitComponent
import net.natruid.jungle.utils.AnimationType

class AnimateSystem : IteratingSystem(Aspect.all(
    UnitComponent::class.java,
    AnimationComponent::class.java
)) {
    private lateinit var mAnimation: ComponentMapper<AnimationComponent>
    private lateinit var mTransform: ComponentMapper<TransformComponent>
    private lateinit var mUnit: ComponentMapper<UnitComponent>

    private fun move(self: Int, target: Int) {
        val pos = Vector2()
        pos.set(mTransform[target].position)
        pos -= mTransform[self].position
        pos.nor()
        pos *= 6 * world.delta
        pos += mTransform[self].position
        mTransform[self].position = pos
    }

    override fun process(entityId: Int) {
        mAnimation[entityId].let {
            it.progress += 1
            when (it.type) {
                AnimationType.ATTACK -> {
                    if (it.progress <= 5) {
                        move(entityId, it.target)
                        if (it.progress >= 5) {
                            it.callback?.invoke()
                        }
                    } else {
                        val tile = mUnit[entityId].tile
                        move(entityId, tile)
                        if (it.progress >= 10) {
                            mTransform[entityId].position = mTransform[tile].position
                            mAnimation.remove(entityId)
                        }
                    }
                }
            }
        }
    }
}

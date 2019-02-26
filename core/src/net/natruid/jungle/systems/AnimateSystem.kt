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
    private lateinit var pathFollowSystem: PathFollowSystem

    val ready get() = entityIds.isEmpty && pathFollowSystem.ready

    private val vector2 = Vector2()

    private fun move(self: Int, target: Int) {
        vector2.set(mTransform[target].position)
        vector2 -= mTransform[self].position
        vector2.nor()
        vector2 *= 400f * world.delta
        vector2 += mTransform[self].position
        mTransform[self].position.set(vector2)
    }

    override fun process(entityId: Int) {
        mAnimation[entityId].let {
            it.time += world.delta
            when (it.type) {
                AnimationType.ATTACK -> {
                    if (it.time <= 0.05f) {
                        move(entityId, it.target)
                    } else {
                        it.callback?.apply {
                            invoke()
                            it.callback = null
                        }
                        val tile = mUnit[entityId].tile
                        move(entityId, tile)
                        if (it.time >= 0.1f) {
                            mTransform[entityId].position.set(mTransform[tile].position)
                            mAnimation.remove(entityId)
                        }
                    }
                }
            }
        }
    }
}

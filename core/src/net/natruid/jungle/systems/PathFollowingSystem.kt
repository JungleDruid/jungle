package net.natruid.jungle.systems

import com.badlogic.ashley.core.EntitySystem
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import ktx.math.minus
import ktx.math.plusAssign
import ktx.math.timesAssign
import net.natruid.jungle.components.PathFollowerComponent
import net.natruid.jungle.components.TransformComponent

class PathFollowingSystem : EntitySystem() {
    companion object {
        private const val speed = 1000f
    }

    private val family = allOf(TransformComponent::class, PathFollowerComponent::class).get()
    private val transformMap = mapperFor<TransformComponent>()
    private val pathFollowerMap = mapperFor<PathFollowerComponent>()

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        for (entity in engine.getEntitiesFor(family)) {
            val transform = transformMap[entity]
            val pathFollower = pathFollowerMap[entity]
            val v = pathFollower.destination!! - transform.position
            val len2 = v.len2()
            if (len2 != 0f && len2 != 1f) {
                v.scl(1f / Math.sqrt(len2.toDouble()).toFloat())
            }
            v *= speed * deltaTime
            if (len2 > v.len2()) {
                v += transform.position
                transform.position = v
            } else {
                transform.position = pathFollower.destination!!
            }

            if (transform.position == pathFollower.destination) {
                val next = pathFollower.next()
                if (next == null) {
                    entity.remove(PathFollowerComponent::class.java)
                    continue
                }
            }
        }
    }
}
package net.natruid.jungle.systems

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.math.Vector3
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import ktx.math.minusAssign
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
    private val tempVector = Vector3()

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        for (entity in engine.getEntitiesFor(family)) {
            val transform = transformMap[entity]
            val pathFollower = pathFollowerMap[entity]
            tempVector.set(pathFollower.destination)
            tempVector -= transform.position
            val dist = tempVector.len2()
            if (dist != 0f && dist != 1f) {
                tempVector.scl(1f / Math.sqrt(dist.toDouble()).toFloat())
            }
            tempVector *= speed * deltaTime
            if (dist > tempVector.len2()) {
                transform.position += tempVector
            } else {
                transform.position.set(pathFollower.destination)
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
package net.natruid.jungle.systems

import com.artemis.Aspect
import com.artemis.ComponentMapper
import com.artemis.systems.IteratingSystem
import ktx.math.minus
import ktx.math.plusAssign
import ktx.math.timesAssign
import net.natruid.jungle.components.PathFollowerComponent
import net.natruid.jungle.components.TransformComponent

class PathFollowingSystem : IteratingSystem(Aspect.all(
    TransformComponent::class.java,
    PathFollowerComponent::class.java
)) {
    companion object {
        private const val speed = 1000f
    }

    private val tiles by lazy { world.getSystem(TileSystem::class.java) }
    private lateinit var mTransform: ComponentMapper<TransformComponent>
    private lateinit var mPathFollower: ComponentMapper<PathFollowerComponent>

    override fun process(entityId: Int) {
        val transform = mTransform[entityId]
        val pathFollower = mPathFollower[entityId]
        val path = pathFollower.path ?: return
        val destination = tiles.getPosition(path[pathFollower.index])!!
        val v = destination - transform.position
        val len2 = v.len2()
        if (len2 != 0f && len2 != 1f) {
            v.scl(1f / Math.sqrt(len2.toDouble()).toFloat())
        }
        v *= speed * world.delta
        if (len2 > v.len2()) {
            v += transform.position
            transform.position = v
        } else {
            transform.position = destination
        }

        if (transform.position == destination) {
            if (pathFollower.index < path.size - 1) {
                pathFollower.index += 1
            } else {
                mPathFollower.remove(entityId)
            }
        }
    }
}

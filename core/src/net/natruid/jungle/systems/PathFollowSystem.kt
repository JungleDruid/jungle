package net.natruid.jungle.systems

import com.artemis.Aspect
import com.artemis.ComponentMapper
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import ktx.math.minusAssign
import ktx.math.plusAssign
import ktx.math.timesAssign
import net.natruid.jungle.components.PathFollowerComponent
import net.natruid.jungle.components.render.PosComponent

class PathFollowSystem : IteratingSystem(Aspect.all(
    PosComponent::class.java,
    PathFollowerComponent::class.java
)) {
    companion object {
        private const val speed = 1000f
    }

    val ready get() = entityIds.isEmpty

    private lateinit var mPos: ComponentMapper<PosComponent>
    private lateinit var mPathFollower: ComponentMapper<PathFollowerComponent>
    private lateinit var behaviorSystem: BehaviorSystem

    private val v = Vector2()

    override fun process(entityId: Int) {
        val pos = mPos[entityId]
        val pathFollower = mPathFollower[entityId]
        val path = pathFollower.path ?: return
        val destination = mPos[path.peek().tile].xy
        v.set(destination)
        v -= pos.xy
        val len2 = v.len2()
        if (len2 != 0f && len2 != 1f) {
            v.scl(1f / Math.sqrt(len2.toDouble()).toFloat())
        }
        v *= speed * world.delta
        if (len2 > v.len2()) {
            v += pos.xy
            pos.set(v)
        } else {
            pos.set(destination)
        }

        if (pos.xy == destination) {
            behaviorSystem.checkAlert(entityId, path.peek().tile)
            if (path.size > 1) {
                path.remove()
            } else {
                mPathFollower[entityId].callback?.invoke()
                mPathFollower.remove(entityId)
            }
        }
    }
}

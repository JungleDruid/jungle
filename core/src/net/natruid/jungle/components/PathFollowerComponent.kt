package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Queue
import net.natruid.jungle.systems.TileSystem

class PathFollowerComponent : Component, Pool.Poolable {
    private var path: Array<TileComponent>? = null
    private val queue = Queue<Vector3>()
    var destination: Vector3? = null
        private set
    val hasNext get() = queue.size > 0

    fun setPath(path: Array<TileComponent>, tileSystem: TileSystem) {
        queue.clear()
        path.forEach {
            queue.addLast(tileSystem.getPosition(it))
        }
        destination = queue.removeFirst()
    }

    fun next(): Vector3? {
        if (!hasNext) return null
        destination = queue.removeFirst()
        return destination
    }

    override fun reset() {
        path = null
        queue.clear()
        destination = null
    }
}
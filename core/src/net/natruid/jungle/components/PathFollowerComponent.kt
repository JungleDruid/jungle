package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Queue
import net.natruid.jungle.systems.TileSystem

class PathFollowerComponent(path: Array<TileComponent>, tileSystem: TileSystem) : Component {
    private val queue by lazy { Queue<Vector3>() }
    var destination: Vector3? = null
        private set
    val hasNext get() = queue.size > 0

    init {
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
}
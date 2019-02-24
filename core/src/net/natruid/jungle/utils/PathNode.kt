package net.natruid.jungle.utils

import com.badlogic.gdx.utils.BinaryHeap
import java.util.*

data class PathNode(var tile: Int, var cost: Float, var prev: PathNode? = null) : BinaryHeap.Node(0f) {
    fun buildPath(): Path {
        val path = LinkedList<PathNode>()
        var current: PathNode? = this
        while (current != null) {
            path.addFirst(current)
            current = current.prev
        }
        return path
    }
}

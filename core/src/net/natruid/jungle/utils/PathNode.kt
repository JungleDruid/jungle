package net.natruid.jungle.utils

import com.badlogic.gdx.utils.BinaryHeap

data class PathNode(var tile: Int, var cost: Float, var prev: PathNode? = null) : BinaryHeap.Node(0f)

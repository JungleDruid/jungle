package net.natruid.jungle.utils

import com.badlogic.gdx.utils.BinaryHeap
import net.natruid.jungle.components.TileComponent

data class PathNode(var tile: TileComponent, var cost: Float, var prev: PathNode? = null) : BinaryHeap.Node(0f)
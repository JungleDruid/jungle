package net.natruid.jungle.utils

import net.natruid.jungle.components.TileComponent

data class PathNode(var tile: TileComponent?, var length: Float, var prev: PathNode? = null)
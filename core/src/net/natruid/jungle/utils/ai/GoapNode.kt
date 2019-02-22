package net.natruid.jungle.utils.ai

data class GoapNode(
    val agentId: Int,
    val parent: GoapNode?,
    val cost: Float,
    val state: Map<GoapType, Boolean>,
    val action: GoapAction?
)

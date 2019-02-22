package net.natruid.jungle.systems

import com.artemis.Aspect
import com.artemis.ComponentMapper
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import ktx.async.skipFrame
import net.natruid.jungle.components.GoapComponent
import net.natruid.jungle.components.UnitComponent
import net.natruid.jungle.systems.abstracts.SortedIteratingSystem
import net.natruid.jungle.utils.Faction
import net.natruid.jungle.utils.FactionComparator
import net.natruid.jungle.utils.Logger
import net.natruid.jungle.utils.ai.GoapAction
import net.natruid.jungle.utils.ai.GoapNode
import net.natruid.jungle.utils.ai.GoapType
import java.util.*

class GoapSystem : SortedIteratingSystem(Aspect.all(GoapComponent::class.java)) {
    override val comparator = FactionComparator()

    private enum class Phase { READY, PLANNING, PERFORMING, STOPPING, STOPPED }

    private lateinit var combatTurnSystem: CombatTurnSystem
    private lateinit var unitManageSystem: UnitManageSystem
    private lateinit var pathFollowSystem: PathFollowSystem
    private lateinit var mUnit: ComponentMapper<UnitComponent>
    private lateinit var mGoap: ComponentMapper<GoapComponent>

    private val goals = mapOf(Pair(Pair(GoapType.CLOSER_TO_ENEMY, true), 5))
    private val worldState = HashMap<GoapType, Boolean>()
    var allies: IntArray? = null
        private set
    var enemies: IntArray? = null
        private set
    private var firstIndex = Int.MAX_VALUE
    private var lastIndex = Int.MIN_VALUE
    private var currentNode: GoapNode? = null
    private var currentJob: Job? = null

    private var phase = Phase.STOPPED

    fun prepare() {
        if (phase == Phase.READY) return
        if (phase != Phase.STOPPED) error("GoapSystem has not stopped yet: $phase")
        phase = Phase.READY
    }

    fun reset() {
        currentJob?.cancel()
        currentJob = null
        worldState.clear()
        allies = null
        enemies = null
        phase = Phase.STOPPED
    }

    private fun inState(state: Map<GoapType, Boolean>, precondition: Pair<GoapType, Boolean>): Boolean {
        var s = state
        val (key, value) = precondition
        if (!s.containsKey(key)) {
            s = worldState
            if (!s.containsKey(key)) {
                if (!generateState(key)) return false
            }
        }
        return s[key] == value
    }

    private fun inState(state: Map<GoapType, Boolean>, preconditions: Map<GoapType, Boolean>): Boolean {
        for (precondition in preconditions) {
            if (!inState(state, precondition.toPair())) return false
        }
        return true
    }

    private fun calculateIndex() {
        firstIndex = Int.MAX_VALUE
        lastIndex = Int.MIN_VALUE
        val currentFaction = combatTurnSystem.faction
        for ((index, id) in sortedEntityIds.withIndex()) {
            val faction = mUnit[id].faction
            if (faction.ordinal < currentFaction.ordinal) continue
            if (faction.ordinal > currentFaction.ordinal) break
            if (faction == currentFaction) {
                if (index < firstIndex) firstIndex = index
                if (index > lastIndex) lastIndex = index
            }
        }
    }

    override fun processSystem() {
        when (phase) {
            Phase.READY -> {
                if (combatTurnSystem.faction == Faction.PLAYER) return
                phase = Phase.PLANNING
                calculateIndex()
                currentJob = KtxAsync.launch {
                    phase = if (plan()) Phase.PERFORMING else Phase.STOPPING
                }
            }
            Phase.PLANNING -> {
            }
            Phase.PERFORMING -> {
                val node = currentNode!!
                node.action!!.perform(node.agentId)
                var hasTurn = false
                loopAgents { id ->
                    if (mUnit[id].hasTurn) {
                        hasTurn = true
                        true
                    } else false
                }
                if (hasTurn) {
                    phase = Phase.PLANNING
                    currentJob = KtxAsync.launch {
                        while (!pathFollowSystem.ready) skipFrame()
                        phase = if (plan()) Phase.PERFORMING else Phase.STOPPING
                    }
                } else {
                    phase = Phase.STOPPED
                }
            }
            Phase.STOPPING -> {
                loopAgents { id ->
                    unitManageSystem.endTurn(id)
                    false
                }
                phase = Phase.STOPPED
            }
            Phase.STOPPED -> {
            }
        }
    }

    private inline fun loopAgents(function: (id: Int) -> Boolean) {
        val ids = sortedEntityIds
        for (i in firstIndex..lastIndex) {
            if (function(ids[i])) break
        }
    }

    private suspend fun plan(): Boolean {
        // init
        loopAgents { id ->
            for (action in mGoap[id].availableActions) {
                world.inject(action)
                action.reset()
            }
            false
        }

        val leaves = HashMap<Int, GoapNode>()
        for ((goal, score) in goals) {
            loopAgents { id ->
                val agent = mGoap[id]
                val start = GoapNode(id, null, 0f, agent.state, null)
                val success = buildGraph(start, leaves, agent.availableActions, goal, score)
                if (!success) {
                    Logger.debug { "No plan!" }
                }
                false
            }
        }

        currentNode = null
        var highestScore = Int.MIN_VALUE
        for ((score, leaf) in leaves) {
            if (highestScore < score) {
                currentNode = leaf
                highestScore = score
            }
        }

        return currentNode != null
    }

    private suspend fun buildGraph(
        parent: GoapNode,
        leaves: HashMap<Int, GoapNode>,
        usableActions: Set<GoapAction>,
        goal: Pair<GoapType, Boolean>,
        score: Int
    ): Boolean {
        var found = false

        usableActions.forEach { action ->
            skipFrame()
            val available = inState(parent.state, action.preconditions)
            val cost = action.check(parent.agentId) ?: return@forEach
            if (available && cost >= 0) {
                val currentState = populateState(parent.state, action.effects)
                val node = GoapNode(parent.agentId, parent, parent.cost + cost, currentState, action)

                if (inState(currentState, goal)) {
                    val old = leaves[score]
                    if (old == null || old.cost > node.cost) leaves[score] = node
                    found = true
                } else if (usableActions.size > 1) {
                    val subset = HashSet(usableActions)
                    subset.remove(action)
                    if (buildGraph(node, leaves, subset, goal, score)) found = true
                }
            }
        }

        return found
    }

    private fun populateState(
        currentState: Map<GoapType, Boolean>,
        effects: Map<GoapType, Boolean>
    ): HashMap<GoapType, Boolean> {
        val newState = HashMap(currentState)
        for ((key, value) in effects) {
            newState[key] = value
        }
        return newState
    }

    private fun generateState(type: GoapType): Boolean {
        when (type) {
            GoapType.HAS_ENEMY -> {
                var array = enemies
                if (array == null) {
                    array = unitManageSystem.getEnemies(combatTurnSystem.faction)
                    enemies = array
                }
                worldState[type] = array.isNotEmpty()
            }
            GoapType.HAS_ALLY -> {
                var array = allies
                if (array == null) {
                    array = unitManageSystem.getAllies(combatTurnSystem.faction)
                    allies = array
                }
                worldState[type] = array.isNotEmpty()
            }
            else -> return false
        }

        return true
    }

    override fun process(entityId: Int) {}
}

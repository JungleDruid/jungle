package net.natruid.jungle.systems

import com.artemis.Aspect
import com.artemis.ComponentMapper
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import ktx.async.skipFrame
import net.natruid.jungle.components.BehaviorComponent
import net.natruid.jungle.components.UnitComponent
import net.natruid.jungle.systems.abstracts.SortedIteratingSystem
import net.natruid.jungle.utils.Faction
import net.natruid.jungle.utils.FactionComparator
import net.natruid.jungle.utils.UnitCondition
import net.natruid.jungle.utils.UnitTargetType

class BehaviorSystem : SortedIteratingSystem(Aspect.all(BehaviorComponent::class.java)) {
    override val comparator = FactionComparator()

    private enum class Phase { READY, PLANNING, PERFORMING, STOPPING, STOPPED }

    private lateinit var unitManageSystem: UnitManageSystem
    private lateinit var combatTurnSystem: CombatTurnSystem
    private lateinit var animateSystem: AnimateSystem
    private lateinit var tileSystem: TileSystem
    private lateinit var mUnit: ComponentMapper<UnitComponent>
    private lateinit var mBehavior: ComponentMapper<BehaviorComponent>

    private val unitGroup = HashMap<UnitTargetType, List<Int>>()
    private var phase = Phase.STOPPED
    private var firstIndex = Int.MAX_VALUE
    private var lastIndex = Int.MIN_VALUE
    private var entitySize = 0
    private var currentJob: Job? = null
    private val asyncJobs = HashSet<Deferred<Pair<Int, Float>?>>()
    private var currentUnit = -1
    private val scoreMap = mapOf(Pair("kill", 1000f), Pair("damage", 100f))

    fun prepare() {
        if (phase == Phase.READY) return
        if (phase != Phase.STOPPED) error("GoapSystem has not stopped yet: $phase")
        phase = Phase.READY
    }

    fun reset() {
        unitGroup.clear()
        currentJob?.cancel()
        currentJob = null
        asyncJobs.forEach { it.cancel() }
        asyncJobs.clear()
        currentUnit = -1
        phase = Phase.STOPPED
    }

    private fun calculateIndex(): Boolean {
        firstIndex = Int.MAX_VALUE
        lastIndex = Int.MIN_VALUE
        entitySize = sortedEntityIds.size
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
        return firstIndex != Int.MAX_VALUE && lastIndex != Int.MIN_VALUE
    }

    private inline fun loopAgents(function: (id: Int) -> Boolean) {
        val ids = sortedEntityIds
        if (entitySize != ids.size) calculateIndex()
        for (i in firstIndex..lastIndex) {
            if (function(ids[i])) break
        }
    }

    override fun processSystem() {
        when (phase) {
            Phase.READY -> {
                if (combatTurnSystem.faction == Faction.PLAYER) return
                phase = Phase.PLANNING
                currentJob = KtxAsync.launch {
                    phase = if (!calculateIndex()) {
                        Phase.STOPPED
                    } else {
                        loopAgents { id ->
                            mBehavior[id].tree.init(world, id)
                            false
                        }
                        if (plan()) Phase.PERFORMING else Phase.STOPPING
                    }
                }
            }
            Phase.PLANNING -> {
            }
            Phase.PERFORMING -> {
                assert(currentUnit >= 0)
                val unit = currentUnit
                mBehavior[unit].execution?.execute()
                mBehavior[unit].execution = null
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
                        while (!animateSystem.ready) skipFrame()
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

    private suspend fun plan(): Boolean {
        // init
        loopAgents { id ->
            mBehavior[id].tree.reset()
            false
        }

        var bestUnit = -1
        var highestScore = 0f
        loopAgents { id ->
            asyncJobs.add(KtxAsync.async {
                val agent = mBehavior[id]
                val success = agent.tree.run()
                if (success) {
                    Pair(id, agent.score)
                } else {
                    null
                }
            })
            false
        }
        for (job in asyncJobs) {
            val (id, score) = job.await() ?: continue
            if (bestUnit < 0 || highestScore < score) {
                bestUnit = id
                highestScore = score
            }
        }

        asyncJobs.clear()
        currentUnit = bestUnit

        return bestUnit >= 0
    }

    fun getUnitGroup(targetType: UnitTargetType): List<Int> {
        var group = unitGroup[targetType]
        if (group == null) {
            group = when (targetType) {
                UnitTargetType.ANY -> unitManageSystem.getUnits()
                UnitTargetType.FRIENDLY -> unitManageSystem.getAllies(combatTurnSystem.faction)
                UnitTargetType.HOSTILE -> unitManageSystem.getEnemies(combatTurnSystem.faction)
            }
            unitGroup[targetType] = group
        }
        return group
    }

    fun getSortedUnitList(self: Int, targetType: UnitTargetType, condition: UnitCondition): ArrayList<Int> {
        val list = ArrayList(getUnitGroup(targetType))
        when (condition) {
            UnitCondition.WEAK -> {
                list.sortBy { mUnit[it].hp }
            }
            UnitCondition.STRONG -> {
                list.sortByDescending { mUnit[it].hp }
            }
            UnitCondition.CLOSE -> {
                list.sortBy {
                    tileSystem.getDistance(mUnit[it].tile, mUnit[self].tile)
                }
            }
            UnitCondition.FAR -> {
                list.sortByDescending {
                    tileSystem.getDistance(mUnit[it].tile, mUnit[self].tile)
                }
            }
        }
        return list
    }

    fun getUnit(self: Int, targetType: UnitTargetType, condition: UnitCondition): Int {
        val group = getUnitGroup(targetType)
        (group as ArrayList).removeIf { mUnit[it] == null }
        return when (condition) {
            UnitCondition.WEAK -> group.minBy { mUnit[it].hp }
            UnitCondition.STRONG -> group.maxBy { mUnit[it].hp }
            UnitCondition.CLOSE -> group.minBy { tileSystem.getDistance(mUnit[it].tile, mUnit[self].tile) }
            UnitCondition.FAR -> group.maxBy { tileSystem.getDistance(mUnit[it].tile, mUnit[self].tile) }
        } ?: -1
    }

    fun getScore(result: String, apCost: Int, amount: Float): Float {
        var score = scoreMap[result] ?: 0f
        score += (amount - apCost) / 100f
        return score
    }

    override fun process(entityId: Int) {}
}

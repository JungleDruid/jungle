package net.natruid.jungle.systems

import com.artemis.Aspect
import com.artemis.BaseEntitySystem
import com.artemis.ComponentMapper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import ktx.async.KtxAsync
import ktx.async.skipFrame
import net.natruid.jungle.components.BehaviorComponent
import net.natruid.jungle.components.TurnComponent
import net.natruid.jungle.components.UnitComponent
import net.natruid.jungle.utils.types.UnitCondition
import net.natruid.jungle.utils.types.UnitTargetType

class BehaviorSystem : BaseEntitySystem(Aspect.all(
    BehaviorComponent::class.java, TurnComponent::class.java
)) {
    val ready get() = phase == Phase.READY || phase == Phase.STOPPED

    private enum class Phase { READY, CHECKING, PLANNING, PERFORMING, STOPPING, STOPPED }

    private lateinit var unitManageSystem: UnitManageSystem
    private lateinit var combatTurnSystem: CombatTurnSystem
    private lateinit var flowControlSystem: FlowControlSystem
    private lateinit var tileSystem: TileSystem
    private lateinit var threatSystem: ThreatSystem
    private lateinit var mUnit: ComponentMapper<UnitComponent>
    private lateinit var mBehavior: ComponentMapper<BehaviorComponent>

    private val unitGroup = HashMap<UnitTargetType, List<Int>>()
    private var phase = Phase.STOPPED
    private var currentJob: Job? = null
    private var planJob: Job? = null
    private var currentUnit = -1
    private var performingUnit = -1
    private val scoreMap = mapOf(Pair("kill", 1000f), Pair("damage", 100f))

    fun prepare() {
        if (phase == Phase.READY) return
        if (phase != Phase.STOPPED) error("GoapSystem has not stopped yet: $phase")
        phase = Phase.READY
    }

    fun reset() {
        currentJob?.apply {
            cancel()
            currentJob = null
        }
        planJob?.apply {
            cancel()
            planJob = null
        }
        currentUnit = -1
        unitGroup.clear()
        phase = Phase.STOPPED
    }

    private inline fun loopAgents(idle: Boolean = false, function: (id: Int) -> Boolean) {
        val data = entityIds.data
        for (i in 0 until entityIds.size()) {
            val unit = data[i]
            val noThreat = mBehavior[unit].threatMap.size == 0
            if (noThreat != idle) continue
            if (function(unit)) break
        }
    }

    override fun inserted(entityId: Int) {
        super.inserted(entityId)
        mBehavior[entityId].tree.init(world, entityId)
    }

    override fun processSystem() {
        when (phase) {
            Phase.READY -> {
                phase = if (entityIds.size() == 0) {
                    Phase.STOPPING
                } else {
                    for (unit in getUnitGroup(UnitTargetType.ANY)) {
                        threatSystem.checkAlert(unit)
                    }
                    Phase.CHECKING
                }
            }
            Phase.CHECKING -> {
                phase = Phase.PLANNING
                currentJob = KtxAsync.launch {
                    while (!flowControlSystem.ready || unitManageSystem.isBusy(performingUnit)) skipFrame()
                    phase = if (plan()) Phase.PERFORMING else Phase.STOPPING
                }
            }
            Phase.PLANNING -> {
            }
            Phase.PERFORMING -> {
                assert(currentUnit >= 0)
                val unit = currentUnit
                performingUnit = unit
                mBehavior[unit].execution?.execute()
                mBehavior[unit].execution = null
                phase = Phase.CHECKING
            }
            Phase.STOPPING -> {
                loopAgents { id ->
                    combatTurnSystem.endTurn(id)
                    false
                }
                loopAgents(true) { id ->
                    combatTurnSystem.endTurn(id)
                    false
                }
                phase = Phase.STOPPED
            }
            Phase.STOPPED -> {
            }
        }
    }

    private var highestScore = 0f

    @Synchronized
    private fun setBestUnit(unit: Int, score: Float) {
        if (currentUnit < 0 || highestScore < score) {
            currentUnit = unit
            highestScore = score
        }
    }

    private suspend fun plan(): Boolean {
        // init
        loopAgents { id ->
            mBehavior[id].tree.reset()
            false
        }

        highestScore = 0f
        currentUnit = -1

        planJob = GlobalScope.launch {
            supervisorScope {
                loopAgents { id ->
                    launch {
                        val agent = mBehavior[id]
                        val success = agent.tree.run()
                        if (success) {
                            setBestUnit(id, agent.score)
                        }
                    }
                    false
                }
            }
        }

        planJob!!.join()
        planJob = null

        return currentUnit >= 0
    }

    @Synchronized
    fun getUnitGroup(targetType: UnitTargetType): List<Int> {
        var group = unitGroup[targetType]
        if (group == null) {
            group = when (targetType) {
                UnitTargetType.ANY -> unitManageSystem.units
                UnitTargetType.FRIENDLY -> unitManageSystem.getAllies(combatTurnSystem.faction)
                UnitTargetType.HOSTILE -> unitManageSystem.getEnemies(combatTurnSystem.faction)
            }
            unitGroup[targetType] = group
        }
        return group!!
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

    @Synchronized
    fun getUnit(self: Int, targetType: UnitTargetType, condition: UnitCondition): Int {
        val group = getUnitGroup(targetType)
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
}

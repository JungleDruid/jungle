package net.natruid.jungle.systems

import com.artemis.Aspect
import com.artemis.BaseEntitySystem
import com.artemis.ComponentMapper
import net.natruid.jungle.components.StatsComponent
import net.natruid.jungle.components.TurnComponent
import net.natruid.jungle.components.UnitComponent
import net.natruid.jungle.utils.Constants
import net.natruid.jungle.utils.Faction
import net.natruid.jungle.utils.Logger

class CombatTurnSystem : BaseEntitySystem(Aspect.all(TurnComponent::class.java)) {
    private enum class Phase { NONE, START, READY, NEXT_TURN }

    var turn = 0
        private set

    val faction get() = factionList[currentFactionIndex]

    private val factionList = ArrayList<Faction>()
    private var currentFactionIndex = 0
    private var phase = Phase.NONE

    private lateinit var behaviorSystem: BehaviorSystem
    private lateinit var flowControlSystem: FlowControlSystem
    private lateinit var mTurn: ComponentMapper<TurnComponent>
    private lateinit var mUnit: ComponentMapper<UnitComponent>
    private lateinit var mStats: ComponentMapper<StatsComponent>

    private val unitSubscription by lazy {
        world.aspectSubscriptionManager.get(Aspect.all(UnitComponent::class.java))
    }

    fun start() {
        phase = Phase.START
    }

    fun addFaction(faction: Faction) {
        if (factionList.contains(faction)) return
        factionList.add(faction)
    }

    fun removeFaction(faction: Faction) {
        factionList.remove(faction)
    }

    fun reset() {
        factionList.clear()
        currentFactionIndex = 0
        phase = Phase.NONE
    }

    override fun processSystem() {
        when (phase) {
            Phase.START -> {
                giveTurn(factionList[currentFactionIndex])
                phase = Phase.READY
            }
            Phase.READY -> {
                if (entityIds.isEmpty) {
                    phase = Phase.NEXT_TURN
                }
            }
            Phase.NEXT_TURN -> {
                if (!flowControlSystem.ready) return
                if (currentFactionIndex >= factionList.size - 1) {
                    turn += 1
                    currentFactionIndex = 0
                } else {
                    currentFactionIndex += 1
                }

                val nextFaction = factionList[currentFactionIndex]
                Logger.debug { "Turn ended. Next faction: $nextFaction" }
                giveTurn(nextFaction)
                behaviorSystem.prepare()
                phase = Phase.READY
            }
            else -> {
            }
        }
    }

    fun giveTurn(faction: Faction) {
        val entities = unitSubscription.entities
        val data = entities.data
        for (i in 0 until entities.size()) {
            val unit = data[i]
            val cUnit = mUnit[unit]

            if (cUnit.faction != faction) continue

            cUnit.ap = (cUnit.ap + 4 + mStats[unit].ap).coerceAtMost(Constants.MAX_AP)
            mTurn.create(unit)
        }
    }

    fun endTurn(unit: Int) {
        if (!mTurn.has(unit)) return
        mTurn.remove(unit)
    }
}

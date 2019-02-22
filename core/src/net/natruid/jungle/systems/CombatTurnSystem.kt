package net.natruid.jungle.systems

import com.artemis.BaseSystem
import net.natruid.jungle.utils.Faction
import net.natruid.jungle.utils.Logger

class CombatTurnSystem : BaseSystem() {
    private enum class Phase { NONE, START, READY, NEXT_TURN }

    var turn = 0
        private set

    val faction get() = factionList[currentFactionIndex]

    private val factionList = ArrayList<Faction>()
    private var currentFactionIndex = 0
    private var phase = Phase.NONE

    private lateinit var unitManageSystem: UnitManageSystem
    private lateinit var goapSystem: GoapSystem
    private lateinit var pathFollowSystem: PathFollowSystem

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

    fun nextTurn() {
        phase = Phase.NEXT_TURN
    }

    fun reset() {
        factionList.clear()
        currentFactionIndex = 0
        phase = Phase.NONE
    }

    override fun processSystem() {
        when (phase) {
            Phase.START -> {
                unitManageSystem.giveTurn(factionList[currentFactionIndex])
                phase = Phase.READY
            }
            Phase.NEXT_TURN -> {
                if (!pathFollowSystem.ready) return
                if (currentFactionIndex == factionList.size - 1) {
                    turn += 1
                    currentFactionIndex = 0
                } else {
                    currentFactionIndex += 1
                }

                val nextFaction = factionList[currentFactionIndex]
                Logger.debug { "Turn ended. Next faction: $nextFaction" }
                unitManageSystem.giveTurn(nextFaction)
                goapSystem.prepare()
                phase = Phase.READY
            }
            else -> {
            }
        }
    }
}

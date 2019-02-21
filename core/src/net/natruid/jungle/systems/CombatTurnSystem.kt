package net.natruid.jungle.systems

import com.artemis.BaseSystem
import net.natruid.jungle.utils.Faction
import net.natruid.jungle.utils.Logger

class CombatTurnSystem : BaseSystem() {
    var turn = 0
        private set

    private val factionList = ArrayList<Faction>()
    private var currentFactionIndex = 0
    private var started = false
    private var shouldStart = false

    private lateinit var unitManageSystem: UnitManageSystem

    fun start() {
        shouldStart = true
    }

    fun addFaction(faction: Faction) {
        if (factionList.contains(faction)) return
        factionList.add(faction)
    }

    fun removeFaction(faction: Faction) {
        factionList.remove(faction)
    }

    fun nextTurn() {
        if (currentFactionIndex == factionList.size - 1) {
            turn += 1
            currentFactionIndex = 0
        } else {
            currentFactionIndex += 1
        }

        val nextFaction = factionList[currentFactionIndex]
        Logger.debug { "Turn ended. Next faction: $nextFaction" }
        unitManageSystem.giveTurn(nextFaction)
    }

    fun reset() {
        factionList.clear()
        currentFactionIndex = 0
        started = false
        shouldStart = false
    }

    override fun processSystem() {
        if (shouldStart && !started) {
            unitManageSystem.giveTurn(factionList[currentFactionIndex])
            started = false
            shouldStart = false
        }
    }
}

package net.natruid.jungle.systems

import com.artemis.Aspect
import com.artemis.ComponentMapper
import com.artemis.managers.TagManager
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Align
import net.natruid.jungle.components.*
import net.natruid.jungle.core.Marsh
import net.natruid.jungle.systems.abstracts.SortedIteratingSystem
import net.natruid.jungle.utils.*
import net.natruid.jungle.views.SkillBarView
import java.util.*
import kotlin.math.ceil

class UnitManageSystem : SortedIteratingSystem(
    Aspect.all(TransformComponent::class.java, UnitComponent::class.java)
), InputProcessor {
    override val comparator by lazy {
        Comparator<Int> { p0, p1 ->
            val f0 = mUnit[p0].faction.ordinal
            val f1 = mUnit[p1].faction.ordinal
            when {
                f0 > f1 -> 1
                f0 < f1 -> -1
                else -> 0
            }
        }
    }

    private lateinit var mUnit: ComponentMapper<UnitComponent>
    private lateinit var mTransform: ComponentMapper<TransformComponent>
    private lateinit var mPathFollower: ComponentMapper<PathFollowerComponent>
    private lateinit var mTile: ComponentMapper<TileComponent>
    private lateinit var mLabel: ComponentMapper<LabelComponent>
    private lateinit var mStats: ComponentMapper<StatsComponent>
    private lateinit var mAttributes: ComponentMapper<AttributesComponent>
    private lateinit var tileSystem: TileSystem
    private lateinit var pathfinderSystem: PathfinderSystem
    private lateinit var indicateSystem: IndicateSystem
    private lateinit var combatTurnSystem: CombatTurnSystem
    private lateinit var viewManageSystem: ViewManageSystem
    private lateinit var tagManager: TagManager

    private var unitsHasTurn = -1
    private var selectedUnit
        get() = tagManager.getEntityId("SELECTED_UNIT")
        set(value) {
            if (value >= 0) tagManager.register("SELECTED_UNIT", value)
            else tagManager.unregister("SELECTED_UNIT")
        }

    fun reset() {
        selectedUnit = -1
        unitsHasTurn = -1
    }

    fun getUnit(coord: Point): Int {
        return mTile[tileSystem[coord]]?.unit ?: -1
    }

    fun addUnit(
        coord: Point = Point(),
        faction: Faction = Faction.NONE
    ): Int {
        val tile = tileSystem[coord]
        assert(tile >= 0)
        val entityId = world.create()
        mTransform.create(entityId).apply {
            position = mTransform[tile].position
            z = Constants.Z_UNIT
        }
        mUnit.create(entityId).apply {
            this.tile = tile
            this.faction = faction
        }
        mAttributes.create(entityId).apply {
            base.fill(12)
        }
        mStats.create(entityId)
        mLabel.create(entityId).apply {
            fontName = "huge"
            color = when (faction) {
                Faction.NONE -> Color.GRAY
                Faction.PLAYER -> Color.GREEN
                Faction.ENEMY -> Color.RED
            }
            text = when (faction) {
                Faction.NONE -> "？"
                Faction.PLAYER -> "Ｎ"
                Faction.ENEMY -> "Ｄ"
            }
            align = Align.center
        }
        mTile[tile].unit = entityId
        calculateStats(entityId)
        combatTurnSystem.addFaction(faction)
        return entityId
    }

    fun moveUnit(unit: Int, tile: Int): Boolean {
        val cUnit = mUnit[unit]
        val area = pathfinderSystem.area(cUnit.tile, getMovement(unit))
        val path = pathfinderSystem.extractPath(area.asIterable(), tile) ?: return false
        moveUnit(unit, path)
        return true
    }

    fun moveUnit(unit: Int, path: Deque<PathNode>, free: Boolean = false) {
        if (unit < 0) return
        mPathFollower.create(unit).apply {
            if (this.path == null) {
                this.path = path
            } else {
                this.path!!.addAll(path)
            }
        }
        val dest = path.peekLast()
        val cUnit = mUnit[unit]
        useAp(unit, if (free) 0 else getMovementCost(unit, dest.cost)) {
            mTile[cUnit.tile]!!.unit = -1
            cUnit.tile = dest.tile
            mTile[dest.tile]!!.unit = unit
        }
    }

    fun freeMoveUnit(unit: Int, goal: Point) {
        val cUnit = mUnit[unit] ?: return
        val path = pathfinderSystem.path(cUnit.tile, tileSystem[goal]) ?: return
        moveUnit(unit, path, true)
    }

    private val statMultipliers = Array(StatType.size) { 1f }
    fun calculateStats(entityId: Int) {
        if (entityId < 0) return
        val cStats = mStats[entityId] ?: return
        if (!cStats.dirty) return
        val cUnit = mUnit[entityId] ?: return
        val cAttr = mAttributes[entityId] ?: return

        statMultipliers.fill(1f)

        val stats = cStats.values
        val attributes = cAttr.modified
        val statDefs = Marsh.statDefs

        // calculate modified attributes
        if (cAttr.dirty) {
            val base = cAttr.base
            for (i in 0 until base.size) {
                attributes[i] = base[i]
            }
            cAttr.dirty = false
        }

        // calculate base stats
        for (i in 0 until stats.size) {
            val def = statDefs[i]
            var value = def.base + def.level * cUnit.level
            def.attributes?.forEach {
                val attr = attributes[it.type.ordinal] - 10
                if (it.add != 0) value += attr * it.add
                if (it.mul != 0f) statMultipliers[i] += attr * it.mul
            }
            stats[i] = value
        }

        // multiply stats with multipliers
        for (i in 0 until stats.size) {
            stats[i] = (stats[i] * statMultipliers[i]).toInt()
        }

        cStats.dirty = false
    }

    fun giveTurn(faction: Faction): Boolean {
        val targetOrdinal = faction.ordinal
        var hasUnit = false
        unitsHasTurn = 0
        for (unit in sortedEntityIds) {
            val cUnit = mUnit[unit]
            val ordinal = cUnit.faction.ordinal
            if (ordinal > targetOrdinal) break
            if (ordinal < targetOrdinal) continue

            cUnit.ap = (cUnit.ap + 4 + mStats[unit].ap).coerceAtMost(Constants.MAX_AP)
            cUnit.hasTurn = true

            unitsHasTurn += 1
            hasUnit = true
        }

        return hasUnit
    }

    fun hasAp(unit: Int, ap: Int): Boolean {
        if (mUnit[unit].ap < ap) return false
        return true
    }

    fun removeAp(unit: Int, ap: Int): Boolean {
        val cUnit = mUnit[unit]
        cUnit.ap -= ap
        viewManageSystem.get<SkillBarView>()?.setAp(cUnit.ap)
        if (cUnit.ap == 0) {
            endTurn(unit)
            return true
        }
        return false
    }

    inline fun useAp(unit: Int, ap: Int, function: () -> Unit): Boolean {
        if (ap > 0 && !hasAp(unit, ap)) return false
        function()
        if (ap <= 0) return false
        return removeAp(unit, ap)
    }

    fun endTurn(unit: Int) {
        mUnit[unit].hasTurn = false
        unitsHasTurn -= 1
    }

    fun getMovement(unit: Int): Float {
        val cUnit = mUnit[unit]
        return mStats[unit].speed * cUnit.ap + cUnit.extraMovement
    }

    private fun showMoveArea(unit: Int) {
        if (unit < 0) return

        val cUnit = mUnit[unit]
        val tile = cUnit.tile
        if (tile >= 0) {
            hideMoveArea(unit)
            if (!indicateSystem.hasResult(unit, IndicatorType.MOVE_AREA)) {
                calculateStats(unit)
                indicateSystem.addResult(unit, IndicatorType.MOVE_AREA, pathfinderSystem.area(tile, getMovement(unit)))
            }
            indicateSystem.show(unit, IndicatorType.MOVE_AREA)
        }
    }

    private fun hideMoveArea(unit: Int) {
        indicateSystem.hide(unit, IndicatorType.MOVE_AREA)
        indicateSystem.hide(unit, IndicatorType.MOVE_PATH)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val mouseCoord = tileSystem.mouseCoord ?: return false

        var unit = getUnit(mouseCoord)
        if (unit >= 0 && button == 0) {
            when (mUnit[unit].faction) {
                Faction.NONE -> {
                }
                Faction.PLAYER -> {
                    if (mUnit[unit].hasTurn) {
                        showMoveArea(unit)
                        selectedUnit = unit
                        viewManageSystem.get<SkillBarView>()?.setAp(mUnit[unit].ap)
                    }
                }
                Faction.ENEMY -> {
                    if (mUnit[unit].hasTurn) {
                        showMoveArea(unit)
                        selectedUnit = unit
                        viewManageSystem.get<SkillBarView>()?.setAp(mUnit[unit].ap)
                    }
                }
            }
            return true
        } else if (selectedUnit >= 0) {
            when (button) {
                0 -> {
                    unit = selectedUnit
                    val path = indicateSystem.getPathTo(tileSystem[mouseCoord], unit)
                    if (path != null) {
                        indicateSystem.remove(unit, IndicatorType.MOVE_AREA)
                        moveUnit(unit, path)
                        if (mUnit[unit].hasTurn) {
                            showMoveArea(unit)
                            return true
                        }
                    }
                    deselectUnit()
                    return true
                }
                1 -> {
                    deselectUnit()
                    return true
                }
            }
        }

        return false
    }

    private fun deselectUnit() {
        hideMoveArea(selectedUnit)
        selectedUnit = -1
        viewManageSystem.get<SkillBarView>()?.hideAp()
    }

    private fun getMovementCost(unit: Int, cost: Float, preview: Boolean = false): Int {
        val cUnit = mUnit[unit]
        var apCost = 0
        if (cost > cUnit.extraMovement) {
            val newCost = cost - cUnit.extraMovement
            val movementPerAp = mStats[unit].speed
            apCost = ceil(newCost / movementPerAp).toInt()
            if (!preview) cUnit.extraMovement = movementPerAp * apCost - newCost
        } else if (!preview) {
            cUnit.extraMovement -= cost
        }

        return apCost
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    private var lastCoord: Point? = null
    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        val mouseCoord = tileSystem.mouseCoord
        if (lastCoord != mouseCoord) {
            if (selectedUnit >= 0) {
                indicateSystem.hide(selectedUnit, IndicatorType.MOVE_PATH)
                if (mouseCoord != null) {
                    val cost = indicateSystem.showPathTo(tileSystem[mouseCoord], selectedUnit)
                    if (cost >= 0) {
                        viewManageSystem.get<SkillBarView>()?.setAp(
                            mUnit[selectedUnit].ap,
                            getMovementCost(selectedUnit, cost, true)
                        )
                    }
                }
            }
            lastCoord = mouseCoord
        }
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        return false
    }

    override fun process(entityId: Int) {
        if (unitsHasTurn == 0) {
            combatTurnSystem.nextTurn()
        }
    }
}

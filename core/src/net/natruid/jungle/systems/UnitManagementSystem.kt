package net.natruid.jungle.systems

import com.artemis.Aspect
import com.artemis.ComponentMapper
import com.artemis.managers.TagManager
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Align
import net.natruid.jungle.components.*
import net.natruid.jungle.core.Marsh
import net.natruid.jungle.utils.*
import net.natruid.jungle.views.SkillBarView
import java.util.*
import kotlin.math.ceil

class UnitManagementSystem : SortedIteratingSystem(
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
    private lateinit var sTile: TileSystem
    private lateinit var sPathfinder: PathfinderSystem
    private lateinit var sIndicator: IndicatorSystem
    private lateinit var sCombatTurn: CombatTurnSystem
    private lateinit var sViewManage: ViewManageSystem
    private lateinit var sTag: TagManager

    private var unitsHasTurn = -1
    private var selectedUnit
        get() = sTag.getEntityId("SELECTED_UNIT")
        set(value) {
            if (value >= 0) sTag.register("SELECTED_UNIT", value)
            else sTag.unregister("SELECTED_UNIT")
        }

    fun reset() {
        selectedUnit = -1
        unitsHasTurn = -1
    }

    fun getUnit(coord: Point): Int {
        return mTile[sTile[coord]]?.unit ?: -1
    }

    fun addUnit(
        coord: Point = Point(),
        faction: Faction = Faction.NONE
    ): Int {
        val tile = sTile[coord]
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
        sCombatTurn.addFaction(faction)
        return entityId
    }

    fun moveUnit(unit: Int, tile: Int): Boolean {
        val cUnit = mUnit[unit]
        val area = sPathfinder.area(cUnit.tile, getMovement(unit))
        val path = sPathfinder.extractPath(area.asIterable(), tile) ?: return false
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
        val path = sPathfinder.path(cUnit.tile, sTile[goal]) ?: return
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
        sViewManage.get<SkillBarView>()?.setAp(cUnit.ap)
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
            if (!sIndicator.hasResult(unit, IndicatorType.MOVE_AREA)) {
                calculateStats(unit)
                sIndicator.addResult(unit, IndicatorType.MOVE_AREA, sPathfinder.area(tile, getMovement(unit)))
            }
            sIndicator.show(unit, IndicatorType.MOVE_AREA)
        }
    }

    private fun hideMoveArea(unit: Int) {
        sIndicator.hide(unit, IndicatorType.MOVE_AREA)
        sIndicator.hide(unit, IndicatorType.MOVE_PATH)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val mouseCoord = sTile.mouseCoord ?: return false

        var unit = getUnit(mouseCoord)
        if (unit >= 0 && button == 0) {
            when (mUnit[unit].faction) {
                Faction.NONE -> {
                }
                Faction.PLAYER -> {
                    if (mUnit[unit].hasTurn) {
                        showMoveArea(unit)
                        selectedUnit = unit
                        sViewManage.get<SkillBarView>()?.setAp(mUnit[unit].ap)
                    }
                }
                Faction.ENEMY -> {
                    if (mUnit[unit].hasTurn) {
                        showMoveArea(unit)
                        selectedUnit = unit
                        sViewManage.get<SkillBarView>()?.setAp(mUnit[unit].ap)
                    }
                }
            }
            return true
        } else if (selectedUnit >= 0) {
            when (button) {
                0 -> {
                    unit = selectedUnit
                    val path = sIndicator.getPathTo(sTile[mouseCoord], unit)
                    if (path != null) {
                        sIndicator.remove(unit, IndicatorType.MOVE_AREA)
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
        sViewManage.get<SkillBarView>()?.hideAp()
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
        val mouseCoord = sTile.mouseCoord
        if (lastCoord != mouseCoord) {
            if (selectedUnit >= 0) {
                sIndicator.hide(selectedUnit, IndicatorType.MOVE_PATH)
                if (mouseCoord != null) {
                    val cost = sIndicator.showPathTo(sTile[mouseCoord], selectedUnit)
                    if (cost >= 0) {
                        sViewManage.get<SkillBarView>()?.setAp(
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
            sCombatTurn.nextTurn()
        }
    }
}

package net.natruid.jungle.systems

import com.artemis.BaseSystem
import com.artemis.ComponentMapper
import com.artemis.managers.TagManager
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Align
import net.natruid.jungle.components.*
import net.natruid.jungle.core.Marsh
import net.natruid.jungle.utils.*
import java.util.*

class UnitManagementSystem : BaseSystem(), InputProcessor {
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
    private lateinit var sTag: TagManager
    private var selectedUnit
        get() = sTag.getEntityId("SELECTED_UNIT")
        set(value) {
            if (value >= 0) sTag.register("SELECTED_UNIT", value)
            else sTag.unregister("SELECTED_UNIT")
        }

    fun clean() {
        selectedUnit = -1
    }

    override fun dispose() {
        clean()
        super.dispose()
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
            this.coord = coord
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
        return entityId
    }

    fun moveUnit(unit: Int, goal: Point, path: Deque<Int>) {
        if (!sTile.isCoordValid(goal)) return
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
        mTile[sTile[cUnit.coord]]!!.unit = -1
        cUnit.coord = mTile[dest].coord
        mTile[dest]!!.unit = unit
    }

    fun freeMoveUnit(unit: Int, goal: Point) {
        val cUnit = mUnit[unit] ?: return
        val path = sPathfinder.path(sTile[cUnit.coord], sTile[goal]) ?: return
        moveUnit(unit, goal, path)
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

    private fun showMoveArea(unit: Int) {
        if (unit < 0) return

        val cUnit = mUnit[unit]
        val tile = sTile[cUnit.coord]
        if (tile >= 0) {
            hideMoveArea(unit)
            if (!sIndicator.hasResult(unit, IndicatorType.MOVE_AREA)) {
                calculateStats(unit)
                val movement = mStats[unit].speed / 100f * 4
                sIndicator.addResult(unit, IndicatorType.MOVE_AREA, sPathfinder.area(tile, movement))
            }
            sIndicator.show(unit, IndicatorType.MOVE_AREA)
        }
    }

    private fun hideMoveArea(unit: Int) {
        sIndicator.hide(unit, IndicatorType.MOVE_AREA)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val mouseCoord = sTile.mouseCoord ?: return false

        var unit = getUnit(mouseCoord)
        if (unit >= 0) {
            when (mUnit[unit].faction) {
                Faction.NONE -> {
                }
                Faction.PLAYER -> {
                    showMoveArea(unit)
                    selectedUnit = unit
                }
                Faction.ENEMY -> {
                }
            }
            return true
        } else if (selectedUnit >= 0) {
            unit = selectedUnit
            val path = sIndicator.getPathTo(mouseCoord, unit)
            hideMoveArea(unit)
            if (path != null) {
                moveUnit(unit, mouseCoord, path)
                sIndicator.remove(unit, IndicatorType.MOVE_AREA)
            } else {
                sIndicator.remove(unit, IndicatorType.MOVE_AREA)
                freeMoveUnit(unit, mouseCoord)
            }
            selectedUnit = -1
            return true
        }

        return false
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
                if (mouseCoord != null)
                    sIndicator.showPathTo(mouseCoord, selectedUnit)
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

    override fun processSystem() {}
}

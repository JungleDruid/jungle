package net.natruid.jungle.systems

import com.artemis.Aspect
import com.artemis.ComponentMapper
import com.artemis.managers.TagManager
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import net.natruid.jungle.components.*
import net.natruid.jungle.components.IndicatorComponent.IndicatorType
import net.natruid.jungle.utils.Point
import java.util.*

class UnitManagementSystem : SortedIteratingSystem(
    Aspect.all(TransformComponent::class.java, UnitComponent::class.java)
), InputProcessor {
    override val comparator: Comparator<in Int>
        get() = Comparator { p0, p1 ->
            val f0 = mUnit[p0].faction.value
            val f1 = mUnit[p1].faction.value
            when {
                f0 < f1 -> 1
                f0 > f1 -> -1
                else -> 0
            }
        }

    private lateinit var mUnit: ComponentMapper<UnitComponent>
    private lateinit var mTransform: ComponentMapper<TransformComponent>
    private lateinit var mCircle: ComponentMapper<CircleComponent>
    private lateinit var mPathFollower: ComponentMapper<PathFollowerComponent>
    private lateinit var mTile: ComponentMapper<TileComponent>
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
        speed: Float = 0f,
        faction: UnitComponent.Faction = UnitComponent.Faction.NONE
    ): Int {
        val tile = sTile[coord]
        assert(tile >= 0)
        val entityId = world.create()
        mTransform.create(entityId).position = mTransform[tile].position
        mUnit.create(entityId).apply {
            this.coord = coord
            this.speed = speed
            this.faction = faction
        }
        mCircle.create(entityId).apply {
            radius = TileSystem.tileSize / 2f - 10f
            color = when (faction) {
                UnitComponent.Faction.NONE -> Color.GRAY
                UnitComponent.Faction.PLAYER -> Color.CYAN
                UnitComponent.Faction.ENEMY -> Color.RED
            }
            type = ShapeRenderer.ShapeType.Filled
        }
        mTile[tile].unit = entityId
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

    private fun showMoveArea(unit: Int) {
        if (unit < 0) return

        val cUnit = mUnit[unit]
        val tile = sTile[cUnit.coord]
        if (tile >= 0) {
            hideMoveArea(unit)
            if (!sIndicator.hasResult(unit, IndicatorType.MOVE_AREA)) {
                sIndicator.addResult(unit, IndicatorType.MOVE_AREA, sPathfinder.area(tile, cUnit.speed))
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
                UnitComponent.Faction.NONE -> {
                }
                UnitComponent.Faction.PLAYER -> {
                    showMoveArea(unit)
                    selectedUnit = unit
                }
                UnitComponent.Faction.ENEMY -> {
                }
            }
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

    override fun process(entityId: Int) {}
}

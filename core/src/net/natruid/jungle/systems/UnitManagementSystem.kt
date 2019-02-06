package net.natruid.jungle.systems

import com.artemis.Aspect
import com.artemis.ComponentMapper
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import net.natruid.jungle.components.CircleComponent
import net.natruid.jungle.components.PathFollowerComponent
import net.natruid.jungle.components.TransformComponent
import net.natruid.jungle.components.UnitComponent
import net.natruid.jungle.utils.AreaIndicator
import net.natruid.jungle.utils.Pathfinder
import net.natruid.jungle.utils.Point
import net.natruid.jungle.utils.extensions.first
import net.natruid.jungle.utils.extensions.firstObject
import kotlin.collections.set

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
    private lateinit var tiles: TileSystem
    private val areaIndicators = HashMap<UnitComponent, AreaIndicator>()
    private var currentMoveArea: AreaIndicator? = null
    private var selectedUnit: UnitComponent? = null

    fun clean() {
        areaIndicators.values.forEach {
            it.clear()
        }
        areaIndicators.clear()
        currentMoveArea = null
    }

    override fun dispose() {
        clean()
        super.dispose()
    }

    fun getUnit(coord: Point): UnitComponent? {
        if (!tiles.isCoordValid(coord)) return null

        return entityIds.firstObject {
            val unit = mUnit[it]
            if (unit.coord == coord) unit else null
        }
    }

    fun getUnitEntity(unit: UnitComponent): Int {
        return entityIds.first {
            val u = mUnit[it]
            u == unit
        } ?: -1
    }

    fun addUnit(
        coord: Point = Point(),
        speed: Float = 0f,
        faction: UnitComponent.Faction = UnitComponent.Faction.NONE
    ): Int {
        val tile = tiles[coord]!!
        val entityId = world.create()
        mTransform.create(entityId).position = tiles.getPosition(tile)!!
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
        return entityId
    }

    fun moveUnit(unit: UnitComponent, goal: Point) {
        if (!tiles.isCoordValid(goal)) return
        val path = Pathfinder.path(tiles, tiles[unit.coord]!!, tiles[goal]!!) ?: return
        val entity = getUnitEntity(unit)
        if (entity < 0) return
        mPathFollower.create(entity).path = path
        unit.coord = goal
    }

    private fun showMoveArea(unit: UnitComponent) {
        val tile = tiles[unit.coord]
        if (tile != null) {
            hideMoveArea()
            var areaIndicator = areaIndicators[unit]
            if (areaIndicator == null) {
                areaIndicator = AreaIndicator(world, Pathfinder.area(tiles, tile, unit.speed))
                areaIndicators[unit] = areaIndicator
            }
            areaIndicator.show()
            currentMoveArea = areaIndicator
        }
    }

    private fun hideMoveArea() {
        currentMoveArea?.apply {
            clearPath()
            hide()
            currentMoveArea = null
        }
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val mouseCoord = tiles.mouseCoord ?: return false

        val unit = getUnit(mouseCoord)
        if (unit != null) {
            when (unit.faction) {
                UnitComponent.Faction.NONE -> {
                }
                UnitComponent.Faction.PLAYER -> {
                    showMoveArea(unit)
                    selectedUnit = unit
                }
                UnitComponent.Faction.ENEMY -> {
                }
            }
        } else {
            val path = currentMoveArea?.getPathTo(mouseCoord)
            hideMoveArea()
            if (path != null) {
                val u = selectedUnit!!
                val entityId = getUnitEntity(u)
                mPathFollower.create(entityId).path = path
                val dest = path[path.size - 1]
                u.coord = dest.coord
                areaIndicators.remove(u)!!.clear()
                selectedUnit = null
            } else if (selectedUnit != null) {
                val u = selectedUnit!!
                areaIndicators.remove(u)?.clear()
                moveUnit(u, mouseCoord)
                selectedUnit = null
            }
        }

        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    private var lastCoord: Point? = null
    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        val mouseCoord = tiles.mouseCoord
        if (lastCoord != mouseCoord) {
            currentMoveArea?.clearPath()
            if (mouseCoord != null) currentMoveArea?.showPathTo(mouseCoord)
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

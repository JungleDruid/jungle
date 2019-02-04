package net.natruid.jungle.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import net.natruid.jungle.components.CircleComponent
import net.natruid.jungle.components.PathFollowerComponent
import net.natruid.jungle.components.TransformComponent
import net.natruid.jungle.components.UnitComponent
import net.natruid.jungle.utils.AreaIndicator
import net.natruid.jungle.utils.Pathfinder
import net.natruid.jungle.utils.Point
import kotlin.collections.set

class UnitManagementSystem : SortedIteratingSystem(
        allOf(TransformComponent::class, UnitComponent::class).get(),
        FactionComparator()
), InputProcessor {
    class FactionComparator : Comparator<Entity> {
        private val unitMapper = mapperFor<UnitComponent>()
        override fun compare(p0: Entity?, p1: Entity?): Int {
            val f0 = unitMapper[p0].faction.value
            val f1 = unitMapper[p1].faction.value
            return when {
                f0 < f1 -> 1
                f0 > f1 -> -1
                else -> 0
            }
        }
    }

    private val unitMapper = mapperFor<UnitComponent>()
    private var tiles: TileSystem? = null
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

    override fun addedToEngine(engine: Engine?) {
        tiles = engine!!.getSystem(TileSystem::class.java)
        if (tiles == null) {
            error("[Error] UnitManagementSystem must be added after TileSystem.")
        }

        super.addedToEngine(engine)
    }

    override fun removedFromEngine(engine: Engine?) {
        clean()
        super.removedFromEngine(engine)
    }

    override fun processEntity(entity: Entity?, deltaTime: Float) {}

    fun getUnit(coord: Point): UnitComponent? {
        if (!tiles!!.isCoordValid(coord)) return null

        for (entity in entities) {
            val unit = unitMapper[entity]
            if (unit.coord == coord) return unit
        }

        return null
    }

    fun getUnitEntity(unit: UnitComponent): Entity? {
        for (entity in entities) {
            val u = unitMapper[entity]
            if (u == unit) return entity
        }
        return null
    }

    fun addUnit(unit: UnitComponent): Entity {
        val tile = tiles!![unit.coord]!!
        val entity = Entity()
        entity.add(TransformComponent(tiles!!.getPosition(tile)!!))
        entity.add(unit)
        entity.add(CircleComponent(
                TileSystem.tileSize / 2f - 10f,
                when (unit.faction) {
                    UnitComponent.Faction.NONE -> Color.GRAY
                    UnitComponent.Faction.PLAYER -> Color.CYAN
                    UnitComponent.Faction.ENEMY -> Color.RED
                },
                ShapeRenderer.ShapeType.Filled
        ))
        engine.addEntity(entity)
        return entity
    }

    fun moveUnit(unit: UnitComponent, goal: Point) {
        if (!tiles!!.isCoordValid(goal)) return
        val path = Pathfinder.path(tiles!!, tiles!![unit.coord]!!, tiles!![goal]!!) ?: return
        val entity = getUnitEntity(unit) ?: return
        entity.add(PathFollowerComponent(path, tiles!!))
        unit.coord = goal
    }

    private fun showMoveArea(unit: UnitComponent) {
        val tile = tiles!![unit.coord]
        if (tile != null) {
            hideMoveArea()
            var areaIndicator = areaIndicators[unit]
            if (areaIndicator == null) {
                areaIndicator = AreaIndicator(engine, Pathfinder.area(tiles!!, tile, unit.speed))
                areaIndicators[unit] = areaIndicator
            }
            areaIndicator.show()
            currentMoveArea = areaIndicator
        }
    }

    private fun hideMoveArea() {
        currentMoveArea?.let {
            it.clearPath()
            it.hide()
            currentMoveArea = null
        }
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val mouseCoord = tiles!!.mouseCoord ?: return false

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
                val entity = getUnitEntity(u)!!
                val pathFollower = PathFollowerComponent(path, tiles!!)
                entity.add(pathFollower)
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
        val mouseCoord = tiles!!.mouseCoord
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
}
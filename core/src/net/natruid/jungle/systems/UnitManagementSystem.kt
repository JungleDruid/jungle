package net.natruid.jungle.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.ObjectMap
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import ktx.collections.set
import net.natruid.jungle.components.CircleComponent
import net.natruid.jungle.components.TransformComponent
import net.natruid.jungle.components.UnitComponent
import net.natruid.jungle.utils.AreaIndicator
import net.natruid.jungle.utils.Pathfinder
import net.natruid.jungle.utils.Point

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
    private val areaIndicators = ObjectMap<UnitComponent, AreaIndicator>()
    private var currentMoveArea: AreaIndicator? = null
    private var selectedUnit: UnitComponent? = null

    fun clean() {
        areaIndicators.values().forEach {
            it.free()
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

    fun getUnit(x: Int, y: Int): UnitComponent? {
        if (!tiles!!.isCoordValid(x, y)) return null

        for (entity in entities) {
            val unit = unitMapper[entity]
            if (unit.x == x && unit.y == y) return unit
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
        val tile = tiles!![unit.x, unit.y]!!
        val entity = engine.createEntity()
        engine.createComponent(TransformComponent::class.java).let {
            it.position.set(tiles!!.getPosition(tile))
            entity.add(it)
        }
        entity.add(unit)
        engine.createComponent(CircleComponent::class.java).let {
            it.radius = TileSystem.tileSize / 2f - 10f
            it.type = ShapeRenderer.ShapeType.Filled
            when (unit.faction) {
                UnitComponent.Faction.NONE -> it.color.set(Color.GRAY)
                UnitComponent.Faction.PLAYER -> it.color.set(Color.BLUE)
                UnitComponent.Faction.ENEMY -> it.color.set(Color.RED)
            }
            entity.add(it)
        }
        engine.addEntity(entity)
        return entity
    }

    private fun showMoveArea(unit: UnitComponent) {
        val tile = tiles!![unit.x, unit.y]
        if (tile != null) {
            hideMoveArea()
            var pathIndicator = areaIndicators[unit]
            if (pathIndicator == null) {
                pathIndicator = AreaIndicator.obtain(engine, Pathfinder.area(tiles!!, tile, unit.speed))
                areaIndicators[unit] = pathIndicator
            }
            pathIndicator.show()
            currentMoveArea = pathIndicator
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
        val mouseCoord = tiles!!.mouseCoord
        if (!mouseCoord.hasValue) return false

        val unit = getUnit(mouseCoord.x, mouseCoord.y)
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
            val path = currentMoveArea?.getPathTo(mouseCoord.x, mouseCoord.y)
            hideMoveArea()
            if (path != null) {
                val u = selectedUnit!!
                val entity = getUnitEntity(u)!!
                val dest = path[path.size - 1]
                entity.getComponent(UnitComponent::class.java).let {
                    it.x = dest.x
                    it.y = dest.y
                }
                entity.getComponent(TransformComponent::class.java).position.set(tiles!!.getPosition(dest))
                areaIndicators.remove(u).free()
            }
            selectedUnit = null
        }

        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    private val lastCoord = Point()
    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        val mouseCoord = tiles!!.mouseCoord
        if (lastCoord != mouseCoord) {
            currentMoveArea?.clearPath()
            currentMoveArea?.showPathTo(mouseCoord.x, mouseCoord.y)
            lastCoord.set(mouseCoord)
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
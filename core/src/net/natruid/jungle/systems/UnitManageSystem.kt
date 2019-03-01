package net.natruid.jungle.systems

import com.artemis.Aspect
import com.artemis.ComponentMapper
import com.artemis.annotations.EntityId
import com.artemis.managers.TagManager
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Align
import net.mostlyoriginal.api.event.common.EventSystem
import net.mostlyoriginal.api.event.common.Subscribe
import net.natruid.jungle.components.*
import net.natruid.jungle.components.render.PosComponent
import net.natruid.jungle.components.render.RenderComponent
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.core.Marsh
import net.natruid.jungle.events.UnitAttackEvent
import net.natruid.jungle.events.UnitHealthChangedEvent
import net.natruid.jungle.events.UnitMoveEvent
import net.natruid.jungle.systems.abstracts.SortedIteratingSystem
import net.natruid.jungle.utils.*
import net.natruid.jungle.utils.ai.BehaviorTree
import net.natruid.jungle.utils.ai.SequenceSelector
import net.natruid.jungle.utils.ai.actions.AttackAction
import net.natruid.jungle.utils.ai.actions.MoveTowardUnitAction
import net.natruid.jungle.utils.ai.conditions.HasUnitInAttackRangeCondition
import net.natruid.jungle.utils.ai.conditions.SimpleUnitTargeter
import net.natruid.jungle.utils.extensions.dispatch
import net.natruid.jungle.views.SkillBarView
import kotlin.math.ceil

class UnitManageSystem : SortedIteratingSystem(
    Aspect.all(PosComponent::class.java, UnitComponent::class.java)
), InputProcessor {
    override val comparator = FactionComparator()

    private lateinit var mUnit: ComponentMapper<UnitComponent>
    private lateinit var mRender: ComponentMapper<RenderComponent>
    private lateinit var mPos: ComponentMapper<PosComponent>
    private lateinit var mPathFollower: ComponentMapper<PathFollowerComponent>
    private lateinit var mTile: ComponentMapper<TileComponent>
    private lateinit var mLabel: ComponentMapper<LabelComponent>
    private lateinit var mStats: ComponentMapper<StatsComponent>
    private lateinit var mAttributes: ComponentMapper<AttributesComponent>
    private lateinit var mBehavior: ComponentMapper<BehaviorComponent>
    private lateinit var mAnimation: ComponentMapper<AnimationComponent>
    private lateinit var es: EventSystem
    private lateinit var tileSystem: TileSystem
    private lateinit var pathfinderSystem: PathfinderSystem
    private lateinit var indicateSystem: IndicateSystem
    private lateinit var combatTurnSystem: CombatTurnSystem
    private lateinit var viewManageSystem: ViewManageSystem
    private lateinit var animateSystem: AnimateSystem
    private lateinit var tagManager: TagManager

    private var unitsHasTurn = -1
    @EntityId
    private var selectedUnit = -1
    @EntityId
    private var nextSelect = -1

    fun reset() {
        selectedUnit = -1
        unitsHasTurn = -1
    }

    fun getUnit(coord: Point): Int {
        return mTile[tileSystem[coord]]?.unit ?: -1
    }

    fun addUnit(
        x: Int,
        y: Int,
        faction: Faction = Faction.NONE
    ): Int {
        val tile = tileSystem[x, y]
        assert(tile >= 0)
        if (mTile[tile].unit >= 0) return -1
        val entityId = world.create()
        mRender.create(entityId).z = Constants.Z_UNIT
        mPos.create(entityId).apply {
            set(mPos[tile])
        }
        mUnit.create(entityId).apply {
            this.tile = tile
            this.faction = faction
            if (faction == Faction.PLAYER)
                this.level = 100
            else
                this.level = 5
        }
        mAttributes.create(entityId).apply {
            if (faction == Faction.PLAYER)
                base.fill(16)
            else
                base.fill(6)
        }
        mStats.create(entityId)
        mLabel.create(entityId).apply {
            fontName = "huge"
            color.set(when (faction) {
                Faction.NONE -> Color.GRAY
                Faction.PLAYER -> Color.GREEN
                Faction.ENEMY -> Color.RED
            })
            text = when (faction) {
                Faction.NONE -> "？"
                Faction.PLAYER -> "Ｎ"
                Faction.ENEMY -> "Ｄ"
            }
            align = Align.center
        }
        if (faction != Faction.PLAYER) {
            mBehavior.create(entityId).tree = BehaviorTree().apply {
                name = "Move Close"
                addBehaviors(
                    SequenceSelector().apply {
                        name = "attack"
                        addBehaviors(
                            HasUnitInAttackRangeCondition(true),
                            AttackAction()
                        )
                    },
                    SequenceSelector().apply {
                        addBehaviors(
                            SimpleUnitTargeter(UnitTargetType.HOSTILE, UnitCondition.CLOSE),
                            MoveTowardUnitAction()
                        )
                    }
                )
            }
        } else {
            tagManager.register("PLAYER", entityId)
        }
        mTile[tile].unit = entityId
        calculateStats(entityId)
        mUnit[entityId].hp = mStats[entityId].hp
        combatTurnSystem.addFaction(faction)
        return entityId
    }

    @Subscribe
    fun unitMoveListener(event: UnitMoveEvent) {
        event.let { e ->
            if (e.path?.let { moveUnit(e.unit, it, e.free) } == null) {
                if (e.free)
                    freeMoveUnit(e.unit, e.targetTile)
                else
                    moveUnit(e.unit, e.targetTile)
            }
        }
    }

    private fun moveUnit(unit: Int, tile: Int): Boolean {
        val cUnit = mUnit[unit]
        val area = pathfinderSystem.area(cUnit.tile, getMovement(unit))
        val path = pathfinderSystem.extractPath(area, tile) ?: return false
        moveUnit(unit, path)
        return true
    }

    private fun moveUnit(unit: Int, path: Path, free: Boolean = false, callback: (() -> Unit)? = null) {
        if (unit < 0) return
        val dest = path.peekLast()
        val cUnit = mUnit[unit]
        useAp(unit, if (free) 0 else getMovementCost(unit, dest.cost)) {
            mPathFollower.create(unit).apply {
                if (this.path == null) {
                    this.path = path
                } else {
                    this.path!!.addAll(path)
                }
                this.callback = callback
            }
            mTile[cUnit.tile]!!.unit = -1
            cUnit.tile = dest.tile
            mTile[dest.tile]!!.unit = unit
        }
    }

    fun isBusy(unit: Int): Boolean {
        if (unit < 0) return false
        return mAnimation.has(unit) || mPathFollower.has(unit)
    }

    private fun freeMoveUnit(unit: Int, tile: Int) {
        val cUnit = mUnit[unit] ?: return
        val path = pathfinderSystem.path(cUnit.tile, tile) ?: return
        moveUnit(unit, path, true)
    }

    fun getMoveAndAttackPath(unit: Int, target: Int): Path? {
        val tile1 = mUnit[unit].tile
        val tile2 = mUnit[target].tile
        val movement = getMovement(unit, 2)
        if (tileSystem.getDistance(tile1, tile2) > movement + 1f) return null
        val area = pathfinderSystem.area(tile1, movement)
        return pathfinderSystem.extractPath(
            area = area,
            goal = tile2,
            unit = unit,
            maxCost = movement,
            type = ExtractPathType.LOWEST_COST,
            inRange = 1f
        ) ?: return null
    }

    @Subscribe
    fun attackListener(event: UnitAttackEvent) {
        event.let {
            val attackPath = it.path ?: getMoveAndAttackPath(it.unit, it.target)
            if (attackPath != null) {
                val unit = it.unit
                val target = it.target
                moveUnit(
                    it.unit,
                    attackPath,
                    callback = {
                        attack(unit, target)
                    }
                )
            }
        }
    }

    private fun attack(unit: Int, target: Int): Boolean {
        return useAp(unit, 2) {
            mAnimation.create(unit).let {
                it.target = target
                it.type = AnimationType.ATTACK
                it.callback = { damage(unit, target, getDamage(unit, target)) }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun getDamage(unit: Int, target: Int): Int {
        return (10f * mStats[unit].damage).toInt()
    }

    private fun damage(unit: Int, target: Int, amount: Int) {
        Logger.debug { "$unit deals $amount damage to $target" }
        var killed = false
        mUnit[target].let {
            it.hp -= amount
            if (it.hp <= 0) {
                kill(unit, target)
                killed = true
            }
        }

        es.dispatch(UnitHealthChangedEvent::class).let {
            it.unit = target
            it.source = unit
            it.amount = -amount
            it.killed = killed
        }
    }

    private fun kill(unit: Int, target: Int) {
        Logger.debug { "$unit kills $target" }
        val cUnit = mUnit[target]
        val faction = cUnit.faction
        val last = sortedEntityIds.count { mUnit[it].faction == faction } <= 1
        mTile[cUnit.tile].unit = -1
        world.delete(target)
        if (last) combatTurnSystem.removeFaction(faction)
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
        indicateSystem.remove(unit, IndicatorType.MOVE_AREA)
        if (cUnit.ap <= 0) {
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
        val cUnit = mUnit[unit]
        if (!cUnit.hasTurn) return
        cUnit.hasTurn = false
        unitsHasTurn -= 1
        if (selectedUnit == unit) {
            deselectUnit()
        }
    }

    fun getMovement(unit: Int, preservedAp: Int = 0): Float {
        val cUnit = mUnit[unit]
        return mStats[unit].speed * (cUnit.ap - preservedAp) + cUnit.extraMovement
    }

    fun hasEnemy(unit: Int): Boolean {
        val faction = mUnit[unit].faction
        for (id in sortedEntityIds) {
            if (mUnit[id].faction != faction) return true
        }

        return false
    }

    fun getUnits(): List<Int> {
        return sortedEntityIds.toList()
    }

    fun getAllies(faction: Faction): List<Int> {
        val list = ArrayList<Int>()
        for (id in sortedEntityIds) {
            if (mUnit[id].faction == faction) list.add(id)
        }
        return list
    }

    fun getEnemies(faction: Faction): List<Int> {
        val list = ArrayList<Int>()
        for (id in sortedEntityIds) {
            if (mUnit[id].faction != faction) list.add(id)
        }
        return list
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
                    if (selectedUnit != unit && mUnit[unit].hasTurn) {
                        select(unit)
                    }
                }
                Faction.ENEMY -> {
                    if (selectedUnit >= 0) {
                        hideMoveArea(selectedUnit)
                        es.dispatch(UnitAttackEvent::class).let {
                            it.unit = selectedUnit
                            it.target = unit
                        }
                        deselectUnit(selectedUnit)
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
                        es.dispatch(UnitMoveEvent::class).let {
                            it.unit = unit
                            it.path = path
                        }
                        deselectUnit(unit)
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

    private fun select(unit: Int) {
        showMoveArea(unit)
        selectedUnit = unit
        viewManageSystem.get<SkillBarView>()?.setAp(mUnit[unit].ap)
    }

    private fun deselectUnit(next: Int = -1) {
        if (selectedUnit < 0) return
        hideMoveArea(selectedUnit)
        selectedUnit = -1
        viewManageSystem.get<SkillBarView>()?.hideAp()
        nextSelect = next
    }

    fun getMovementCost(unit: Int, cost: Float, preview: Boolean = false): Int {
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
            if (mouseCoord != null) {
                val tile = tileSystem[mouseCoord]
                Jungle.instance.debugView?.unitLabel?.setText("Unit: ${mTile[tile].unit}")
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
        if (keycode == Keys.E && selectedUnit >= 0) {
            indicateSystem.remove(selectedUnit, IndicatorType.MOVE_AREA)
            endTurn(selectedUnit)
            return true
        }
        return false
    }

    override fun process(entityId: Int) {
        if (unitsHasTurn == 0) {
            if (animateSystem.ready) combatTurnSystem.nextTurn()
        } else if (nextSelect >= 0) {
            if (mUnit[nextSelect].hasTurn) {
                if (!mPathFollower.has(nextSelect) && !mAnimation.has(nextSelect)) {
                    select(nextSelect)
                    nextSelect = -1
                }
            } else {
                nextSelect = -1
            }
        }
    }
}

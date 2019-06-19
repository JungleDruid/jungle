package net.natruid.jungle.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.EntityId;
import com.artemis.managers.TagManager;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisWindow;
import net.mostlyoriginal.api.event.common.EventSystem;
import net.mostlyoriginal.api.event.common.Subscribe;
import net.natruid.jungle.components.*;
import net.natruid.jungle.components.render.PosComponent;
import net.natruid.jungle.components.render.RenderComponent;
import net.natruid.jungle.core.Sky;
import net.natruid.jungle.data.Marsh;
import net.natruid.jungle.data.StatDef;
import net.natruid.jungle.events.UnitHealthChangedEvent;
import net.natruid.jungle.events.UnitMoveEvent;
import net.natruid.jungle.events.UnitSkillEvent;
import net.natruid.jungle.systems.abstracts.SortedIteratingSystem;
import net.natruid.jungle.utils.*;
import net.natruid.jungle.utils.ai.BehaviorTree;
import net.natruid.jungle.utils.ai.SequenceSelector;
import net.natruid.jungle.utils.ai.actions.AttackAction;
import net.natruid.jungle.utils.ai.actions.MoveTowardUnitAction;
import net.natruid.jungle.utils.ai.conditions.HasUnitInAttackRangeCondition;
import net.natruid.jungle.utils.ai.conditions.SimpleUnitTargeter;
import net.natruid.jungle.utils.skill.Effect;
import net.natruid.jungle.utils.skill.ModdedValue;
import net.natruid.jungle.utils.skill.Skill;
import net.natruid.jungle.utils.types.*;
import net.natruid.jungle.views.DebugView;
import net.natruid.jungle.views.SkillBarView;
import net.natruid.jungle.views.UnitStatsQuickview;

import java.util.*;

import static net.natruid.jungle.utils.types.Faction.PLAYER;

public class UnitManageSystem extends SortedIteratingSystem implements InputProcessor {
    private final float[] statMultipliers = new float[StatType.length];
    private final Vector3 pos = new Vector3();
    private ComponentMapper<UnitComponent> mUnit;
    private ComponentMapper<RenderComponent> mRender;
    private ComponentMapper<PosComponent> mPos;
    private ComponentMapper<PathFollowerComponent> mPathFollower;
    private ComponentMapper<TileComponent> mTile;
    private ComponentMapper<LabelComponent> mLabel;
    private ComponentMapper<StatsComponent> mStats;
    private ComponentMapper<AttributesComponent> mAttributes;
    private ComponentMapper<BehaviorComponent> mBehavior;
    private ComponentMapper<AnimationComponent> mAnimation;
    private ComponentMapper<TurnComponent> mTurn;
    private EventSystem es;
    private TileSystem tileSystem;
    private PathfinderSystem pathfinderSystem;
    private IndicateSystem indicateSystem;
    private CombatTurnSystem combatTurnSystem;
    private ViewManageSystem viewManageSystem;
    private CameraSystem cameraSystem;
    private TagManager tagManager;
    @EntityId
    private int selectedUnit = -1;
    @EntityId
    private int nextSelect = -1;
    private Point lastCoord = null;

    public UnitManageSystem() {
        super(Aspect.all(PosComponent.class, UnitComponent.class));
    }

    @Override
    public Comparator<Integer> getComparator() {
        return FactionComparator.INSTANCE;
    }

    public void reset() {
        selectedUnit = -1;
        nextSelect = -1;
    }

    public int getUnit(Point coord) {
        TileComponent tile = mTile.get(tileSystem.get(coord));
        if (tile == null) return -1;
        return tile.unit;
    }

    public int addUnit(int x, int y, Faction faction) {
        int tile = tileSystem.get(x, y);
        assert tile >= 0;
        if (mTile.get(tile).unit >= 0) return -1;
        int entityId = world.create();
        mRender.create(entityId).z = Constants.Z_UNIT;
        {
            PosComponent it = mPos.create(entityId);
            it.set(mPos.get(tile));
        }
        {
            UnitComponent it = mUnit.create(entityId);
            it.tile = tile;
            it.faction = faction;
            Skill attack = Sky.marsh.getSkill("attack");
            assert attack != null;
            it.skills.add(attack);
            if (faction == PLAYER)
                it.level = 100;
            else
                it.level = 5;
        }
        {
            AttributesComponent it = mAttributes.create(entityId);
            if (faction == PLAYER)
                Arrays.fill(it.base, 16);
            else
                Arrays.fill(it.base, 16);
        }
        mStats.create(entityId);
        {
            LabelComponent it = mLabel.create(entityId);
            it.fontName = "huge";
            Color color;
            switch (faction) {
                case NONE:
                    color = Color.GRAY;
                    it.text = "？";
                    break;
                case PLAYER:
                    color = Color.GREEN;
                    it.text = "Ｎ";
                    break;
                case ENEMY:
                    color = Color.RED;
                    it.text = "Ｄ";
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + faction);
            }
            it.color.set(color);
            it.align = Align.center;
        }
        if (faction != PLAYER) {
            {
                BehaviorTree it = new BehaviorTree();
                mBehavior.create(entityId).tree = it;
                SequenceSelector attack = new SequenceSelector();
                attack.name = "attack";
                attack.addBehaviors(
                    new HasUnitInAttackRangeCondition(true),
                    new AttackAction()
                );
                SequenceSelector moveClose = new SequenceSelector();
                moveClose.name = "move close";
                moveClose.addBehaviors(
                    new SimpleUnitTargeter(UnitTargetType.HOSTILE, UnitCondition.CLOSE),
                    new MoveTowardUnitAction()
                );
                it.addBehaviors(attack, moveClose);
            }
        } else {
            tagManager.register("PLAYER", entityId);
        }
        mTile.get(tile).unit = entityId;
        calculateStats(entityId);
        mUnit.get(entityId).hp = mStats.get(entityId).getHp();
        combatTurnSystem.addFaction(faction);
        return entityId;
    }

    @Subscribe
    public void unitMoveListener(UnitMoveEvent e) {
        if (e.path != null) {
            moveUnit(e.unit, e.path, e.free);
        } else if (e.free) {
            freeMoveUnit(e.unit, e.targetTile);
        } else {
            moveUnit(e.unit, e.targetTile);
        }
    }

    private boolean moveUnit(int unit, int tile) {
        UnitComponent cUnit = mUnit.get(unit);
        PathNode[] area = pathfinderSystem.area(cUnit.tile, getMovement(unit), true);
        Deque<PathNode> path = pathfinderSystem.extractPath(area, tile);
        moveUnit(unit, path);
        return true;
    }

    public float getMovement(int unit) {
        return getMovement(unit, 0);
    }

    public float getMovement(int unit, int preservedAp) {
        UnitComponent cUnit = mUnit.get(unit);
        return mStats.get(unit).getSpeed() * (cUnit.ap - preservedAp) + cUnit.extraMovement;
    }

    public boolean hasAp(int unit, int ap) {
        return mUnit.get(unit).ap >= ap;
    }

    public boolean removeAp(int unit, int ap) {
        if (ap <= 0) return false;
        UnitComponent cUnit = mUnit.get(unit);
        cUnit.ap -= ap;
        viewManageSystem.get(SkillBarView.class).setAp(cUnit.ap);
        indicateSystem.remove(unit, IndicatorType.MOVE_AREA);
        if (cUnit.ap <= 0) {
            combatTurnSystem.endTurn(unit);
            return true;
        }
        return false;
    }

    public int getMovementCost(int unit, float cost) {
        return getMovementCost(unit, cost, false);
    }

    public int getMovementCost(int unit, float cost, boolean preview) {
        UnitComponent cUnit = mUnit.get(unit);
        int apCost = 0;
        if (cost > cUnit.extraMovement) {
            float newCost = cost - cUnit.extraMovement;
            float movementPerAp = mStats.get(unit).getSpeed();
            apCost = (int) Math.ceil(newCost / movementPerAp);
            if (!preview) cUnit.extraMovement = movementPerAp * apCost - newCost;
        } else if (!preview) {
            cUnit.extraMovement -= cost;
        }

        return apCost;
    }

    public void moveUnit(int unit, Deque<PathNode> path, boolean free) {
        moveUnit(unit, path, free, null);
    }

    public void moveUnit(int unit, Deque<PathNode> path) {
        moveUnit(unit, path, false, null);
    }

    public void moveUnit(int unit, Deque<PathNode> path, Runnable callback) {
        moveUnit(unit, path, false, callback);
    }

    public void moveUnit(int unit, Deque<PathNode> path, boolean free, Runnable callback) {
        if (unit < 0) return;
        PathNode dest = path.peekLast();
        UnitComponent cUnit = mUnit.get(unit);
        int apCost = free ? 0 : getMovementCost(unit, dest.cost);
        if (hasAp(unit, apCost)) {
            {
                PathFollowerComponent it = mPathFollower.create(unit);
                if (it.path == null) {
                    it.path = path;
                } else {
                    it.path.addAll(path);
                }
                it.callback = callback;
            }
            mTile.get(cUnit.tile).unit = -1;
            cUnit.tile = dest.tile;
            mTile.get(dest.tile).unit = unit;
            removeAp(unit, apCost);
        }
    }

    private void freeMoveUnit(int unit, int tile) {
        UnitComponent cUnit = mUnit.get(unit);
        if (cUnit == null) return;
        Deque<PathNode> path = pathfinderSystem.path(cUnit.tile, tile, true);
        if (path == null) return;
        moveUnit(unit, path, true);
    }

    public Deque<PathNode> getMoveAndActPath(int unit, int target, int ap, float range) {
        int tile1 = mUnit.get(unit).tile;
        int tile2 = mUnit.get(target).tile;
        float movement = getMovement(unit, ap);
        if (tileSystem.getDistance(tile1, tile2) > movement + range) return null;
        PathNode[] area = pathfinderSystem.area(tile1, movement, true);
        return pathfinderSystem.extractPath(area, tile2, unit, ExtractPathType.LOWEST_COST, movement, range);
    }

    public boolean isBusy(int unit) {
        if (unit < 0) return false;
        return mAnimation.has(unit) || mPathFollower.has(unit);
    }

    @Subscribe
    public void skillListener(UnitSkillEvent e) {
        Skill skill = mUnit.get(e.unit).skills.get(e.skill);
        Deque<PathNode> path = e.path == null ? getMoveAndActPath(
            e.unit,
            e.target,
            skill.cost,
            getModdedValue(e.unit, skill.range)
        ) : e.path;
        if (path != null) {
            int unit = e.unit;
            int target = e.target;
            moveUnit(
                e.unit,
                path,
                () -> useSkill(unit, skill, target)
            );
        }
    }

    private void useSkill(int unit, Skill skill, int target) {
        if (hasAp(unit, skill.cost)) {
            Sky.log.debug("Unit " + unit + " used skill " + skill.name + " on " + target);
            {
                AnimationComponent it = mAnimation.create(unit);
                it.target = target;
                it.type = AnimationType.ATTACK;
                it.callback = () -> applyEffects(unit, skill, target);
            }
            removeAp(unit, skill.cost);
        }
    }

    private void applyEffects(int unit, Skill skill, int target) {
        for (Effect effect : skill.effects) {
            //noinspection SwitchStatementWithTooFewBranches
            switch (effect.type) {
                case "damage":
                    damage(
                        unit,
                        target,
                        (int) (getModdedValue(unit, effect.amount) * mStats.get(unit).getDamage())
                    );
                    break;
            }
        }
    }

    public float getModdedValue(int unit, ModdedValue moddedValue) {
        float value = moddedValue.base;
        if (moddedValue.proficiency != null) {
            Integer proLevel = mUnit.get(unit).proficiencies.get(Sky.marsh.getProficiency(moddedValue.proficiency), 0);
            value += proLevel * moddedValue.magnitude;
        }
        return value;
    }

    private void damage(int unit, int target, int amount) {
        Sky.log.debug(String.format("%d deals %d damage to %d", unit, amount, target));
        boolean killed = false;
        {
            UnitComponent it = mUnit.get(target);
            it.hp -= amount;
            if (it.hp <= 0) {
                kill(unit, target);
                killed = true;
            }
        }
        {
            UnitHealthChangedEvent it = es.dispatch(UnitHealthChangedEvent.class);
            it.unit = target;
            it.source = unit;
            it.amount = -amount;
            it.killed = killed;
        }
    }

    private void kill(int unit, int target) {
        Sky.log.debug(unit + " kills " + target);
        UnitComponent cUnit = mUnit.get(target);
        Faction faction = cUnit.faction;
        boolean last = countFactionUnits(faction) <= 1;
        mTile.get(cUnit.tile).unit = -1;
        world.delete(target);
        if (last) combatTurnSystem.removeFaction(faction);
    }

    private int countFactionUnits(Faction faction) {
        int count = 0;
        for (int entityId : getSortedEntityIds()) {
            if (mUnit.get(entityId).faction == faction) count += 1;
        }
        return count;
    }

    public void calculateStats(int entityId) {
        if (entityId < 0) return;
        StatsComponent cStats = mStats.get(entityId);
        if (cStats == null) return;
        if (!cStats.dirty) return;
        UnitComponent cUnit = mUnit.get(entityId);
        if (cUnit == null) return;
        AttributesComponent cAttr = mAttributes.get(entityId);
        if (cAttr == null) return;

        Arrays.fill(statMultipliers, 1);

        int[] stats = cStats.values;
        int[] attributes = cAttr.modified;

        // calculate modified attributes
        if (cAttr.dirty) {
            int[] base = cAttr.base;
            System.arraycopy(base, 0, attributes, 0, base.length);
            cAttr.dirty = false;
        }

        Marsh marsh = Sky.marsh;

        // calculate base stats
        for (int i = 0; i < stats.length; i++) {
            StatDef def = marsh.getStatDef(i);
            int value = def.base + def.level * cUnit.level;
            AttributeModifier[] attributeDefs = def.attributes;
            if (attributeDefs != null) {
                for (AttributeModifier it : attributeDefs) {
                    int attr = attributes[it.type.ordinal()] - 10;
                    if (it.add != 0) value += attr * it.add;
                    if (it.mul != 0f) statMultipliers[i] += attr * it.mul;
                }
            }
            stats[i] = value;
        }

        // multiply stats with multipliers
        for (int i = 0; i < stats.length; i++) {
            stats[i] = (int) (stats[i] * statMultipliers[i]);
        }

        cStats.dirty = false;
    }

    public boolean hasEnemy(int unit) {
        Faction faction = mUnit.get(unit).faction;
        for (int id : getSortedEntityIds()) {
            if (mUnit.get(id).faction != faction) return true;
        }

        return false;
    }

    public List<Integer> getUnits() {
        return getSortedEntityIds();
    }

    public List<Integer> getAllies(Faction faction) {
        ArrayList<Integer> list = new ArrayList<>();
        for (Integer id : getSortedEntityIds()) {
            if (mUnit.get(id).faction == faction) list.add(id);
        }
        return list;
    }

    public List<Integer> getEnemies(Faction faction) {
        ArrayList<Integer> list = new ArrayList<>();
        for (Integer id : getSortedEntityIds()) {
            if (mUnit.get(id).faction != faction) list.add(id);
        }
        return list;
    }

    private void showMoveArea(int unit) {
        if (unit < 0) return;

        UnitComponent cUnit = mUnit.get(unit);
        int tile = cUnit.tile;
        if (tile >= 0) {
            hideMoveArea(unit);
            if (!indicateSystem.hasResult(unit, IndicatorType.MOVE_AREA)) {
                calculateStats(unit);
                indicateSystem.addResult(unit, IndicatorType.MOVE_AREA, pathfinderSystem.area(tile, getMovement(unit), true));
            }
            indicateSystem.show(unit, IndicatorType.MOVE_AREA);
        }
    }

    private void hideMoveArea(int unit) {
        indicateSystem.hide(unit, IndicatorType.MOVE_AREA);
        indicateSystem.hide(unit, IndicatorType.MOVE_PATH);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Point mouseCoord = tileSystem.getMouseCoord();
        if (mouseCoord == null) return false;

        int unit = getUnit(mouseCoord);
        if (unit >= 0 && button == 0) {
            switch (mUnit.get(unit).faction) {
                case NONE:
                    break;
                case PLAYER:
                    if (selectedUnit != unit && mTurn.has(unit)) {
                        select(unit);
                    }
                    break;
                case ENEMY:
                    if (selectedUnit >= 0) {
                        hideMoveArea(selectedUnit);
                        {
                            UnitSkillEvent it = es.dispatch(UnitSkillEvent.class);
                            it.unit = selectedUnit;
                            it.skill = 0;
                            it.target = unit;
                        }
                        deselectUnit(selectedUnit);
                    }
                    break;
            }
            return true;
        } else if (selectedUnit >= 0) {
            switch (button) {
                case 0:
                    unit = selectedUnit;
                    Deque<PathNode> path = indicateSystem.getPathTo(tileSystem.get(mouseCoord), unit);
                    if (path != null) {
                        indicateSystem.remove(unit, IndicatorType.MOVE_AREA);
                        {
                            UnitMoveEvent it = es.dispatch(UnitMoveEvent.class);
                            it.unit = unit;
                            it.path = path;
                        }
                        deselectUnit(unit);
                    }
                    deselectUnit();
                    return true;
                case 1:
                    deselectUnit();
                    return true;

            }
        }

        return false;
    }

    private void select(int unit) {
        showMoveArea(unit);
        selectedUnit = unit;
        SkillBarView skillBarView = viewManageSystem.get(SkillBarView.class);
        if (skillBarView != null) skillBarView.setAp(mUnit.get(unit).ap);
    }

    private void deselectUnit() {
        deselectUnit(-1);
    }

    private void deselectUnit(int next) {
        if (selectedUnit < 0) return;
        hideMoveArea(selectedUnit);
        selectedUnit = -1;
        SkillBarView skillBarView = viewManageSystem.get(SkillBarView.class);
        if (skillBarView != null) skillBarView.hideAp();
        nextSelect = next;
    }

    public boolean isEnemy(int self, int target) {
        return mUnit.get(self).faction != mUnit.get(target).faction;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        Point mouseCoord = tileSystem.getMouseCoord();
        if (lastCoord != mouseCoord) {
            if (selectedUnit >= 0) {
                indicateSystem.hide(selectedUnit, IndicatorType.MOVE_PATH);
                if (mouseCoord != null) {
                    float cost = indicateSystem.showPathTo(tileSystem.get(mouseCoord), selectedUnit);
                    if (cost >= 0) {
                        SkillBarView skillBarView = viewManageSystem.get(SkillBarView.class);
                        skillBarView.setAp(
                            mUnit.get(selectedUnit).ap,
                            getMovementCost(selectedUnit, cost, true)
                        );
                    }
                }
            }
            if (mouseCoord != null) {
                int tile = tileSystem.get(mouseCoord);
                DebugView debugView = Sky.jungle.getDebugView();
                if (debugView != null) {
                    debugView.unitLabel.setText("Unit: " + mTile.get(tile).unit);
                }

                int unit = mTile.get(tile).unit;
                if (unit >= 0) {
                    UnitStatsQuickview view = viewManageSystem.show(UnitStatsQuickview.class);
                    pos.set(mPos.get(unit).xy.x + 32f, mPos.get(unit).xy.y, 0f);
                    cameraSystem.camera.project(pos);
                    UnitComponent cUnit = mUnit.get(unit);
                    {
                        VisWindow it = view.window;
                        it.setPosition(pos.x, pos.y);
                        it.align(Align.left);
                        it.getTitleLabel().setText(cUnit.faction.name());
                    }
                    view.hp.setText(cUnit.hp);
                    {
                        AttributesComponent it = mAttributes.get(unit);
                        view.intelligence.setText(it.get(AttributeType.INTELLIGENCE));
                        view.determination.setText(it.get(AttributeType.DETERMINATION));
                        view.endurance.setText(it.get(AttributeType.ENDURANCE));
                        view.awareness.setText(it.get(AttributeType.AWARENESS));
                        view.luck.setText(it.get(AttributeType.LUCK));
                    }
                } else {
                    viewManageSystem.hide(UnitStatsQuickview.class);
                }
            }
            lastCoord = mouseCoord;
        }
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.E && selectedUnit >= 0) {
            indicateSystem.remove(selectedUnit, IndicatorType.MOVE_AREA);
            combatTurnSystem.endTurn(selectedUnit);
            return true;
        }
        return false;
    }

    @Override
    public void process(int entityId) {
        if (selectedUnit >= 0 && !mTurn.has(selectedUnit)) {
            deselectUnit();
        }
        if (nextSelect >= 0) {
            if (mTurn.has(nextSelect)) {
                if (!mPathFollower.has(nextSelect) && !mAnimation.has(nextSelect)) {
                    select(nextSelect);
                    nextSelect = -1;
                }
            } else {
                nextSelect = -1;
            }
        }
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}

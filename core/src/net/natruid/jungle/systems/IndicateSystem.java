package net.natruid.jungle.systems;

import com.artemis.BaseSystem;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.utils.Bag;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntArray;
import net.natruid.jungle.components.IndicatorComponent;
import net.natruid.jungle.components.IndicatorOwnerComponent;
import net.natruid.jungle.components.LabelComponent;
import net.natruid.jungle.components.render.*;
import net.natruid.jungle.utils.Constants;
import net.natruid.jungle.utils.Logger;
import net.natruid.jungle.utils.PathNode;
import net.natruid.jungle.utils.types.IndicatorType;

import java.text.DecimalFormat;
import java.util.Deque;

public class IndicateSystem extends BaseSystem {
    private static final DecimalFormat FORMATTER = new DecimalFormat("#.#");
    private static final Color MOVE_AREA_COLOR = new Color(0, 1, 1, .4f);
    private final IntArray entityArrayBuilder = new IntArray();
    private PathfinderSystem pathfinderSystem;
    private ComponentMapper<RenderComponent> mRender;
    private ComponentMapper<PosComponent> mPos;
    private ComponentMapper<RectComponent> mRect;
    private ComponentMapper<LabelComponent> mLabel;
    private ComponentMapper<IndicatorComponent> mIndicator;
    private ComponentMapper<IndicatorOwnerComponent> mIndicatorOwner;
    private ComponentMapper<UIComponent> mUI;
    private ComponentMapper<InvisibleComponent> mInvisible;

    public void addResult(int entityId, IndicatorType indicatorType, PathNode[] pathfinderResult) {
        mIndicatorOwner.create(entityId).resultMap.put(indicatorType, pathfinderResult);
    }

    public boolean hasResult(int entityId, IndicatorType indicatorType) {
        IndicatorOwnerComponent indicatorOwnerComponent = mIndicatorOwner.get(entityId);
        return indicatorOwnerComponent != null && indicatorOwnerComponent.resultMap.containsKey(indicatorType);
    }

    public void show(int entityId, IndicatorType indicatorType) {

        IndicatorOwnerComponent cOwner = mIndicatorOwner.get(entityId);
        if (cOwner == null) throw new RuntimeException("Cannot find indicator owner: " + entityId);

        {
            int[] indicator = cOwner.indicatorMap.get(indicatorType);
            if (indicator != null) {
                for (int i : indicator) {
                    mInvisible.remove(i);
                }
                return;
            }
        }

        PathNode[] result = cOwner.resultMap.get(indicatorType);
        if (result == null) throw new RuntimeException("Cannot find indicator result: $entityId-$indicatorType");
        for (PathNode p : result) {
            int tile = p.tile;
            int indicator = world.create();
            mRender.create(indicator).z = Constants.Z_PATH_INDICATOR;
            mPos.create(indicator).set(mPos.get(tile).xy);
            RectComponent rect = mRect.create(indicator);
            rect.width = TileSystem.TILE_SIZE;
            rect.height = TileSystem.TILE_SIZE;
            rect.color.set(MOVE_AREA_COLOR);
            {
                IndicatorComponent it = mIndicator.create(indicator);
                it.entityId = entityId;
                it.type = indicatorType;
            }
            entityArrayBuilder.add(indicator);

            int indicatorText = world.create();
            mRender.create(indicatorText).z = Constants.Z_PATH_INDICATOR + 0.1f;
            mPos.create(indicatorText).set(mPos.get(tile).xy);
            LabelComponent label = mLabel.create(indicatorText);
            label.text = FORMATTER.format(p.cost);
            label.align = Align.center;
            label.fontName = "small";
            {
                IndicatorComponent it = mIndicator.create(indicatorText);
                it.entityId = entityId;
                it.type = indicatorType;
            }
            mUI.create(indicatorText);
            entityArrayBuilder.add(indicatorText);
        }
        cOwner.indicatorMap.put(indicatorType, entityArrayBuilder.toArray());
        entityArrayBuilder.clear();
    }

    public void hide(int entityId, IndicatorType indicatorType) {
        IndicatorOwnerComponent owner = mIndicatorOwner.get(entityId);
        if (owner != null) {
            int[] indicator = owner.indicatorMap.get(indicatorType);
            if (indicator != null) {
                for (int it : indicator) {
                    if (!mIndicator.has(it)) {
                        Logger.warn("Entity " + it + " doesn't have indicator component.");
                        Bag<Component> bag = new Bag<>();
                        world.getComponentManager().getComponentsFor(it, bag);
                        boolean first = true;
                        for (Component c : bag) {
                            if (!first) System.out.print(", ");
                            System.out.print(c.getClass().getSimpleName().replace("Component", ""));
                            first = false;
                        }
                        System.out.println();
                    } else {
                        mInvisible.create(it);
                    }
                }
            }
        }
    }

    public Deque<PathNode> getPathTo(int goal, int entityId) {
        IndicatorOwnerComponent owner = mIndicatorOwner.get(entityId);
        if (owner != null) {
            PathNode[] result = owner.resultMap.get(IndicatorType.MOVE_AREA);
            if (result != null) {
                return pathfinderSystem.extractPath(result, goal);
            }
        }
        return null;
    }

    public float showPathTo(int goal, int entityId) {
        Deque<PathNode> path = getPathTo(goal, entityId);
        if (path == null) return Float.MIN_VALUE;
        remove(entityId, IndicatorType.MOVE_PATH);
        for (PathNode node : path) {
            {
                int indicator = world.create();
                mRender.create(indicator).z = Constants.Z_PATH_INDICATOR;
                {
                    PosComponent it = mPos.create(indicator);
                    it.set(mPos.get(node.tile).xy);
                }
                {
                    RectComponent it = mRect.create(indicator);
                    it.width = TileSystem.TILE_SIZE;
                    it.height = TileSystem.TILE_SIZE;
                    it.color.set(MOVE_AREA_COLOR);
                }
                {
                    IndicatorComponent it = mIndicator.create(indicator);
                    it.entityId = entityId;
                    it.type = IndicatorType.MOVE_PATH;
                }
                entityArrayBuilder.add(indicator);
            }
        }
        mIndicatorOwner.get(entityId).indicatorMap.put(IndicatorType.MOVE_PATH, entityArrayBuilder.toArray());
        entityArrayBuilder.clear();
        return path.getLast().cost;
    }

    public void remove(int entityId, IndicatorType indicatorType) {
        IndicatorOwnerComponent cOwner = mIndicatorOwner.get(entityId);
        if (cOwner == null) return;
        //noinspection SwitchStatementWithTooFewBranches
        switch (indicatorType) {
            case MOVE_AREA:
                cOwner.resultMap.remove(indicatorType);
                remove(entityId, IndicatorType.MOVE_PATH);
                break;
            default:
        }
        int[] indicator = cOwner.indicatorMap.get(indicatorType);
        if (indicator != null) {
            for (int it : indicator) {
                if (mIndicator.has(it)) world.delete(it);
            }
        }
        cOwner.indicatorMap.remove(indicatorType);
    }

    @Override
    protected void processSystem() {
    }
}

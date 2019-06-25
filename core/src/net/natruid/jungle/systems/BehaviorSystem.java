package net.natruid.jungle.systems;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.annotations.EntityId;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectMap;
import net.natruid.jungle.components.BehaviorComponent;
import net.natruid.jungle.components.TurnComponent;
import net.natruid.jungle.components.UnitComponent;
import net.natruid.jungle.utils.ai.BehaviorAction;
import net.natruid.jungle.utils.types.UnitCondition;
import net.natruid.jungle.utils.types.UnitTargetType;

import java.util.*;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BehaviorSystem extends BaseEntitySystem {
    private static final Map<String, Float> scoreMap = new HashMap<String, Float>() {
        {
            put("kill", 1000f);
            put("damage", 100f);
        }
    };
    private final ObjectMap<UnitTargetType, List<Integer>> unitGroup = new ObjectMap<>();
    private final IntArray idleAgents = new IntArray();
    private final IntArray activeAgents = new IntArray();

    private Phase phase = Phase.STOPPED;
    private Thread currentThread = null;
    private ExecutorService planExecutor = Executors.newFixedThreadPool(8);
    private CompletionService<Boolean> completion = new ExecutorCompletionService<>(planExecutor);
    @EntityId
    private int currentUnit = -1;
    @EntityId
    private int performingUnit = -1;
    private UnitManageSystem unitManageSystem;
    private CombatTurnSystem combatTurnSystem;
    private FlowControlSystem flowControlSystem;
    private TileSystem tileSystem;
    private ThreatSystem threatSystem;
    private ComponentMapper<UnitComponent> mUnit;
    private ComponentMapper<BehaviorComponent> mBehavior;
    private float highestScore = 0;

    public BehaviorSystem() {
        super(Aspect.all(BehaviorComponent.class, TurnComponent.class));
    }

    public boolean isReady() {
        return phase == Phase.READY || phase == Phase.STOPPED;
    }

    public void prepare() {
        if (phase == Phase.READY) return;
        if (phase != Phase.STOPPED) throw new RuntimeException("BehaviorSystem has not stopped yet: " + phase);
        phase = Phase.READY;
    }

    public void reset() {
        if (currentThread != null) {
            currentThread.interrupt();
            currentThread = null;
        }
        if (planExecutor != null) {
            planExecutor.shutdown();
            planExecutor = Executors.newFixedThreadPool(8);
            completion = new ExecutorCompletionService<>(planExecutor);
        }
        currentUnit = -1;
        unitGroup.clear();
        phase = Phase.STOPPED;
    }

    @Override
    protected void inserted(int entityId) {
        super.inserted(entityId);
        mBehavior.get(entityId).tree.init(world, entityId);
    }

    private void filterAgents() {
        idleAgents.clear();
        activeAgents.clear();
        IntBag entityIds = getEntityIds();
        int[] data = entityIds.getData();
        for (int i = 0; i < entityIds.size(); i++) {
            int unit = data[i];
            if (mUnit.get(unit).faction != combatTurnSystem.getFaction()) continue;
            if (mBehavior.get(unit).threatMap.size == 0) {
                idleAgents.add(unit);
            } else {
                activeAgents.add(unit);
            }
        }
    }

    @Override
    protected void processSystem() {
        switch (phase) {
            case READY:
                if (getEntityIds().size() == 0) {
                    phase = Phase.STOPPING;
                } else {
                    for (Integer unit : getUnitGroup(UnitTargetType.ANY)) {
                        threatSystem.checkAlert(unit);
                    }
                    filterAgents();
                    phase = Phase.CHECKING;
                }
                break;
            case CHECKING:
                phase = Phase.PLANNING;
                currentThread = new Thread(() -> {
                    while (!flowControlSystem.isReady() || unitManageSystem.isBusy(performingUnit)) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ignored) {
                        }
                    }
                    phase = plan() ? Phase.PERFORMING : Phase.STOPPING;
                });
                currentThread.start();
                break;
            case PLANNING:
                break;
            case PERFORMING:
                assert currentUnit >= 0;
                int unit = currentUnit;
                performingUnit = unit;
                BehaviorAction execution = mBehavior.get(unit).execution;
                if (execution != null) {
                    execution.execute();
                    mBehavior.get(unit).execution = null;
                }
                phase = Phase.CHECKING;
                break;
            case STOPPING:
                for (int i = 0; i < activeAgents.size; i++) {
                    combatTurnSystem.endTurn(activeAgents.get(i));
                }
                for (int i = 0; i < idleAgents.size; i++) {
                    combatTurnSystem.endTurn(idleAgents.get(i));
                }
                phase = Phase.STOPPED;
                break;
            case STOPPED:
        }
    }

    private boolean plan() {
        for (int i = 0; i < activeAgents.size; i++) {
            mBehavior.get(activeAgents.get(i)).tree.reset();
        }

        highestScore = 0;
        currentUnit = -1;

        for (int i = 0; i < activeAgents.size; i++) {
            int finalI = i;
            completion.submit(() -> {
                int entityId = activeAgents.get(finalI);
                BehaviorComponent agent = mBehavior.get(entityId);
                boolean success = agent.tree.run();
                if (success) {
                    setBestUnit(entityId, agent.score);
                }
                return true;
            });
        }

        for (int i = 0; i < activeAgents.size; i++) {
            try {
                completion.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return currentUnit >= 0;
    }

    private synchronized void setBestUnit(int unit, float score) {
        if (currentUnit < 0 || highestScore < score) {
            currentUnit = unit;
            highestScore = score;
        }
    }

    public synchronized List<Integer> getUnitGroup(UnitTargetType targetType) {
        List<Integer> group = unitGroup.get(targetType);
        if (group == null) {
            switch (targetType) {
                case ANY:
                    group = unitManageSystem.getUnits();
                    break;
                case FRIENDLY:
                    group = unitManageSystem.getAllies(combatTurnSystem.getFaction());
                    break;
                case HOSTILE:
                    group = unitManageSystem.getEnemies(combatTurnSystem.getFaction());
                    break;
                default:
                    throw new RuntimeException("Unknown target type.");
            }
            unitGroup.put(targetType, group);
        }
        return group;
    }

    public ArrayList<Integer> getSortedUnitList(int self, UnitTargetType targetType, UnitCondition condition) {
        ArrayList<Integer> list = new ArrayList<>(getUnitGroup(targetType));
        switch (condition) {
            case WEAK:
                list.sort(Comparator.comparingInt(i -> mUnit.get(i).hp));
                break;
            case STRONG:
                list.sort(Comparator.comparingInt(i -> mUnit.get((Integer) i).hp).reversed());
                break;
            case CLOSE:
                list.sort(Comparator.comparingDouble(i -> tileSystem.getDistance(mUnit.get(i).tile, mUnit.get(self).tile)));
                break;
            case FAR:
                list.sort(Comparator.comparingDouble(i -> tileSystem.getDistance(mUnit.get((Integer) i).tile, mUnit.get(self).tile)).reversed());
                break;
        }
        return list;
    }

    public synchronized int getUnit(int self, UnitTargetType targetType, UnitCondition condition) {
        List<Integer> group = getUnitGroup(targetType);
        int ret;
        switch (condition) {
            case WEAK:
                ret = group.stream().min(Comparator.comparingInt(i -> mUnit.get(i).hp)).orElse(-1);
                break;
            case STRONG:
                ret = group.stream().max(Comparator.comparingInt(i -> mUnit.get(i).hp)).orElse(-1);
                break;
            case CLOSE:
                ret = group.stream()
                    .min(Comparator.comparingDouble(i -> tileSystem.getDistance(mUnit.get(i).tile, mUnit.get(self).tile)))
                    .orElse(-1);
                break;
            case FAR:
                ret = group.stream()
                    .max(Comparator.comparingDouble(i -> tileSystem.getDistance(mUnit.get(i).tile, mUnit.get(self).tile)))
                    .orElse(-1);
                break;
            default:
                throw new RuntimeException("Unknown condition.");
        }
        return ret;
    }

    public float getScore(String result, int apCost, float amount) {
        float score = scoreMap.getOrDefault(result, 0f);
        score += (amount - apCost) / 100f;
        return score;
    }

    @Override
    protected void dispose() {
        if (currentThread != null) {
            currentThread.interrupt();
        }
        if (planExecutor != null) {
            planExecutor.shutdown();
        }
    }

    private enum Phase {READY, CHECKING, PLANNING, PERFORMING, STOPPING, STOPPED}
}

package net.natruid.jungle.systems;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.utils.Array;
import net.natruid.jungle.components.StatsComponent;
import net.natruid.jungle.components.TurnComponent;
import net.natruid.jungle.components.UnitComponent;
import net.natruid.jungle.utils.Constants;
import net.natruid.jungle.utils.Logger;
import net.natruid.jungle.utils.types.Faction;

public class CombatTurnSystem extends BaseEntitySystem {
    private final Array<Faction> factionList = new Array<>();
    private int turn;
    private int currentFactionIndex = 0;
    private Phase phase = Phase.NONE;

    private BehaviorSystem behaviorSystem;
    private FlowControlSystem flowControlSystem;
    private ComponentMapper<TurnComponent> mTurn;
    private ComponentMapper<UnitComponent> mUnit;
    private ComponentMapper<StatsComponent> mStats;

    private EntitySubscription unitSubscription;

    public CombatTurnSystem() {
        super(Aspect.all(TurnComponent.class));
    }

    @Override
    protected void initialize() {
        super.initialize();
        unitSubscription = world.getAspectSubscriptionManager().get(Aspect.all(UnitComponent.class));
    }

    public int getTurn() {
        return turn;
    }

    public void start() {
        phase = Phase.START;
    }

    public void addFaction(Faction faction) {
        if (factionList.contains(faction, true)) return;
        factionList.add(faction);
    }

    public Faction getFaction() {
        return factionList.get(currentFactionIndex);
    }

    public void removeFaction(Faction faction) {
        factionList.removeValue(faction, true);
    }

    public void reset() {
        factionList.clear();
        currentFactionIndex = 0;
        phase = Phase.NONE;
    }

    @Override
    protected void processSystem() {
        switch (phase) {
            case START:
                giveTurn(factionList.get(currentFactionIndex));
                phase = Phase.READY;
                break;
            case READY:
                if (getEntityIds().isEmpty() && behaviorSystem.isReady() && flowControlSystem.isReady()) {
                    phase = Phase.NEXT_TURN;
                }
                break;
            case NEXT_TURN:
                if (currentFactionIndex >= factionList.size - 1) {
                    turn += 1;
                    currentFactionIndex = 0;
                } else {
                    currentFactionIndex += 1;
                }

                Faction nextFaction = factionList.get(currentFactionIndex);
                Logger.debug("Turn ended. Next faction:" + nextFaction);
                giveTurn(nextFaction);
                behaviorSystem.prepare();
                phase = Phase.READY;
                break;
        }
    }

    public void giveTurn(Faction faction) {
        IntBag entities = unitSubscription.getEntities();
        int[] data = entities.getData();
        for (int i = 0; i < entities.size(); i++) {
            int unit = data[i];
            UnitComponent cUnit = mUnit.get(unit);

            if (cUnit.faction != faction) continue;

            cUnit.ap = Math.min((cUnit.ap + 4 + mStats.get(unit).getAp()), Constants.MAX_AP);
            mTurn.create(unit);
        }
    }

    public void endTurn(int unit) {
        if (!mTurn.has(unit)) return;
        mTurn.remove(unit);
    }

    private enum Phase {NONE, START, READY, NEXT_TURN}
}

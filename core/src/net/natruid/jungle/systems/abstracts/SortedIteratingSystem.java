package net.natruid.jungle.systems.abstracts;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class SortedIteratingSystem extends BaseEntitySystem {
    private final ArrayList<Integer> entities = new ArrayList<>();
    private boolean needSorting = false;

    public SortedIteratingSystem(Aspect.Builder aspect) {
        super(aspect);
    }

    public List<Integer> getSortedEntityIds() {
        return entities;
    }

    public abstract Comparator<Integer> getComparator();

    @Override
    protected void begin() {
        super.begin();
        world.inject(getComparator());
    }

    @Override
    protected void inserted(int entityId) {
        super.inserted(entityId);
        entities.add(entityId);
        needSorting = true;
    }

    @Override
    protected void removed(int entityId) {
        super.removed(entityId);
        entities.remove((Integer) entityId);
    }

    @Override
    protected void processSystem() {
        if (needSorting) {
            entities.sort(getComparator());
            needSorting = false;
        }
        preProcess();
        for (int entity : entities) {
            process(entity);
        }
        postProcess();
    }

    public void sort() {
        needSorting = true;
    }

    public abstract void process(int entityId);

    public void preProcess() {
    }

    public void postProcess() {
    }
}

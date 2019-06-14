package net.natruid.jungle.utils;

import com.artemis.ComponentMapper;
import net.natruid.jungle.components.UnitComponent;

import java.util.Comparator;

public class FactionComparator implements Comparator<Integer> {
    private ComponentMapper<UnitComponent> mUnit;

    @Override
    public int compare(Integer p0, Integer p1) {
        int f0 = mUnit.get(p0).faction.ordinal();
        int f1 = mUnit.get(p1).faction.ordinal();
        return Integer.compare(f0, f1);
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }
}

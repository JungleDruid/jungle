package net.natruid.jungle.components;

import com.artemis.Component;
import net.natruid.jungle.utils.types.StatType;

public class StatsComponent extends Component {
    public final int[] values = new int[StatType.length];
    public boolean dirty = true;

    protected void reset() {
        for (int i = 0; i < values.length; i++) {
            values[i] = 0;
        }
    }

    public final int getHp() {
        return this.values[StatType.HP.ordinal()];
    }

    public final float getSpeed() {
        return this.values[StatType.SPEED.ordinal()] / 100f;
    }

    public final float getDamage() {
        return this.values[StatType.DAMAGE.ordinal()] / 100f + 1f;
    }

    public final float getHeal() {
        return this.values[StatType.HEAL.ordinal()] / 100f + 1f;
    }

    public final float getAccuracy() {
        return this.values[StatType.ACCURACY.ordinal()] / 100f + 1f;
    }

    public final float getDodge() {
        return this.values[StatType.DODGE.ordinal()] / 100f + 1f;
    }

    public final float getArea() {
        return this.values[StatType.AREA.ordinal()] / 100f + 1f;
    }

    public final float getDuration() {
        return this.values[StatType.DURATION.ordinal()] / 100f + 1f;
    }

    public final int getRange() {
        return this.values[StatType.RANGE.ordinal()];
    }

    public final int getAp() {
        return this.values[StatType.AP.ordinal()];
    }

    public final boolean getStun() {
        return this.values[StatType.STUN.ordinal()] > 0;
    }
}

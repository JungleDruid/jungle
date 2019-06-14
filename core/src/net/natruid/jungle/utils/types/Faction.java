package net.natruid.jungle.utils.types;

public enum Faction {
    NONE(0), PLAYER(1), ENEMY(1 << 1);

    public final int value;

    Faction(int value) {
        this.value = value;
    }
}

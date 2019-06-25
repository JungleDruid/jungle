package net.natruid.jungle.utils.types;

public enum TerrainType {
    NONE(0), DIRT(1), GRASS(2), WATER(16);

    private static TerrainType[] values = TerrainType.values();
    public final int value;

    TerrainType(int value) {
        this.value = value;
    }

    public static TerrainType fromValue(int value) {
        for (TerrainType type :
            values) {
            if (type.value == value) return type;
        }
        return null;
    }
}

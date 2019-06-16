package net.natruid.jungle.data;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import net.natruid.jungle.utils.AttributeModifier;
import net.natruid.jungle.utils.types.AttributeType;

public class StatDef implements Json.Serializable {
    public static final StatDef NONE = new StatDef();
    private static final Array<AttributeModifier> modifierList = new Array<>(AttributeModifier.class);
    public int base = 0;
    public int level = 0;
    public AttributeModifier[] attributes = null;

    @Override
    public void write(Json json) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        for (JsonValue stat : jsonData) {
            switch (stat.name) {
                case "base":
                    base = stat.asInt();
                    break;
                case "level":
                    level = stat.asInt();
                    break;
                case "attributes":
                    for (JsonValue attr : stat) {
                        AttributeType type = AttributeType.valueOf(attr.name.toUpperCase());
                        int add = 0;
                        float mul = 0f;
                        for (JsonValue mod : attr) {
                            switch (mod.name) {
                                case "add":
                                    add = mod.asInt();
                                    break;
                                case "mul":
                                    mul = mod.asFloat();
                                    break;
                            }
                        }
                        modifierList.add(new AttributeModifier(type, add, mul));
                    }
                    break;
            }
        }
        if (modifierList.size > 0) {
            attributes = modifierList.toArray(AttributeModifier.class);
            modifierList.clear();
        }
    }
}

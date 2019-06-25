package net.natruid.jungle.utils;

import net.natruid.jungle.utils.types.AttributeType;

public class AttributeModifier implements Modifier {
    public final AttributeType type;
    public final int add;
    public final float mul;

    public AttributeModifier(AttributeType type, int add, float mul) {
        this.type = type;
        this.add = add;
        this.mul = mul;
    }
}

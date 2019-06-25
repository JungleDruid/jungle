package net.natruid.jungle.utils.types;

public enum ShaderUniformType {
    UNKNOWN(0),
    UNIFORM1F(1), UNIFORM2F(2), UNIFORM3F(3), UNIFORM4F(4),
    UNIFORM1FV(5), UNIFORM2FV(6), UNIFORM3FV(7), UNIFORM4FV(8),
    UNIFORM1I(9), UNIFORM2I(10), UNIFORM3I(11), UNIFORM4I(12);

    public final int value;

    ShaderUniformType(int value) {
        this.value = value;
    }
}

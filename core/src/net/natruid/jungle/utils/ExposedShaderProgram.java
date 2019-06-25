package net.natruid.jungle.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ObjectMap;
import net.natruid.jungle.core.Sky;
import net.natruid.jungle.utils.types.ShaderUniformType;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public final class ExposedShaderProgram extends ShaderProgram {
    private static final ObjectMap<Pair<String, String>, ObjectMap<Integer, ShaderUniformType>> uniformTypeMap
        = new ObjectMap<>();

    public final Pair<String, String> pair;
    private int program = 0;
    private boolean disposed = false;
    private FloatBuffer floatBuffer = BufferUtils.newFloatBuffer(16);
    private IntBuffer intBuffer = BufferUtils.newIntBuffer(16);

    public ExposedShaderProgram(Pair<String, String> pair) {
        super(
            Sky.scout.locate("assets/shaders/" + pair.getFirst() + "Vertex.glsl"),
            Sky.scout.locate("assets/shaders/" + pair.getSecond() + "Fragment.glsl")
        );
        this.pair = pair;
    }

    @Override
    protected int createProgram() {
        program = super.createProgram();
        return program;
    }

    @Override
    public void dispose() {
        super.dispose();
        disposed = true;
    }

    public int getProgram() {
        return program;
    }

    public boolean isDisposed() {
        return disposed;
    }

    public void copy(ExposedShaderProgram other) {
        assert this.pair == other.pair;
        final ObjectMap<Integer, ShaderUniformType> map = uniformTypeMap.get(pair);
        if (map == null) return;
        final GL20 gl = Gdx.gl;
        for (ObjectMap.Entry<Integer, ShaderUniformType> kv : map.entries()) {
            final Integer location = kv.key;
            final ShaderUniformType type = kv.value;
            if (type.value == 0) continue;
            if (type.value < 9) {
                floatBuffer.clear();
                gl.glGetUniformfv(other.program, location, floatBuffer);
            } else {
                intBuffer.clear();
                gl.glGetUniformiv(other.program, location, intBuffer);
            }
            switch (type) {
                case UNIFORM1F:
                    gl.glUniform1f(location, floatBuffer.get());
                    break;
                case UNIFORM2F:
                    gl.glUniform2f(location, floatBuffer.get(), floatBuffer.get());
                    break;
                case UNIFORM3F:
                    gl.glUniform3f(location, floatBuffer.get(), floatBuffer.get(), floatBuffer.get());
                    break;
                case UNIFORM4F:
                    gl.glUniform4f(location, floatBuffer.get(), floatBuffer.get(), floatBuffer.get(), floatBuffer.get());
                    break;
                case UNIFORM1FV:
                    gl.glUniform1fv(location, 0, floatBuffer);
                    break;
                case UNIFORM2FV:
                    gl.glUniform2fv(location, 0, floatBuffer);
                    break;
                case UNIFORM3FV:
                    gl.glUniform3fv(location, 0, floatBuffer);
                    break;
                case UNIFORM4FV:
                    gl.glUniform4fv(location, 0, floatBuffer);
                    break;
                case UNIFORM1I:
                    gl.glUniform1i(location, intBuffer.get());
                    break;
                case UNIFORM2I:
                    gl.glUniform2i(location, intBuffer.get(), intBuffer.get());
                    break;
                case UNIFORM3I:
                    gl.glUniform3i(location, intBuffer.get(), intBuffer.get(), intBuffer.get());
                    break;
                case UNIFORM4I:
                    gl.glUniform4i(location, intBuffer.get(), intBuffer.get(), intBuffer.get(), intBuffer.get());
                    break;
                case UNKNOWN:
                    break;
            }
        }
    }

    private void setType(int location, ShaderUniformType type) {
        ObjectMap<Integer, ShaderUniformType> map = uniformTypeMap.get(pair);
        if (map == null) {
            map = new ObjectMap<>();
        }
        map.put(location, type);
    }

    @Override
    public void setUniformi(String name, int value) {
        setUniformi(getUniformLocation(name), value);
    }

    @Override
    public void setUniformi(int location, int value) {
        super.setUniformi(location, value);
        setType(location, ShaderUniformType.UNIFORM1I);
    }

    @Override
    public void setUniformi(String name, int value1, int value2) {
        setUniformi(getUniformLocation(name), value1, value2);
    }

    @Override
    public void setUniformi(int location, int value1, int value2) {
        super.setUniformi(location, value1, value2);
        setType(location, ShaderUniformType.UNIFORM2I);
    }

    @Override
    public void setUniformi(String name, int value1, int value2, int value3) {
        setUniformi(getUniformLocation(name), value1, value2, value3);
    }

    @Override
    public void setUniformi(int location, int value1, int value2, int value3) {
        super.setUniformi(location, value1, value2, value3);
        setType(location, ShaderUniformType.UNIFORM3I);
    }

    @Override
    public void setUniformi(String name, int value1, int value2, int value3, int value4) {
        setUniformi(getUniformLocation(name), value1, value2, value3, value4);
    }

    @Override
    public void setUniformi(int location, int value1, int value2, int value3, int value4) {
        super.setUniformi(location, value1, value2, value3, value4);
        setType(location, ShaderUniformType.UNIFORM4I);
    }

    @Override
    public void setUniformf(String name, float value) {
        setUniformf(getUniformLocation(name), value);
    }

    @Override
    public void setUniformf(int location, float value) {
        super.setUniformf(location, value);
        setType(location, ShaderUniformType.UNIFORM1F);
    }

    @Override
    public void setUniformf(String name, float value1, float value2) {
        setUniformf(getUniformLocation(name), value1, value2);
    }

    @Override
    public void setUniformf(int location, float value1, float value2) {
        super.setUniformf(location, value1, value2);
        setType(location, ShaderUniformType.UNIFORM2F);
    }

    @Override
    public void setUniformf(String name, float value1, float value2, float value3) {
        setUniformf(getUniformLocation(name), value1, value2, value3);
    }

    @Override
    public void setUniformf(int location, float value1, float value2, float value3) {
        super.setUniformf(location, value1, value2, value3);
        setType(location, ShaderUniformType.UNIFORM3F);
    }

    @Override
    public void setUniformf(String name, float value1, float value2, float value3, float value4) {
        setUniformf(getUniformLocation(name), value1, value2, value3, value4);
    }

    @Override
    public void setUniformf(int location, float value1, float value2, float value3, float value4) {
        super.setUniformf(location, value1, value2, value3, value4);
        setType(location, ShaderUniformType.UNIFORM4F);
    }

    @Override
    public void setUniform1fv(String name, float[] values, int offset, int length) {
        setUniform1fv(getUniformLocation(name), values, offset, length);
    }

    @Override
    public void setUniform1fv(int location, float[] values, int offset, int length) {
        super.setUniform1fv(location, values, offset, length);
        setType(location, ShaderUniformType.UNIFORM1FV);
    }

    @Override
    public void setUniform2fv(String name, float[] values, int offset, int length) {
        setUniform2fv(getUniformLocation(name), values, offset, length);
    }

    @Override
    public void setUniform2fv(int location, float[] values, int offset, int length) {
        super.setUniform2fv(location, values, offset, length);
        setType(location, ShaderUniformType.UNIFORM2FV);
    }

    @Override
    public void setUniform3fv(String name, float[] values, int offset, int length) {
        setUniform3fv(getUniformLocation(name), values, offset, length);
    }

    @Override
    public void setUniform3fv(int location, float[] values, int offset, int length) {
        super.setUniform3fv(location, values, offset, length);
        setType(location, ShaderUniformType.UNIFORM3FV);
    }

    @Override
    public void setUniform4fv(String name, float[] values, int offset, int length) {
        setUniform4fv(getUniformLocation(name), values, offset, length);
    }

    @Override
    public void setUniform4fv(int location, float[] values, int offset, int length) {
        super.setUniform4fv(location, values, offset, length);
        setType(location, ShaderUniformType.UNIFORM4FV);
    }
}

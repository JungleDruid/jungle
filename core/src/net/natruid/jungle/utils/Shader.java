package net.natruid.jungle.utils;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.ObjectMap;

import java.lang.reflect.Array;

public final class Shader {
    private static final String DEFAULT_NAME = "default";
    private static final Pair<String, String> defaultPair = new Pair<>(DEFAULT_NAME, DEFAULT_NAME);
    private static boolean resetting = false;
    private static int index = 0;
    @SuppressWarnings("unchecked")
    private static final ObjectMap<Pair<String, String>, ExposedShaderProgram>[] shaderProgramsArray
        = (ObjectMap<Pair<String, String>, ExposedShaderProgram>[]) Array.newInstance(ObjectMap.class, 2);

    static {
        for (int i = 0; i < shaderProgramsArray.length; i++) {
            shaderProgramsArray[i] = new ObjectMap<>();
        }
    }

    public static final Shader DEFAULT = new Shader();

    public static ShaderProgram getDefaultShaderProgram() {
        ExposedShaderProgram program = shaderProgramsArray[index].get(defaultPair);
        if (program == null) {
            shaderProgramsArray[index].put(defaultPair, new ExposedShaderProgram(defaultPair));
        }
        return program;
    }

    private static void resetGlobal() {
        if (resetting) return;
        resetting = true;
        index = 1 - index;
    }

    private static void restore() {
        if (!resetting) return;
        resetting = false;
        ObjectMap<Pair<String, String>, ExposedShaderProgram> map = shaderProgramsArray[1 - index];
        for (ExposedShaderProgram program : map.values()) {
            try {
                program.dispose();
            } catch (Exception ignored) {
            }
        }
        map.clear();
    }

    private ExposedShaderProgram source = null;
    private ExposedShaderProgram instance = null;
    private boolean resettingInstance = false;
    private Pair<String, String> pair;

    public Shader() {
        this(DEFAULT_NAME, DEFAULT_NAME);
    }

    public Shader(String fragment) {
        pair = new Pair<>(DEFAULT_NAME, fragment);
        initSource();
    }

    public Shader(String vertex, String fragment) {
        pair = new Pair<>(vertex, fragment);
        initSource();
    }

    private ExposedShaderProgram createShaderProgram() {
        return new ExposedShaderProgram(pair);
    }

    private void initSource() {
        ObjectMap<Pair<String, String>, ExposedShaderProgram> map = shaderProgramsArray[index];
        ExposedShaderProgram program = map.get(pair);
        if (program == null) {
            program = createShaderProgram();
            if (source != null && source.pair == pair) {
                program.copy(source);
            }
            map.put(pair, program);
        }
        source = program;
    }

    public ShaderProgram getProgram() {
        restore();
        resettingInstance = false;
        if (source.isDisposed()) {
            initSource();
        }
        assert source != null;
        return instance == null ? source : instance;
    }

    public void setProgram(String vertex, String fragment) {
        removeInstance();
        pair = new Pair<>(vertex, fragment);
        initSource();
    }

    public ShaderProgram getInstance() {
        if (instance == null) {
            ExposedShaderProgram p = createShaderProgram();
            assert source != null;
            p.copy(source);
            instance = p;
        }
        return getProgram();
    }

    public void removeInstance() {
        Utils.safelyDispose(instance);
        instance = null;
        resettingInstance = false;
    }

    public void reset() {
        Shader.resetGlobal();
        initSource();
        if (!resettingInstance && instance != null) {
            ExposedShaderProgram oldInstance = instance;
            resettingInstance = true;
            ExposedShaderProgram p = createShaderProgram();
            p.copy(oldInstance);
            instance = p;
            Utils.safelyDispose(oldInstance);
        }
    }
}

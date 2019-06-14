package net.natruid.jungle.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import net.natruid.jungle.core.Jungle;
import net.natruid.jungle.utils.Client;
import org.lwjgl.glfw.GLFW;

public final class DesktopLauncher implements Client {
    private final Jungle game;
    private long window;
    private boolean focused = false;

    public DesktopLauncher(boolean debug) {
        game = new Jungle(this, debug);
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Jungle");
        config.setWindowedMode(1024, 768);
        config.setResizable(false);
        new Lwjgl3Application(game, config);
    }

    public static void main(String[] args) {
        boolean debug = false;
        for (String s : args) {
            if (s.equals("--debug")) debug = true;
        }
        new DesktopLauncher(debug);
    }

    @Override
    public boolean init() {
        window = GLFW.glfwGetCurrentContext();
        GLFW.glfwSetWindowFocusCallback(window, (l, focused) -> {
            this.focused = focused;
            game.focusChanged();
        });
        focused = GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE;
        return true;
    }

    @Override
    public boolean resize(int width, int height) {
        GLFW.glfwSetWindowSize(window, width, height);
        return true;
    }

    @Override
    public boolean setTitle(String title) {
        GLFW.glfwSetWindowTitle(window, title);
        return true;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }
}

package net.natruid.jungle.core;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import net.natruid.jungle.data.Marsh;
import net.natruid.jungle.screens.AbstractScreen;
import net.natruid.jungle.screens.FieldScreen;
import net.natruid.jungle.screens.LoadingScreen;
import net.natruid.jungle.screens.TestScreen;
import net.natruid.jungle.utils.*;
import net.natruid.jungle.views.AbstractView;
import net.natruid.jungle.views.DebugView;
import net.natruid.jungle.views.TestView;

import java.lang.management.ManagementFactory;

import static net.natruid.jungle.utils.Utils.safelyDispose;

public class Jungle implements ApplicationListener, InputProcessor {
    private final static int backgroundFPS = 10;
    private final static boolean pauseOnBackground = true;
    private final Client client;
    private boolean debug = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
    private ScreenViewport uiViewport;
    private LoadingScreen loadingScreen;
    private boolean mouseMoved = false;
    private float time;
    private DebugView debugView;
    private AbstractScreen currentScreen;
    private Array<AbstractView> viewList = new Array<>();
    private int targetFPS = 60;
    private boolean resizing = false;
    private boolean vSync = true;
    private boolean paused = false;
    private Sync sync = new Sync();

    public Jungle(Client client, boolean debug) {
        Sky.jungle = this;
        this.client = client;
        this.debug = this.debug || debug;
    }

    public DebugView getDebugView() {
        return debugView;
    }

    public float getTime() {
        return time;
    }

    public boolean isMouseMoved() {
        return mouseMoved;
    }

    public boolean isDebug() {
        return debug;
    }

    public ScreenViewport getUiViewport() {
        return uiViewport;
    }

    public LoadingScreen getLoadingScreen() {
        return loadingScreen;
    }

    @Override
    public void create() {
        Sky.log = new Logger();
        Sky.scout = new Scout();
        Sky.marsh = new Marsh();

        Logger log = Sky.log;

        uiViewport = new ScreenViewport();
        loadingScreen = new LoadingScreen();

        log.startWatch("Initialization");
        log.debug("Initializing...");

        client.init();
        loadingScreen.progress();

        try {
            Sky.marsh.load();
        } catch (Exception e) {
            log.error("Data loading failed", e);
        }
        loadingScreen.progress();

        try {
            VisUI.load(new Bark("assets/ui/jungle.json"));
        } catch (Exception e) {
            log.error("Skin loading failed.");
        }
        loadingScreen.progress();

        try {
            I18NBundle bundle = Sky.marsh.locale.get("assets/locale/UI");
            client.setTitle(bundle.get("title"));
        } catch (Exception e) {
            log.error("I18n bundle loading failed.", e);
        }
        loadingScreen.progress();

        if (debug) {
            debugView = new DebugView();
            DebugView.show = true;
        }
        setScreen(new FieldScreen());
        Gdx.graphics.setVSync(vSync);
        log.info("Game initialized.");
        loadingScreen.finish();

        Gdx.input.setInputProcessor(this);

        log.stopWatch("Initialization");
    }

    @Override
    public void render() {
        if (paused && loadingScreen.isDone()) {
            sync.sync(1);
            return;
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float delta = Gdx.graphics.getDeltaTime();

        if (!loadingScreen.isDone()) {
            loadingScreen.render(delta);
            sync.sync(10);
            return;
        }

        time += delta;

        if (currentScreen != null) currentScreen.render(delta);
        for (AbstractView view : viewList) {
            view.render(delta);
        }
        if (debugView != null) debugView.render(delta);

        if (!resizing) {
            int f = client.isFocused() || backgroundFPS == 0 ? targetFPS : backgroundFPS;
            if (f > 0 && (f < 60 || !vSync)) {
                sync.sync(f);
            }
        } else {
            resizing = false;
        }
    }

    @Override
    public void dispose() {
        loadingScreen.dispose();
        safelyDispose(currentScreen);
        for (AbstractView view : viewList) {
            Utils.safelyDispose(view);
        }
        viewList.clear();
        safelyDispose(debugView);
        VisUI.dispose();
    }

    private void setScreen(AbstractScreen screen) {
        safelyDispose(currentScreen);

        currentScreen = screen;
        if (screen == null) return;
        screen.show();
    }

    public void showView(AbstractView view) {
        if (!viewList.contains(view, true)) {
            viewList.add(view);
            view.show();
        }
    }

    public void hideView(AbstractView view) {
        if (viewList.removeValue(view, true)) {
            view.hide();
        }
    }

    public AbstractView hideLastView() {
        if (viewList.size == 0) return null;
        AbstractView view = viewList.removeIndex(viewList.size - 1);
        view.hide();
        return view;
    }

    public void destroyView(AbstractView view) {
        hideView(view);
        safelyDispose(view);
    }

    public void destroyAllViews() {
        for (AbstractView view : viewList) {
            Utils.safelyDispose(view);
        }
        viewList.clear();
    }

    @Override
    public void pause() {
        paused = true;
        mouseMoved = false;
        if (currentScreen != null) currentScreen.pause();
        for (AbstractView view : viewList) {
            view.pause();
        }
        if (debugView != null) debugView.pause();
    }

    @Override
    public void resume() {
        paused = false;
        if (currentScreen != null) currentScreen.resume();
        for (AbstractView view : viewList) {
            view.resume();
        }
        if (debugView != null) debugView.resume();
    }

    @Override
    public void resize(int width, int height) {
        resizing = true;
        uiViewport.update(width, height, true);
        if (currentScreen != null) currentScreen.resize(width, height);
    }

    public void unfocusAll() {
        for (AbstractView view : viewList) {
            view.unfocusAll();
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            unfocusAll();
        }

        for (AbstractView view : viewList) {
            if (view.keyDown(keycode)) return true;
        }

        if (currentScreen == null) return false;
        return currentScreen.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        for (AbstractView view : viewList) {
            if (view.keyUp(keycode)) return true;
        }

        if (currentScreen != null && currentScreen.keyUp(keycode)) return true;

        if (debug) {
            if (keycode == Input.Keys.F11) {
                if (currentScreen instanceof TestScreen) {
                    destroyAllViews();
                    setScreen(new FieldScreen());
                } else {
                    setScreen(new TestScreen());
                    showView(new TestView());
                }

                return true;
            }
            if (keycode == Input.Keys.F9) {
                vSync = !vSync;
                targetFPS = vSync ? 60 : 0;
                Gdx.graphics.setVSync(vSync);
            }
            if (keycode == Input.Keys.F10) {
                Runtime.getRuntime().gc();
            }
            if (keycode == Input.Keys.F12) {
                DebugView.show = !DebugView.show;
            }
        }

        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        for (AbstractView view : viewList) {
            if (view.keyTyped(character)) return true;
        }

        if (currentScreen == null) return false;
        return currentScreen.keyTyped(character);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        unfocusAll();

        for (AbstractView view : viewList) {
            if (view.touchDown(screenX, screenY, pointer, button)) return true;
        }

        if (currentScreen == null) return false;
        return currentScreen.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        for (AbstractView view : viewList) {
            if (view.touchUp(screenX, screenY, pointer, button)) return true;
        }

        if (currentScreen == null) return false;
        return currentScreen.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        for (AbstractView view : viewList) {
            if (view.touchDragged(screenX, screenY, pointer)) return true;
        }

        if (currentScreen == null) return false;
        return currentScreen.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        mouseMoved = true;
        for (AbstractView view : viewList) {
            if (view.mouseMoved(screenX, screenY)) return true;
        }

        if (currentScreen == null) return false;
        return currentScreen.mouseMoved(screenX, screenY);
    }

    @Override
    public boolean scrolled(int amount) {
        for (AbstractView view : viewList) {
            if (view.scrolled(amount)) return true;
        }

        if (currentScreen == null) return false;
        return currentScreen.scrolled(amount);
    }

    public void focusChanged() {
        if (pauseOnBackground) {
            if (client.isFocused()) {
                resume();
            } else {
                pause();
            }
        }
    }
}

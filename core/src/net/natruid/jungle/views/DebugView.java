package net.natruid.jungle.views;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import net.natruid.jungle.utils.RendererHelper;

public class DebugView extends AbstractView {
    public static boolean show = false;
    public final VisLabel tileLabel;
    public final VisLabel unitLabel;
    private final VisLabel fpsLabel;
    private final VisLabel ramLabel;
    private final VisLabel rcLabel;
    private final VisLabel threadsLabel;
    public RendererHelper renderer;
    private int fps = 0;
    private float timer = 0;
    private int threads = 0;

    public DebugView() {
        VisTable table = new VisTable();
        table.setFillParent(true);
        table.align(Align.topLeft);
        table.padLeft(5);
        table.defaults().align(Align.left);
        fpsLabel = new VisLabel("FPS: 0", "green-small");
        table.add(fpsLabel);
        table.row();
        ramLabel = new VisLabel("RAM: 0", "green-small");
        table.add(ramLabel);
        table.row();
        rcLabel = new VisLabel("RC: 0", "green-small");
        table.add(rcLabel);
        table.row();
        threadsLabel = new VisLabel("Threads: 0", "green-small");
        table.add(threadsLabel);
        table.row();
        tileLabel = new VisLabel("Tile: -1", "green-small");
        table.add(tileLabel);
        table.row();
        unitLabel = new VisLabel("Unit: -1", "green-small");
        table.add(unitLabel);
        addActor(table);
    }

    @Override
    public void render(float delta) {
        if (!show) return;

        fps += 1;
        timer += delta;

        threads = Math.max(Thread.activeCount(), threads);

        if (timer >= 1) {
            timer -= 1;
            fpsLabel.setText("FPS: " + fps);
            Runtime runtime = Runtime.getRuntime();
            ramLabel.setText("RAM: " + ((runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024));
            threadsLabel.setText("Threads: " + threads);
            fps = 0;
            threads = 0;
        }

        if (renderer != null) {
            SpriteBatch batch = renderer.batch;
            rcLabel.setText(String.format("RC: %d/%d (%d)", batch.totalRenderCalls, renderer.begins, renderer.diffs));
            batch.totalRenderCalls = 0;
            renderer.begins = 0;
            renderer.diffs = 0;
        }

        super.render(delta);
    }
}

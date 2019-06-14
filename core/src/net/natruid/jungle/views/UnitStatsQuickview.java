package net.natruid.jungle.views;

import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisWindow;

public class UnitStatsQuickview extends AbstractView {
    public final VisLabel hp;
    public final VisLabel intelligence;
    public final VisLabel determination;
    public final VisLabel endurance;
    public final VisLabel awareness;
    public final VisLabel luck;
    public final VisWindow window;

    public UnitStatsQuickview() {
        window = new VisWindow("", "info");
        window.setMovable(false);
        window.defaults().pad(0, 5, 0, 5);
        window.add(new VisLabel("HP", "green-small"));
        hp = new VisLabel("0", "green-small");
        window.add(hp);
        window.row();
        window.add(new VisLabel("I", "green-small"));
        intelligence = new VisLabel("0", "green-small");
        window.add(intelligence);
        window.row();
        window.add(new VisLabel("D", "green-small"));
        determination = new VisLabel("0", "green-small");
        window.add(determination);
        window.row();
        window.add(new VisLabel("E", "green-small"));
        endurance = new VisLabel("0", "green-small");
        window.add(endurance);
        window.row();
        window.add(new VisLabel("A", "green-small"));
        awareness = new VisLabel("0", "green-small");
        window.add(awareness);
        window.row();
        window.add(new VisLabel("L", "green-small"));
        luck = new VisLabel("0", "green-small");
        window.add(luck);
        window.pack();
        window.setWidth(128);
        addActor(window);
    }
}

package net.natruid.jungle.views;

import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisWindow;
import net.natruid.jungle.core.Marsh;

public class TestView extends AbstractView {
    public TestView() {
        VisWindow window = new VisWindow(Marsh.I18N.INSTANCE.get("assets/locale/UI").get("title"));
        window.add(new VisLabel(Marsh.I18N.INSTANCE.get("assets/locale/UI").get("test")));
        window.add(new VisTextField("Test"));
        window.row();
        window.add(new VisTextField("Another text field"));
        window.pack();
        window.centerWindow();
        addActor(window);
    }
}

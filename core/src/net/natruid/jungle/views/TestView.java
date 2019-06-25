package net.natruid.jungle.views;

import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisWindow;
import net.natruid.jungle.core.Sky;

public class TestView extends AbstractView {
    public TestView() {
        VisWindow window = new VisWindow(Sky.marsh.locale.get("assets/locale/UI").get("title"));
        window.add(new VisLabel(Sky.marsh.locale.get("assets/locale/UI").get("test")));
        window.add(new VisTextField("Test"));
        window.row();
        window.add(new VisTextField("Another text field"));
        window.pack();
        window.centerWindow();
        addActor(window);
    }
}

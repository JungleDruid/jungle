package net.natruid.jungle.systems;

import com.artemis.BaseSystem;
import com.badlogic.gdx.utils.ObjectMap;
import net.natruid.jungle.core.Sky;
import net.natruid.jungle.utils.Utils;
import net.natruid.jungle.views.AbstractView;

@SuppressWarnings("unchecked")
public class ViewManageSystem extends BaseSystem {
    private ObjectMap<Class<? extends AbstractView>, AbstractView> viewMap = new ObjectMap<>();

    public <T extends AbstractView> T show(Class<T> type) {
        AbstractView view = viewMap.get(type);
        if (view == null) {
            try {
                view = type.newInstance();
                viewMap.put(type, view);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        assert view != null;
        Sky.jungle.showView(view);
        return (T) view;
    }

    public <T extends AbstractView> T get(Class<T> type) {
        AbstractView view = viewMap.get(type);
        if (view == null) return null;
        return (T) view;
    }

    public <T extends AbstractView> T hide(Class<T> type) {
        T t = get(type);
        if (t != null) Sky.jungle.hideView(t);
        return t;
    }

    public AbstractView hideLast() {
        return Sky.jungle.hideLastView();
    }

    public void hideAll() {
        for (AbstractView view : viewMap.values()) {
            Sky.jungle.hideView(view);
        }
    }

    @Override
    protected void dispose() {
        for (AbstractView view : viewMap.values()) {
            Utils.safelyDispose(view);
        }
        viewMap.clear();
    }

    @Override
    protected void processSystem() {
    }
}

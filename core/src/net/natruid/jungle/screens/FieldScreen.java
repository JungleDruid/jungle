package net.natruid.jungle.screens;

import com.artemis.Aspect;
import com.artemis.WorldConfigurationBuilder;
import com.artemis.managers.TagManager;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import net.mostlyoriginal.api.event.common.EventSystem;
import net.mostlyoriginal.api.event.common.SubscribeAnnotationFinder;
import net.mostlyoriginal.api.event.dispatcher.PollingPooledEventDispatcher;
import net.natruid.jungle.components.UnitComponent;
import net.natruid.jungle.core.Sky;
import net.natruid.jungle.systems.*;
import net.natruid.jungle.systems.render.*;
import net.natruid.jungle.utils.types.Faction;
import net.natruid.jungle.views.SkillBarView;

public class FieldScreen extends AbstractScreen {
    public FieldScreen() {
        init();
    }

    @Override
    WorldConfigurationBuilder getConfiguration(WorldConfigurationBuilder builder) {
        return builder.with(
            new EventSystem(new PollingPooledEventDispatcher(), new SubscribeAnnotationFinder()),
            new TagManager(),
            new FlowControlSystem(),
            new TileSystem(),
            new UnitManageSystem(),
            new CombatTurnSystem(),
            new IndicateSystem(),
            new PathfinderSystem(),
            new PathFollowSystem(),
            new AnimateSystem(),
            new ThreatSystem(),
            new BehaviorSystem(),
            new ViewManageSystem(),
            new ImageRenderSystem(),
            new LabelRenderSystem(),
            new RectRenderSystem(),
            new CustomRenderSystem(),
            new UnitHpRenderSystem()
        );
    }

    public void init(Long seed) {
        world.getSystem(TileSystem.class).create(20, 20, seed);
        UnitManageSystem unitManageSystem = world.getSystem(UnitManageSystem.class);
        int player = unitManageSystem.addUnit(0, 0, Faction.PLAYER);
        world.getMapper(UnitComponent.class).get(player).proficiencies.put("weapon", 20);
        for (int i = 0; i < 6; ) {
            if (unitManageSystem.addUnit(Sky.fate.nextInt(20), Sky.fate.nextInt(20), Faction.ENEMY) >= 0) {
                i += 1;
            }
        }
        world.getSystem(ViewManageSystem.class).show(SkillBarView.class);
        world.getSystem(CombatTurnSystem.class).start();
    }

    public void init() {
        init(Sky.fate.nextLong());
    }

    @Override
    public void show() {
        super.show();
        OrthographicCamera camera = world.getSystem(CameraSystem.class).getCamera();
        camera.translate(400, 300);
        camera.update();
    }

    @Override
    public boolean keyUp(int keycode) {
        super.keyUp(keycode);
        if (keycode == Input.Keys.R) {
            world.getSystem(BehaviorSystem.class).reset();
            world.getSystem(UnitManageSystem.class).reset();
            world.getSystem(CombatTurnSystem.class).reset();
            world.getSystem(ViewManageSystem.class).hideAll();
            IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all()).getEntities();
            int[] data = entities.getData();
            for (int i = 0; i < entities.size(); i++) {
                world.delete(data[i]);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                init(world.getSystem(TileSystem.class).getSeed());
            } else {
                init();
            }
            return true;
        }

        return false;
    }
}

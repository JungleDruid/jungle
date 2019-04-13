package net.natruid.jungle.screens

import com.artemis.Aspect
import com.artemis.WorldConfigurationBuilder
import com.artemis.managers.TagManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import net.mostlyoriginal.api.event.common.EventSystem
import net.mostlyoriginal.api.event.common.SubscribeAnnotationFinder
import net.mostlyoriginal.api.event.dispatcher.PollingPooledEventDispatcher
import net.natruid.jungle.components.UnitComponent
import net.natruid.jungle.systems.*
import net.natruid.jungle.systems.render.*
import net.natruid.jungle.utils.Faction
import net.natruid.jungle.utils.extensions.forEach
import net.natruid.jungle.views.SkillBarView
import kotlin.random.Random

class FieldScreen : AbstractScreen() {
    init {
        init()
    }

    override fun getConfiguration(builder: WorldConfigurationBuilder): WorldConfigurationBuilder {
        return builder.with(
            EventSystem(PollingPooledEventDispatcher(), SubscribeAnnotationFinder()),
            TagManager(),
            TileSystem(),
            UnitManageSystem(),
            CombatTurnSystem(),
            IndicateSystem(),
            PathfinderSystem(),
            PathFollowSystem(),
            AnimateSystem(),
            BehaviorSystem(),
            ViewManageSystem(),
            ImageRenderSystem(),
            LabelRenderSystem(),
            RectRenderSystem(),
            CustomRenderSystem(),
            UnitHpRenderSystem()
        )
    }

    fun init(seed: Long = Random.nextLong()) {
        world.getSystem(TileSystem::class.java).create(20, 20, seed)
        world.getSystem(UnitManageSystem::class.java).apply {
            addUnit(0, 0, faction = Faction.PLAYER).let {
                world.getMapper(UnitComponent::class.java)[it].proficiencies["weapon"] = 20
            }
            var count = 0
            while (count < 20) {
                if (addUnit(Random.nextInt(20), Random.nextInt(20), Faction.ENEMY) >= 0)
                    count += 1
            }
        }
        world.getSystem(ViewManageSystem::class.java).show<SkillBarView>()
        world.getSystem(CombatTurnSystem::class.java).start()
    }

    override fun show() {
        super.show()
        val camera = world.getSystem(CameraSystem::class.java).camera
        camera.translate(400f, 300f)
        camera.update()
    }

    override fun keyUp(keycode: Int): Boolean {
        super.keyUp(keycode)
        if (keycode == Input.Keys.R) {
            world.getSystem(BehaviorSystem::class.java).reset()
            world.getSystem(UnitManageSystem::class.java).reset()
            world.getSystem(CombatTurnSystem::class.java).reset()
            world.getSystem(ViewManageSystem::class.java).hideAll()
            world.aspectSubscriptionManager.get(Aspect.all()).entities.forEach {
                world.delete(it)
            }
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                init(world.getSystem(TileSystem::class.java).seed)
            } else {
                init()
            }
            return true
        }

        return false
    }
}

package net.natruid.jungle.systems

import com.artemis.Aspect
import com.artemis.BaseEntitySystem
import com.artemis.ComponentMapper
import net.natruid.jungle.components.render.ActingComponent

class FlowControlSystem : BaseEntitySystem(Aspect.all(ActingComponent::class.java)) {

    val ready get() = entityIds.isEmpty

    private lateinit var mActing: ComponentMapper<ActingComponent>

    override fun processSystem() {}

    fun addAct(entityId: Int) {
        mActing.create(entityId).apply {
            actions += 1
        }
    }

    fun delAct(entityId: Int) {
        mActing[entityId].apply {
            actions -= 1
            if (actions == 0) {
                mActing.remove(entityId)
            }
        }
    }
}

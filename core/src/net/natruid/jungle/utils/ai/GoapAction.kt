package net.natruid.jungle.utils.ai

import com.artemis.ComponentMapper
import net.natruid.jungle.components.UnitComponent
import net.natruid.jungle.systems.GoapSystem

abstract class GoapAction {
    protected lateinit var goapSystem: GoapSystem
    protected lateinit var mUnit: ComponentMapper<UnitComponent>

    val preconditions: Map<GoapType, Boolean> = HashMap()
    val effects: Map<GoapType, Boolean> = HashMap()

    init {
        this.prepare()
    }

    protected fun addPrecondition(key: GoapType, value: Boolean) {
        (preconditions as HashMap)[key] = value
    }

    protected fun addEffect(key: GoapType, value: Boolean) {
        (effects as HashMap)[key] = value
    }

    protected abstract fun prepare()

    abstract fun check(self: Int): Float?

    abstract fun perform(self: Int)

    abstract fun reset()
}

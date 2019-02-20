package net.natruid.jungle.systems

import com.artemis.Aspect
import com.artemis.BaseEntitySystem

abstract class SortedIteratingSystem(aspect: Aspect.Builder)
    : BaseEntitySystem(aspect) {
    private val entities = ArrayList<Int>()
    private var needSorting = false

    val sortedEntityIds get() = entities as List<Int>

    abstract val comparator: Comparator<in Int>

    override fun inserted(entityId: Int) {
        super.inserted(entityId)
        entities.add(entityId)
        needSorting = true
    }

    override fun removed(entityId: Int) {
        super.removed(entityId)
        entities.remove(entityId)
    }

    override fun processSystem() {
        if (needSorting) {
            entities.sortWith(comparator)
            needSorting = false
        }
        for (entity in entities) {
            process(entity)
        }
    }

    fun sort() {
        needSorting = true
    }

    abstract fun process(entityId: Int)
}

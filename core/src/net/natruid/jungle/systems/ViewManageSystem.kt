package net.natruid.jungle.systems

import com.artemis.BaseSystem
import ktx.assets.disposeSafely
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.views.AbstractView
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class ViewManageSystem : BaseSystem() {
    private val viewMap = HashMap<KClass<out AbstractView>, AbstractView>()

    inline fun <reified T : AbstractView> show(): T {
        return show(T::class)
    }

    fun <T : AbstractView> show(type: KClass<T>): T {
        var view = viewMap[type]
        if (view == null) {
            view = AbstractView.createView(type)
            viewMap[type] = view
        }
        Jungle.instance.showView(view)
        return view as T
    }

    inline fun <reified T : AbstractView> get(): T? {
        return get(T::class)
    }

    fun <T : AbstractView> get(type: KClass<out AbstractView>): T? {
        val view = viewMap[type] ?: return null
        return view as T
    }

    inline fun <reified T : AbstractView> hide(): T? {
        return hide(T::class)
    }

    fun <T : AbstractView> hide(type: KClass<out AbstractView>): T? {
        val view = viewMap[type] ?: return null
        Jungle.instance.hideView(view)
        return view as T
    }

    fun hideLast(): AbstractView? {
        return Jungle.instance.hideLastView()
    }

    fun hideAll() {
        viewMap.values.forEach {
            Jungle.instance.hideView(it)
        }
    }

    override fun dispose() {
        viewMap.values.forEach {
            it.disposeSafely()
        }
        viewMap.clear()
    }

    override fun processSystem() {}
}

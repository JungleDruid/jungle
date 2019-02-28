package net.natruid.jungle.utils.extensions

import net.mostlyoriginal.api.event.common.Event
import net.mostlyoriginal.api.event.common.EventSystem
import kotlin.reflect.KClass

fun <T : Event> EventSystem.dispatch(kClass: KClass<T>): T {
    return dispatch(kClass.java)
}

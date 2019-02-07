package net.natruid.jungle.utils.extensions

import com.artemis.utils.ImmutableIntBag

fun ImmutableIntBag<*>.forEach(function: (Int) -> Unit) {
    val s = size()
    for (i in 0 until s) {
        function(this[i])
    }
}

fun ImmutableIntBag<*>.first(function: (Int) -> Boolean): Int? {
    val s = size()
    for (i in 0 until s) {
        val value = this[i]
        if (function(value)) return value
    }
    return null
}

fun <T> ImmutableIntBag<*>.firstObject(function: (Int) -> T?): T? {
    val s = size()
    for (i in 0 until s) {
        val value = function(this[i])
        if (value != null) return value
    }
    return null
}

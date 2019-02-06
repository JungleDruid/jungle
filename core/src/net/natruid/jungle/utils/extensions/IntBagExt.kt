package net.natruid.jungle.utils.extensions

import com.artemis.utils.IntBag

fun IntBag.forEach(function: (Int) -> Unit) {
    val data = data
    val s = size()
    for (i in 0 until s) {
        function(data[i])
    }
}

fun IntBag.first(function: (Int) -> Boolean): Int? {
    val data = data
    val s = size()
    for (i in 0 until s) {
        val value = data[i]
        if (function(value)) return value
    }
    return null
}

fun <T> IntBag.firstObject(function: (Int) -> T?): T? {
    val data = data
    val s = size()
    for (i in 0 until s) {
        val value = function(data[i])
        if (value != null) return value
    }
    return null
}

package net.natruid.jungle.utils

interface Client {
    fun resize(width: Int, height: Int): Boolean
    fun setTitle(title: String): Boolean
    fun init(): Boolean
    fun isFocused(): Boolean
}

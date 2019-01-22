package net.natruid.jungle.utils

interface DesktopClient {
    fun resize(width: Int, height: Int): Boolean
    fun setTitle(title: String): Boolean
}
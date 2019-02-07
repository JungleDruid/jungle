package net.natruid.jungle.desktop

fun main(vararg args: String) {
    val debug = args.contains("--debug")
    DesktopLauncher(debug)
}

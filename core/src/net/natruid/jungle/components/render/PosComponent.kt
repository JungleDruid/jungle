package net.natruid.jungle.components.render

class PosComponent(x: Float = 0f, y: Float = 0f) : Vector2Component(x, y) {
    companion object {
        val DEFAULT = PosComponent()
    }
}

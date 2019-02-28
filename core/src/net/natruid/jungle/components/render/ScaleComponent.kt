package net.natruid.jungle.components.render

class ScaleComponent(x: Float = 1f, y: Float = 1f) : Vector2Component(x, y) {
    companion object {
        val DEFAULT = ScaleComponent()
    }

    override fun reset() {
        set(1f, 1f)
    }
}

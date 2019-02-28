package net.natruid.jungle.components.render

class PivotComponent(x: Float = 0.5f, y: Float = 0.5f) : Vector2Component(x, y) {
    companion object {
        val DEFAULT = PivotComponent()
    }

    override fun reset() {
        set(0.5f, 0.5f)
    }
}

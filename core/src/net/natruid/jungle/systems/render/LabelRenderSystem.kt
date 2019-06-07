package net.natruid.jungle.systems.render

import com.artemis.Aspect
import com.artemis.ComponentMapper
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.utils.Align
import net.natruid.jungle.components.LabelComponent
import net.natruid.jungle.components.render.PivotComponent
import net.natruid.jungle.core.Marsh

class LabelRenderSystem : RenderSystem(
    Aspect.all(LabelComponent::class.java)
) {
    private lateinit var mLabel: ComponentMapper<LabelComponent>
    private lateinit var mPivot: ComponentMapper<PivotComponent>

    private val glyphLayout = GlyphLayout()

    override fun render(entityId: Int) {
        val label = mLabel[entityId]
        val pivot = mPivot.getSafe(entityId, PivotComponent.DEFAULT)

        val font = Marsh.Fonts[label.fontName]
        val hAlign = when {
            label.align == Align.top -> Align.center
            label.align == Align.bottom -> Align.center
            else -> label.align
        }
        glyphLayout.setText(font, label.text, label.color, label.width, hAlign, label.width > 0f)
        val offsetY: Float = when {
            label.align.and(Align.top) != 0 -> 0f
            label.align.and(Align.bottom) != 0 -> glyphLayout.height
            else -> glyphLayout.height * pivot.xy.y
        }
        val position = getPos()
        font.draw(renderer.batch, glyphLayout, position.x, position.y + offsetY)
    }
}

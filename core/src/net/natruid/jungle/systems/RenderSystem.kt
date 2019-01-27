package net.natruid.jungle.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import ktx.ashley.oneOf
import net.natruid.jungle.components.*
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.core.Marsh
import net.natruid.jungle.utils.Layer
import net.natruid.jungle.utils.RendererHelper

class RenderSystem
    : SortedIteratingSystem(
        allOf(TransformComponent::class).oneOf(
                TextureComponent::class,
                LabelComponent::class,
                RectComponent::class,
                RenderableComponent::class
        ).get(),
        ZComparator()
) {
    private val camera = Jungle.instance.camera
    private val renderer = Jungle.instance.renderer
    private val batch = renderer.batch
    private val shapeRenderer = renderer.shapeRenderer
    private val transformMapper = mapperFor<TransformComponent>()
    private val textureMapper = mapperFor<TextureComponent>()
    private val labelMapper = mapperFor<LabelComponent>()
    private val rectMapper = mapperFor<RectComponent>()
    private val renderableMapper = mapperFor<RenderableComponent>()
    private val glyphLayout = GlyphLayout()
    private var layer = Layer.DEFAULT.value

    class ZComparator : Comparator<Entity> {
        private val transformMapper = mapperFor<TransformComponent>()
        override fun compare(p0: Entity?, p1: Entity?): Int {
            val z0 = transformMapper[p0].position.z
            val z1 = transformMapper[p1].position.z
            return if (z0 > z1) 1 else if (z0 < z1) -1 else 0
        }
    }

    override fun processEntity(entity: Entity?, deltaTime: Float) {
        val transform = transformMapper[entity]

        if (!transform.visible || transform.layer.value.and(layer) == 0) {
            return
        }

        val region = textureMapper[entity]?.region
        if (region != null) {
            val width = region.regionWidth.toFloat()
            val height = region.regionHeight.toFloat()

            val originX = width * transform.pivot.x
            val originY = height * transform.pivot.y

            renderer.begin(camera, RendererHelper.Type.SpriteBatch)
            batch.draw(
                    region,
                    transform.position.x - originX,
                    transform.position.y - originY,
                    originX,
                    originY,
                    width,
                    height,
                    transform.scale.x,
                    transform.scale.y,
                    transform.rotation
            )
            return
        }

        val label = labelMapper[entity]
        if (label != null) {
            val font = Marsh.Fonts[label.fontName]
            glyphLayout.setText(font, label.text, label.color, label.width, label.align, label.width > 0f)
            val originX = glyphLayout.width * transform.pivot.x
            val originY = glyphLayout.height * transform.pivot.y
            renderer.begin(camera, RendererHelper.Type.SpriteBatch)
            font.draw(batch, glyphLayout, transform.position.x - originX, transform.position.y - originY)
            return
        }

        val rect = rectMapper[entity]
        if (rect != null) {
            val originX = rect.width * transform.pivot.x
            val originY = rect.height * transform.pivot.y

            renderer.begin(camera, RendererHelper.Type.ShapeRenderer, rect.type)
            shapeRenderer.color = rect.color
            shapeRenderer.rect(
                    transform.position.x - originX,
                    transform.position.y - originY,
                    originX,
                    originY,
                    rect.width,
                    rect.height,
                    transform.scale.x,
                    transform.scale.y,
                    transform.rotation
            )
            return
        }

        renderableMapper[entity]?.render(transform)
    }

    fun setLayer(vararg layers: Layer) {
        layer = 0
        for (l in layers) {
            layer = layer.or(l.value)
        }
    }
}
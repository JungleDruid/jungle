package net.natruid.jungle.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import ktx.ashley.oneOf
import ktx.graphics.use
import net.natruid.jungle.components.LabelComponent
import net.natruid.jungle.components.RectComponent
import net.natruid.jungle.components.TextureComponent
import net.natruid.jungle.components.TransformComponent
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.core.Marsh
import net.natruid.jungle.utils.Layer

class RenderSystem(private val camera: Camera)
    : SortedIteratingSystem(
        allOf(TransformComponent::class).oneOf(
                TextureComponent::class,
                LabelComponent::class,
                RectComponent::class
        ).get(),
        ZComparator()
) {

    private val batch = Jungle.instance.batch
    private val transformMapper = mapperFor<TransformComponent>()
    private val textureMapper = mapperFor<TextureComponent>()
    private val labelMapper = mapperFor<LabelComponent>()
    private val rectMapper = mapperFor<RectComponent>()
    private val glyphLayout = GlyphLayout()
    private val shapeRenderer = ShapeRenderer()
    private var layer = Layer.DEFAULT.value

    class ZComparator : Comparator<Entity> {
        private val transformMapper = mapperFor<TransformComponent>()
        override fun compare(p0: Entity?, p1: Entity?): Int {
            val z0 = transformMapper[p0].position.z
            val z1 = transformMapper[p1].position.z
            return if (z0 > z1) 1 else if (z0 < z1) -1 else 0
        }
    }

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        shapeRenderer.setAutoShapeType(true)
    }

    override fun processEntity(entity: Entity?, deltaTime: Float) {
        val transform = transformMapper[entity]

        if (!transform.visible || transform.layer.value.and(layer) == 0) {
            return
        }

        val texture = textureMapper[entity]
        if (texture?.region != null) {
            val region = texture.region!!

            val width = region.regionWidth.toFloat()
            val height = region.regionHeight.toFloat()

            val originX = width * transform.pivot.x
            val originY = height * transform.pivot.y

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
        }

        val label = labelMapper[entity]
        if (label != null) {
            val font = Marsh.Fonts[label.fontName]
            glyphLayout.setText(font, label.text, label.color, label.width, label.align, label.width > 0f)
            val originX = glyphLayout.width * transform.pivot.x
            val originY = glyphLayout.height * transform.pivot.y
            font.draw(batch, glyphLayout, transform.position.x - originX, transform.position.y - originY)
        }

        val rect = rectMapper[entity]
        if (rect != null) {
            val originX = rect.width * transform.pivot.x
            val originY = rect.height * transform.pivot.y

            shapeRenderer.color = rect.color
            shapeRenderer.begin(rect.type)
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
            shapeRenderer.end()
        }
    }

    override fun update(deltaTime: Float) {
        batch.color = Color.WHITE
        batch.projectionMatrix = camera.combined
        batch.enableBlending()
        shapeRenderer.projectionMatrix = camera.combined
        batch.use {
            super.update(deltaTime)
        }
    }

    fun setLayer(vararg layers: Layer) {
        layer = 0
        for (l in layers) {
            layer = layer.or(l.value)
        }
    }
}
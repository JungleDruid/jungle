package net.natruid.jungle.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import ktx.graphics.use
import net.natruid.jungle.components.LabelComponent
import net.natruid.jungle.components.TextureComponent
import net.natruid.jungle.components.TransformComponent
import net.natruid.jungle.core.Data
import net.natruid.jungle.utils.Layer
import java.lang.Float.max
import java.lang.Float.min

class RenderSystem(private val batch: SpriteBatch)
    : SortedIteratingSystem(allOf(TransformComponent::class).get(), ZComparator()) {

    val camera = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
    var zoom: Float
        get() = camera.zoom
        set(value) {
            camera.zoom = max(0.2f, min(2f, value))
            camera.update()
        }

    private val transformMapper = mapperFor<TransformComponent>()
    private val textureMapper = mapperFor<TextureComponent>()
    private val labelMapper = mapperFor<LabelComponent>()
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
            val font = Data.Fonts[label.fontName]
            glyphLayout.setText(font, label.text, label.color, label.width, label.align, label.width > 0f)
            val originX = glyphLayout.width * transform.pivot.x
            val originY = glyphLayout.height * transform.pivot.y
            font.draw(batch, glyphLayout, transform.position.x - originX, transform.position.y - originY)
        }
    }

    override fun update(deltaTime: Float) {
        batch.color = Color.WHITE
        batch.projectionMatrix = camera.combined
        batch.enableBlending()
        batch.use {
            super.update(deltaTime)
        }
    }

    fun updateCamera(width: Int, height: Int) {
        camera.viewportWidth = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.update()
    }

    fun setLayer(vararg layers: Layer) {
        layer = 0
        for (l in layers) {
            layer = layer.or(l.value)
        }
    }
}
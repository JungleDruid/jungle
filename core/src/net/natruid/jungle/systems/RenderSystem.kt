package net.natruid.jungle.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.utils.Align
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
        CircleComponent::class,
        RenderableComponent::class
    ).get(),
    ZComparator()
) {
    private val camera = Jungle.instance.camera
    private val renderer = Jungle.instance.renderer
    private val batch = renderer.batch
    private val shapeRenderer = renderer.shapeRenderer
    private val transformMapper = mapperFor<TransformComponent>()
    private val shaderMapper = mapperFor<ShaderComponent>()
    private val shaderDefault = ShaderComponent()
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
        if (entity == null) return

        val transform = transformMapper[entity]

        if (!transform.visible || transform.layer.value.and(layer) == 0) {
            return
        }

        val shader = shaderMapper[entity] ?: shaderDefault

        for (component in entity.components) {
            when (component) {
                is TextureComponent -> {
                    component.region?.let { region ->
                        val width = region.regionWidth.toFloat()
                        val height = region.regionHeight.toFloat()

                        val originX = width * transform.pivot.x
                        val originY = height * transform.pivot.y

                        region.flip(region.isFlipX != component.flipX, region.isFlipY != component.flipY)
                        renderer.begin(camera, RendererHelper.Type.SPRITE_BATCH, shaderProgram = shader.shader)
                        batch.setBlendFunction(shader.blendSrcFunc, shader.blendDstFunc)
                        batch.color = component.color
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
                }
                is LabelComponent -> {
                    val font = Marsh.Fonts[component.fontName]
                    val hAlign = when {
                        component.align == Align.top -> Align.center
                        component.align == Align.bottom -> Align.center
                        else -> component.align
                    }
                    glyphLayout.setText(font, component.text, component.color, component.width, hAlign, component.width > 0f)
                    val offsetY: Float = when {
                        component.align.and(Align.top) != 0 -> 0f
                        component.align.and(Align.bottom) != 0 -> glyphLayout.height
                        else -> glyphLayout.height * transform.pivot.y
                    }
                    renderer.begin(camera, RendererHelper.Type.SPRITE_BATCH, shaderProgram = shader.shader)
                    batch.setBlendFunction(shader.blendSrcFunc, shader.blendDstFunc)
                    font.draw(batch, glyphLayout, transform.position.x, transform.position.y + offsetY)
                    batch.enableBlending()
                }
                is RectComponent -> {
                    val originX = component.width * transform.pivot.x
                    val originY = component.height * transform.pivot.y

                    renderer.begin(camera, RendererHelper.Type.SHAPE_RENDERER, component.type)
                    shapeRenderer.color = component.color
                    shapeRenderer.rect(
                        transform.position.x - originX,
                        transform.position.y - originY,
                        originX,
                        originY,
                        component.width,
                        component.height,
                        transform.scale.x,
                        transform.scale.y,
                        transform.rotation
                    )
                }
                is CircleComponent -> {
                    renderer.begin(camera, RendererHelper.Type.SHAPE_RENDERER, component.type)
                    shapeRenderer.color = component.color
                    shapeRenderer.circle(
                        transform.position.x,
                        transform.position.y,
                        component.radius,
                        1.coerceAtLeast((8 * Math.cbrt(component.radius.toDouble()) / camera.zoom).toInt())
                    )
                }
                is RenderableComponent -> component.render(transform)
            }
        }
    }

    fun setLayer(vararg layers: Layer) {
        layer = 0
        for (l in layers) {
            layer = layer.or(l.value)
        }
    }
}

package net.natruid.jungle.systems

import com.artemis.Aspect
import com.artemis.Component
import com.artemis.ComponentMapper
import com.artemis.utils.Bag
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.utils.Align
import net.natruid.jungle.components.*
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.core.Marsh
import net.natruid.jungle.utils.Layer
import net.natruid.jungle.utils.RendererHelper

class RenderSystem : SortedIteratingSystem(Aspect.all(TransformComponent::class.java).one(
    TextureComponent::class.java,
    LabelComponent::class.java,
    RectComponent::class.java,
    CircleComponent::class.java,
    RenderableComponent::class.java
)) {
    override val comparator by lazy {
        Comparator<Int> { p0, p1 ->
            val z0 = mTransform[p0].position.z
            val z1 = mTransform[p1].position.z
            when {
                z0 > z1 -> 1
                z0 < z1 -> -1
                else -> 0
            }
        }
    }

    private val camera = Jungle.instance.camera
    private val renderer = Jungle.instance.renderer
    private val batch = renderer.batch
    private val shapeRenderer = renderer.shapeRenderer
    private lateinit var mTransform: ComponentMapper<TransformComponent>
    private lateinit var mShader: ComponentMapper<ShaderComponent>
    private val shaderDefault = ShaderComponent()
    private val glyphLayout = GlyphLayout()
    private var layer = Layer.DEFAULT.value

    private val componentBag = Bag<Component>()

    override fun process(entityId: Int) {
        val transform = mTransform[entityId]

        if (!transform.visible || transform.layer.value.and(layer) == 0) {
            return
        }

        val shader = mShader[entityId] ?: shaderDefault

        for (component in world.componentManager.getComponentsFor(entityId, componentBag)) {
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

        componentBag.clear()
    }

    fun setLayer(vararg layers: Layer) {
        layer = 0
        for (l in layers) {
            layer = layer.or(l.value)
        }
    }
}

package net.natruid.jungle.systems.abstracts

import com.artemis.Aspect
import com.artemis.Component
import com.artemis.ComponentMapper
import com.artemis.utils.Bag
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Align
import net.natruid.jungle.components.*
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.core.Marsh
import net.natruid.jungle.utils.Layer
import net.natruid.jungle.utils.RendererType

abstract class AbstractRenderSystem(
    private val camera: OrthographicCamera,
    aspect: Aspect.Builder
) : SortedIteratingSystem(aspect) {
    override val comparator by lazy {
        fun compare(p0: Float, p1: Float): Int {
            return p0.compareTo(p1)
        }

        fun compare(p0: Int, p1: Int): Int {
            return p0.compareTo(p1)
        }

        Comparator<Int> { p0, p1 ->
            compare(mTransform[p0].z, mTransform[p1].z).let {
                if (it != 0) return@Comparator it
            }

            compare(
                mTexture[p0]?.region?.texture?.hashCode() ?: 0,
                mTexture[p1]?.region?.texture?.hashCode() ?: 0
            ).let {
                if (it != 0) return@Comparator it
            }

            compare(
                mShader[p0]?.hashCode() ?: 0,
                mShader[p1]?.hashCode() ?: 0
            ).let {
                if (it != 0) return@Comparator it
            }

            compare(
                mCrop[p0]?.rect?.hashCode() ?: 0,
                mCrop[p1]?.rect?.hashCode() ?: 0
            ).let {
                if (it != 0) return@Comparator it
            }

            0
        }
    }

    private val renderer = Jungle.instance.renderer
    private val batch = renderer.batch
    private val shapeRenderer = renderer.shapeRenderer
    private lateinit var mTransform: ComponentMapper<TransformComponent>
    private lateinit var mShader: ComponentMapper<ShaderComponent>
    private lateinit var mCrop: ComponentMapper<CropComponent>
    private lateinit var mTexture: ComponentMapper<TextureComponent>
    private val shaderDefault = ShaderComponent()
    private val glyphLayout = GlyphLayout()
    private var layer = Layer.DEFAULT.value
    private val position = Vector3()

    private val componentBag = Bag<Component>()

    open fun modifyPosition(position: Vector3) {}

    override fun process(entityId: Int) {
        try {
            val transform = mTransform[entityId]

            if (!transform.visible || transform.layer.value.and(layer) == 0) {
                return
            }

            val cShader = mShader[entityId] ?: shaderDefault
            val crop = mCrop[entityId]?.rect

            for (component in world.componentManager.getComponentsFor(entityId, componentBag)) {
                when (component) {
                    is TextureComponent -> {
                        component.region?.let { region ->
                            val width = region.regionWidth.toFloat()
                            val height = region.regionHeight.toFloat()

                            val originX = width * transform.pivot.x
                            val originY = height * transform.pivot.y

                            region.flip(region.isFlipX != component.flipX, region.isFlipY != component.flipY)
                            renderer.begin(
                                camera,
                                RendererType.SPRITE_BATCH,
                                shaderProgram = cShader.shader.program,
                                crop = crop
                            )
                            batch.setBlendFunction(cShader.blendSrcFunc, cShader.blendDstFunc)
                            batch.color = component.color
                            modifyPosition(position.set(
                                transform.position.x - originX,
                                transform.position.y - originY,
                                0f
                            ))
                            batch.draw(
                                region,
                                position.x,
                                position.y,
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
                        renderer.begin(
                            camera,
                            RendererType.SPRITE_BATCH,
                            shaderProgram = cShader.shader.program,
                            crop = crop
                        )
                        batch.setBlendFunction(cShader.blendSrcFunc, cShader.blendDstFunc)
                        modifyPosition(position.set(
                            transform.position.x,
                            transform.position.y,
                            0f
                        ))
                        font.draw(batch, glyphLayout, position.x, position.y + offsetY)
                    }
                    is RectComponent -> {
                        val originX = component.width * transform.pivot.x
                        val originY = component.height * transform.pivot.y

                        renderer.begin(
                            camera,
                            RendererType.SHAPE_RENDERER,
                            component.type,
                            crop = crop
                        )
                        shapeRenderer.color = component.color
                        modifyPosition(position.set(
                            transform.position.x - originX,
                            transform.position.y - originY,
                            0f
                        ))
                        shapeRenderer.rect(
                            position.x,
                            position.y,
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
                        renderer.begin(
                            camera,
                            RendererType.SHAPE_RENDERER,
                            component.type,
                            crop = crop
                        )
                        shapeRenderer.color = component.color
                        modifyPosition(position.set(
                            transform.position.x,
                            transform.position.y,
                            0f
                        ))
                        shapeRenderer.circle(
                            position.x,
                            position.y,
                            component.radius,
                            1.coerceAtLeast((8 * Math.cbrt(component.radius.toDouble()) / camera.zoom).toInt())
                        )
                    }
                    is RenderableComponent -> component.render(transform)
                }
            }

            componentBag.clear()
        } catch (e: Exception) {
        }
    }

    fun setLayer(vararg layers: Layer) {
        layer = 0
        for (l in layers) {
            layer = layer.or(l.value)
        }
    }
}

package net.natruid.jungle.systems.render

import com.artemis.Aspect
import com.artemis.BaseEntitySystem
import com.artemis.ComponentMapper
import com.badlogic.gdx.math.Vector3
import net.natruid.jungle.components.render.*
import net.natruid.jungle.systems.CameraSystem
import net.natruid.jungle.utils.RendererType

abstract class RenderSystem(
    aspect: Aspect.Builder,
    private val rendererType: RendererType = RendererType.SPRITE_BATCH
) : BaseEntitySystem(
    aspect.all(RenderComponent::class.java).exclude(InvisibleComponent::class.java)
) {
    private lateinit var renderBatchSystem: RenderBatchSystem
    private lateinit var cameraSystem: CameraSystem

    private lateinit var mPos: ComponentMapper<PosComponent>
    private lateinit var mUI: ComponentMapper<UIComponent>
    private lateinit var mShader: ComponentMapper<ShaderComponent>
    private lateinit var mCrop: ComponentMapper<CropComponent>

    protected val renderer by lazy { renderBatchSystem.renderer }

    private val position = Vector3()
    private var current = -1

    open val zOverride: Float? = null

    protected val camera
        get() = if (mUI.has(current)) cameraSystem.uiCamera else cameraSystem.camera

    override fun initialize() {
        super.initialize()
        isEnabled = false
    }

    private fun renderBegin() {
        if (rendererType == RendererType.NONE) return

        if (rendererType == RendererType.SPRITE_BATCH) {
            val shaderComponent = mShader.getSafe(current, ShaderComponent.DEFAULT)
            val program = shaderComponent.shader.program
            val batch = renderer.batch
            if (batch.shader != program) {
                batch.shader = program
            }
            batch.setBlendFunction(shaderComponent.blendSrcFunc, shaderComponent.blendDstFunc)
        }

        renderer.begin(camera, rendererType, mCrop[current]?.rect)
    }

    protected fun getPos(offsetX: Float = 0f, offsetY: Float = 0f, entityId: Int = current): Vector3 {
        val xy = mPos.getSafe(entityId, PosComponent.DEFAULT).xy
        position.set(xy.x + offsetX, xy.y + offsetY, 0f)
        if (mUI.has(entityId)) {
            cameraSystem.camera.project(position)
        }
        return position
    }

    fun process(e: Int) {
        current = e
        renderBegin()
        render(e)
    }

    override fun inserted(entityId: Int) {
        super.inserted(entityId)
        renderBatchSystem.registerAgent(entityId, this)
    }

    override fun removed(entityId: Int) {
        renderBatchSystem.unregisterAgent(entityId, this)
        super.removed(entityId)
    }

    override fun processSystem() {}

    abstract fun render(entityId: Int)
}

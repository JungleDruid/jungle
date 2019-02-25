package net.natruid.jungle.systems

import com.artemis.Aspect
import com.badlogic.gdx.math.Vector3
import net.natruid.jungle.components.*
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.systems.abstracts.AbstractRenderSystem

class UIRenderSystem : AbstractRenderSystem(
    Jungle.instance.uiCamera,
    Aspect.all(TransformComponent::class.java, UIComponent::class.java).one(
        TextureComponent::class.java,
        LabelComponent::class.java,
        RectComponent::class.java,
        CircleComponent::class.java,
        RenderableComponent::class.java
    )
) {
    private val gameCamera = Jungle.instance.camera

    override fun modifyPosition(position: Vector3) {
        gameCamera.project(position)
    }
}

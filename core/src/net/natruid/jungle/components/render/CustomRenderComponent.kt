package net.natruid.jungle.components.render

import com.artemis.Component
import net.natruid.jungle.utils.RendererHelper

class CustomRenderComponent : Component() {
    var renderCallback: ((renderer: RendererHelper) -> Unit)? = null
}

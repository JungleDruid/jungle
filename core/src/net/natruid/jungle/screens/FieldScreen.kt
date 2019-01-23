package net.natruid.jungle.screens

import net.natruid.jungle.systems.CameraMovementSystem
import net.natruid.jungle.systems.GridRenderSystem

class FieldScreen : AbstractScreen() {
    init {
        engine.addSystem(CameraMovementSystem(camera))
        engine.addSystem(GridRenderSystem(camera))
    }
}
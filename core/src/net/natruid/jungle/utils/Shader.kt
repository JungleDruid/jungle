package net.natruid.jungle.utils

import com.badlogic.gdx.graphics.glutils.ShaderProgram
import ktx.assets.disposeSafely

class Shader(vertex: String = DEFAULT_NAME, fragment: String = DEFAULT_NAME) {
    companion object {
        private const val DEFAULT_NAME = "default"
        private val defaultPair = Pair(DEFAULT_NAME, DEFAULT_NAME)
        private var resetting = false
        private var index = 0
        private val shaderProgramsArray = Array(2) { HashMap<Pair<String, String>, ExposedShaderProgram>() }
        private val shaderPrograms get() = shaderProgramsArray[index]
        private fun reset() {
            if (resetting) return
            resetting = true
            index = 1 - index
        }

        private fun restore() {
            if (!resetting) return
            resetting = false
            shaderProgramsArray[1 - index].let {
                it.forEach { _, program ->
                    program.disposeSafely()
                }
                it.clear()
            }
        }

        val defaultShader = Shader()

        val defaultShaderProgram: ShaderProgram
            get() {
                var program = shaderPrograms[defaultPair]
                if (program == null) {
                    program = ExposedShaderProgram(defaultPair)
                    shaderPrograms[defaultPair] = program
                }
                return program
            }
    }

    private var source: ExposedShaderProgram? = null
    private var instance: ExposedShaderProgram? = null
    private var resettingInstance = false
    private var pair = Pair(vertex, fragment)
    val program: ShaderProgram
        get() {
            restore()
            resettingInstance = false
            if (source!!.disposed) {
                initSource()
            }
            return instance ?: source!!
        }

    init {
        initSource()
    }

    private fun initSource() {
        var shaderProgram = shaderPrograms[pair]
        if (shaderProgram == null) {
            shaderProgram = createShaderProgram()
            source?.let {
                if (it.pair == pair) shaderProgram.copy(it)
            }
            shaderPrograms[pair] = shaderProgram
        }
        source = shaderProgram
    }

    private fun createShaderProgram(): ExposedShaderProgram {
        return ExposedShaderProgram(pair)
    }

    fun setProgram(vertex: String = DEFAULT_NAME, fragment: String = DEFAULT_NAME) {
        removeInstance()
        pair = Pair(vertex, fragment)
        initSource()
    }

    fun getInstance(): ShaderProgram {
        if (instance == null) {
            instance = createShaderProgram().apply {
                copy(source!!)
            }
        }
        return program
    }

    fun removeInstance() {
        instance?.disposeSafely()
        instance = null
        resettingInstance = false
    }

    fun reset() {
        Shader.reset()
        initSource()
        if (!resettingInstance) instance?.let { oldInstance ->
            resettingInstance = true
            instance = createShaderProgram().apply {
                copy(oldInstance)
            }
            oldInstance.disposeSafely()
        }
    }
}

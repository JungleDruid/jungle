package net.natruid.jungle.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.BufferUtils
import ktx.graphics.use

class ExposedShaderProgram(val pair: Pair<String, String>) : ShaderProgram(
    Scout["assets/shaders/${pair.first}Vertex.glsl"],
    Scout["assets/shaders/${pair.second}Fragment.glsl"]
) {

    companion object {
        private val uniformTypeMap = HashMap<Pair<String, String>, HashMap<Int, ShaderUniformType>>()
    }

    var program = 0
        private set
    var disposed = false

    override fun createProgram(): Int {
        program = super.createProgram()
        return program
    }

    private val floatBuffer = BufferUtils.newFloatBuffer(16)
    private val intBuffer = BufferUtils.newIntBuffer(16)
    fun copy(other: ExposedShaderProgram) {
        if (pair != other.pair) error("Unmatched pair: $pair and ${other.pair}")
        uniformTypeMap[pair]?.apply {
            val gl = Gdx.gl
            use {
                forEach { location, type ->
                    when {
                        type.value == 0 -> return@forEach
                        type.value < 9 -> {
                            floatBuffer.clear()
                            gl.glGetUniformfv(other.program, location, floatBuffer)
                        }
                        else -> {
                            intBuffer.clear()
                            gl.glGetUniformiv(other.program, location, intBuffer)
                        }
                    }
                    when (type) {
                        ShaderUniformType.UNIFORM1F ->
                            gl.glUniform1f(location, floatBuffer.get())
                        ShaderUniformType.UNIFORM2F ->
                            gl.glUniform2f(location, floatBuffer.get(), floatBuffer.get())
                        ShaderUniformType.UNIFORM3F ->
                            gl.glUniform3f(location, floatBuffer.get(), floatBuffer.get(), floatBuffer.get())
                        ShaderUniformType.UNIFORM4F ->
                            gl.glUniform4f(location, floatBuffer.get(), floatBuffer.get(), floatBuffer.get(), floatBuffer.get())
                        ShaderUniformType.UNIFORM1FV ->
                            gl.glUniform1fv(location, 0, floatBuffer)
                        ShaderUniformType.UNIFORM2FV ->
                            gl.glUniform2fv(location, 0, floatBuffer)
                        ShaderUniformType.UNIFORM3FV ->
                            gl.glUniform3fv(location, 0, floatBuffer)
                        ShaderUniformType.UNIFORM4FV ->
                            gl.glUniform4fv(location, 0, floatBuffer)
                        ShaderUniformType.UNIFORM1I ->
                            gl.glUniform1i(location, intBuffer.get())
                        ShaderUniformType.UNIFORM2I ->
                            gl.glUniform2i(location, intBuffer.get(), intBuffer.get())
                        ShaderUniformType.UNIFORM3I ->
                            gl.glUniform3i(location, intBuffer.get(), intBuffer.get(), intBuffer.get())
                        ShaderUniformType.UNIFORM4I ->
                            gl.glUniform4i(location, intBuffer.get(), intBuffer.get(), intBuffer.get(), intBuffer.get())
                        ShaderUniformType.UNKNOWN -> {
                        }
                    }
                }
            }
        }
    }

    private fun setType(location: Int, type: ShaderUniformType) {
        var map = uniformTypeMap[pair]
        if (map == null) {
            map = HashMap()
            uniformTypeMap[pair] = map
        }
        map[location] = type
    }

    override fun setUniformf(name: String, value: Float) {
        setUniformf(getUniformLocation(name), value)
    }

    override fun setUniformf(location: Int, value: Float) {
        super.setUniformf(location, value)
        setType(location, ShaderUniformType.UNIFORM1F)
    }

    override fun setUniformf(name: String, value1: Float, value2: Float) {
        setUniformf(getUniformLocation(name), value1, value2)
    }

    override fun setUniformf(location: Int, value1: Float, value2: Float) {
        super.setUniformf(location, value1, value2)
        setType(location, ShaderUniformType.UNIFORM2F)
    }

    override fun setUniformf(name: String, value1: Float, value2: Float, value3: Float) {
        setUniformf(getUniformLocation(name), value1, value2, value3)
    }

    override fun setUniformf(location: Int, value1: Float, value2: Float, value3: Float) {
        super.setUniformf(location, value1, value2, value3)
        setType(location, ShaderUniformType.UNIFORM3F)
    }

    override fun setUniformf(name: String, value1: Float, value2: Float, value3: Float, value4: Float) {
        setUniformf(getUniformLocation(name), value1, value2, value3, value4)
    }

    override fun setUniformf(location: Int, value1: Float, value2: Float, value3: Float, value4: Float) {
        super.setUniformf(location, value1, value2, value3, value4)
        setType(location, ShaderUniformType.UNIFORM4F)
    }

    override fun setUniform1fv(name: String, values: FloatArray, offset: Int, length: Int) {
        setUniform1fv(getUniformLocation(name), values, offset, length)
    }

    override fun setUniform1fv(location: Int, values: FloatArray, offset: Int, length: Int) {
        super.setUniform1fv(location, values, offset, length)
        setType(location, ShaderUniformType.UNIFORM1FV)
    }

    override fun setUniform2fv(name: String, values: FloatArray, offset: Int, length: Int) {
        setUniform2fv(getUniformLocation(name), values, offset, length)
    }

    override fun setUniform2fv(location: Int, values: FloatArray, offset: Int, length: Int) {
        super.setUniform2fv(location, values, offset, length)
        setType(location, ShaderUniformType.UNIFORM2FV)
    }

    override fun setUniform3fv(name: String?, values: FloatArray?, offset: Int, length: Int) {
        setUniform3fv(getUniformLocation(name), values, offset, length)
    }

    override fun setUniform3fv(location: Int, values: FloatArray?, offset: Int, length: Int) {
        super.setUniform3fv(location, values, offset, length)
        setType(location, ShaderUniformType.UNIFORM3FV)
    }

    override fun setUniform4fv(name: String, values: FloatArray, offset: Int, length: Int) {
        setUniform4fv(getUniformLocation(name), values, offset, length)
    }

    override fun setUniform4fv(location: Int, values: FloatArray, offset: Int, length: Int) {
        super.setUniform4fv(location, values, offset, length)
        setType(location, ShaderUniformType.UNIFORM4FV)
    }

    override fun setUniformi(name: String, value: Int) {
        setUniformi(getUniformLocation(name), value)
    }

    override fun setUniformi(location: Int, value: Int) {
        super.setUniformi(location, value)
        setType(location, ShaderUniformType.UNIFORM1I)
    }

    override fun setUniformi(name: String, value1: Int, value2: Int) {
        setUniformi(getUniformLocation(name), value1, value2)
    }

    override fun setUniformi(location: Int, value1: Int, value2: Int) {
        super.setUniformi(location, value1, value2)
        setType(location, ShaderUniformType.UNIFORM2I)
    }

    override fun setUniformi(name: String, value1: Int, value2: Int, value3: Int) {
        setUniformi(getUniformLocation(name), value1, value2, value3)
    }

    override fun setUniformi(location: Int, value1: Int, value2: Int, value3: Int) {
        super.setUniformi(location, value1, value2, value3)
        setType(location, ShaderUniformType.UNIFORM3I)
    }

    override fun setUniformi(name: String, value1: Int, value2: Int, value3: Int, value4: Int) {
        setUniformi(getUniformLocation(name), value1, value2, value3, value4)
    }

    override fun setUniformi(location: Int, value1: Int, value2: Int, value3: Int, value4: Int) {
        super.setUniformi(location, value1, value2, value3, value4)
        setType(location, ShaderUniformType.UNIFORM4I)
    }

    override fun dispose() {
        super.dispose()
        disposed = true
    }
}

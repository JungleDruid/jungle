/**
 * File Handle Helper
 */
package net.natruid.jungle.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import java.io.File

object Scout {
    private val assetPath: String

    init {
        val jarFile = File(this::class.java.protectionDomain.codeSource.location.toURI())
        var jarPath = jarFile.path
        val workPath = System.getProperty("user.dir")
        assetPath = if (jarFile.isDirectory) {
            workPath
        } else {
            jarPath = jarPath.substring(0, jarPath.lastIndexOf(jarFile.name) - 1)
            if (jarPath.length != workPath.length && jarPath.indexOf(workPath) >= 0) {
                jarPath.substring(workPath.length + 1)
            } else {
                jarPath
            }
        }
    }

    operator fun get(path: String): FileHandle {
        return Gdx.files.internal("$assetPath/$path")
    }
}
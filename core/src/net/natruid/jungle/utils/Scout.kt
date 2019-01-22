/**
 * File Handle Helper
 */
package net.natruid.jungle.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import java.io.File
import java.util.zip.ZipFile

object Scout {
    private val assetPath: String
    private val zipPath: String
    private val hasAssetDir: Boolean

    init {
        val jarFile = File(this::class.java.protectionDomain.codeSource.location.toURI())
        var jarPath = jarFile.path.replace('\\', '/')
        val workPath = System.getProperty("user.dir").replace('\\', '/')
        assetPath = if (jarFile.isDirectory) {
            "$workPath/"
        } else {
            jarPath = jarPath.substring(0, jarPath.lastIndexOf(jarFile.name) - 1)
            when {
                jarPath == workPath -> ""
                jarPath.indexOf(workPath) >= 0 -> jarPath.substring(workPath.length + 1) + "/"
                else -> "$jarPath/"
            }
        }
        val zip = assetPath + "assets.zip"
        zipPath = if (Gdx.files.internal(zip).exists()) zip else ""
        val asset = Gdx.files.internal(assetPath + "assets")
        hasAssetDir = asset.exists() && asset.isDirectory
    }

    operator fun get(path: String, useZip: Boolean = false): FileHandle {
        return when {
            hasAssetDir && !useZip -> {
                val file = Gdx.files.internal(when {
                    assetPath.isEmpty() || path[0] == '/' || path[1] == ':' -> path
                    else -> assetPath + path
                })

                if (file.exists() || file.path().indexOf("assets/locale/") >= 0) {
                    file
                } else {
                    get(path, true)
                }
            }
            !zipPath.isEmpty() -> {
                ArchiveFileHandle(ZipFile(zipPath), path)
            }
            else -> {
                error("[Error] Cannot find assets.zip or assets folder.")
            }
        }
    }
}
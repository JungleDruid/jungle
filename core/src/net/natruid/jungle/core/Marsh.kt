/**
 * Data Manager
 */
package net.natruid.jungle.core

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonReader
import ktx.freetype.generateFont
import net.natruid.jungle.utils.Scout

object Marsh {
    class FontDef {
        val name: String = ""
        val file: String = ""
        val size: Int = 0
    }

    private val fontDefs = ArrayList<FontDef>()
    private val fontGeneratorMap = HashMap<String, FreeTypeFontGenerator>()
    private var fonts = HashMap<String, BitmapFont>()

    object Fonts {
        operator fun get(key: String): BitmapFont {
            if (!fonts.containsKey(key)) {
                println("[Warning] Cannot find font name: $key")
                return fonts["normal"]!!
            }

            return fonts[key]!!
        }
    }

    fun load() {
        val jsonFiles = getFiles("assets/data/", "json", true)
        val json = Json()

        for (f in jsonFiles) {
            readJson(json, f)
        }
    }

    fun getFontDefs(): List<FontDef> {
        return fontDefs.toList()
    }

    private fun getFiles(path: String, ext: String, recursive: Boolean, list: ArrayList<FileHandle> = ArrayList())
            : List<FileHandle> {

        val dir = Scout[path]

        if (!dir.isDirectory) {
            error("[Marsh] getFiles: $path is not a directory.")
        }

        for (f in dir.list()) {
            if (f.isDirectory && recursive) {
                getFiles(f.path(), ext, true, list)
            } else if (f.extension() == ext) {
                list.add(f)
            }
        }

        return list
    }

    private fun readJson(json: Json, file: FileHandle) {
        val root = JsonReader().parse(file)
        if (!root.isObject) {
            error("[Marsh] readJson: ${file.path()} is not a json object.")
        }

        for (j in root) {
            when (j.name) {
                "fonts" -> {
                    for (fontDefJson in j) {
                        val def = json.readValue(FontDef::class.java, fontDefJson)
                        val fontPath = "assets/fonts/" + def.file
                        var generator = fontGeneratorMap[fontPath]
                        if (generator == null) {
                            generator = FreeTypeFontGenerator(Scout[fontPath])
                            fontGeneratorMap[fontPath] = generator
                        }

                        val font = generator.generateFont {
                            incremental = true
                            size = def.size
                            minFilter = Texture.TextureFilter.Linear
                            magFilter = Texture.TextureFilter.Linear
                            borderWidth = 1f
                        }

                        fonts[def.name] = font
                        fontDefs.add(def)
                    }
                }
                else -> println("[Warning] Found unknown property ${j.name} in ${file.name()}")
            }
        }
    }
}
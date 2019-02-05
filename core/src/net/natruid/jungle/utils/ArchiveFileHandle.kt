package net.natruid.jungle.utils

import com.badlogic.gdx.Files.FileType
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.GdxRuntimeException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ArchiveFileHandle : FileHandle {
    private val archive: ZipFile
    private val archiveEntry: ZipEntry?

    constructor(archive: ZipFile, file: File) : super(file, FileType.Classpath) {
        this.archive = archive
        archiveEntry = this.archive.getEntry(file.path)
    }

    constructor(archive: ZipFile, fileName: String) : super(fileName.replace('\\', '/'), FileType.Classpath) {
        this.archive = archive
        this.archiveEntry = archive.getEntry(fileName.replace('\\', '/'))
    }

    override fun child(name: String): FileHandle {
        val n = name.replace('\\', '/')
        return if (file.path.isEmpty()) ArchiveFileHandle(archive, File(n)) else ArchiveFileHandle(archive, File(file, n))
    }

    override fun sibling(name: String): FileHandle {
        val n = name.replace('\\', '/')
        if (file.path.isEmpty()) throw GdxRuntimeException("Cannot get the sibling of the root.")
        return ArchiveFileHandle(archive, File(file.parent, n))
    }

    override fun parent(): FileHandle {
        var parent: File? = file.parentFile
        if (parent == null) {
            parent = if (type == FileType.Absolute)
                File("/")
            else
                File("")
        }
        return ArchiveFileHandle(archive, parent)
    }

    override fun read(): InputStream {
        try {
            return archive.getInputStream(archiveEntry!!)
        } catch (e: IOException) {
            throw GdxRuntimeException("File not found: $file (Archive)")
        }

    }

    override fun exists(): Boolean {
        return archiveEntry != null
    }

    override fun length(): Long {
        return archiveEntry!!.size
    }

    override fun lastModified(): Long {
        return archiveEntry!!.time
    }

    override fun isDirectory(): Boolean {
        return archiveEntry!!.isDirectory
    }

    override fun list(): Array<FileHandle> {
        val list = ArrayList<FileHandle>()
        for (f in archive.entries()) {
            if (f.name.indexOf(archiveEntry!!.name) == 0
                && f.name.length > archiveEntry.name.length
                && f.name.substring(archiveEntry.name.length + 1, f.name.length - 1).indexOf('/') < 0) {
                list.add(ArchiveFileHandle(archive, f.name))
            }
        }

        return list.toTypedArray()
    }
}

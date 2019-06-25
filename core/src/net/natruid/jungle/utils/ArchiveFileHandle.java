package net.natruid.jungle.utils;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ArchiveFileHandle extends FileHandle {
    private final ZipFile archive;
    private final ZipEntry archiveEntry;

    public ArchiveFileHandle(ZipFile archive, File file) {
        super(file, FileType.Classpath);
        this.archive = archive;
        String n = file.getPath().replace('\\', '/');
        archiveEntry = this.archive.getEntry(n);
    }

    public ArchiveFileHandle(ZipFile archive, String fileName) {
        super(fileName.replace('\\', '/'), FileType.Classpath);
        this.archive = archive;
        archiveEntry = archive.getEntry(fileName.replace('\\', '/'));
    }

    public FileHandle child(String name) {
        String n = name.replace('\\', '/');
        return file.getPath().isEmpty()
            ? new ArchiveFileHandle(archive, new File(n))
            : new ArchiveFileHandle(archive, new File(file, n));
    }


    public FileHandle sibling(String name) {
        String n = name.replace('\\', '/');
        if (file.getPath().isEmpty()) throw new GdxRuntimeException("Cannot get the sibling of the root.");
        String parent = file.getParent().replace('\\', '/');
        return new ArchiveFileHandle(archive, new File(parent, n));
    }


    public FileHandle parent() {
        File parent = file.getParentFile();
        if (parent == null) {
            parent = type == FileType.Absolute
                ? new File("/")
                : new File("");
        }
        return new ArchiveFileHandle(archive, parent);
    }


    public InputStream read() {
        try {
            return archive.getInputStream(archiveEntry);
        } catch (IOException e) {
            throw new GdxRuntimeException("File not found: " + file + " (Archive)");
        }
    }

    public boolean exists() {
        return archiveEntry != null;
    }

    public long length() {
        return archiveEntry.getSize();
    }

    public long lastModified() {
        return archiveEntry.getTime();
    }

    public boolean isDirectory() {
        return archiveEntry.isDirectory();
    }


    public FileHandle[] list() {
        Array<FileHandle> list = new Array<>();
        Enumeration<? extends ZipEntry> entries = archive.entries();
        while (entries.hasMoreElements()) {
            ZipEntry f = entries.nextElement();
            if (f.getName().indexOf(archiveEntry.getName()) == 0
                && f.getName().length() > archiveEntry.getName().length()
                && f.getName().substring(archiveEntry.getName().length() + 1, f.getName().length() - 1).indexOf('/') < 0) {
                list.add(new ArchiveFileHandle(archive, f.getName()));
            }
        }

        return list.toArray(FileHandle.class);
    }
}


package net.natruid.jungle.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.zip.ZipFile;

public final class Scout {
    private static final String assetPath;
    private static final String zipPath;
    private static final boolean hasAssetDir;

    static {
        File jarFile = null;
        try {
            jarFile = new File(Scout.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        assert jarFile != null;
        String jarPath = jarFile.getPath().replace('\\', '/');
        String workPath = System.getProperty("user.dir").replace('\\', '/');
        Logger.info("Working dir: " + workPath);
        Logger.info("Jar file: " + jarFile);
        if (Gdx.files.internal(workPath + "/assets").exists()) {
            assetPath = workPath + "/";
        } else {
            jarPath = jarPath.substring(0, jarPath.lastIndexOf(jarFile.getName()) - 1);
            if (jarPath.equals(workPath)) {
                assetPath = "";
            } else if (jarPath.contains(workPath)) {
                assetPath = jarPath.substring(workPath.length() + 1) + "/";
            } else {
                assetPath = jarPath + "/";
            }
        }
        String zip = assetPath + "assets.zip";
        zipPath = Gdx.files.internal(zip).exists() ? zip : "";
        FileHandle asset = Gdx.files.internal(assetPath + "assets");
        hasAssetDir = asset.exists() && asset.isDirectory();
        Logger.info(String.format("Assets path: %s", hasAssetDir ? asset.path() : zipPath));
    }

    public static FileHandle get(String path) {
        return get(path, false);
    }

    public static FileHandle get(String path, boolean useZip) {
        FileHandle ret = null;
        if (hasAssetDir && !useZip) {
            FileHandle file = Gdx.files.internal(
                (assetPath.isEmpty() || path.charAt(0) == '/' || path.charAt(1) == ':')
                    ? path
                    : assetPath + path
            );

            if (file.exists() || file.path().contains("assets/locale/")) {
                ret = file;
            } else {
                ret = get(path, true);
            }
        } else if (!zipPath.isEmpty()) {
            try {
                ret = new ArchiveFileHandle(new ZipFile(zipPath), path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("Cannot find " + path + " in assets.zip or assets folder.");
        }

        return ret;
    }
}

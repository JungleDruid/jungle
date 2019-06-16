package net.natruid.jungle.data;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import net.natruid.jungle.core.Sky;
import net.natruid.jungle.utils.skill.Proficiency;
import net.natruid.jungle.utils.skill.Skill;
import net.natruid.jungle.utils.types.StatType;

import java.util.Arrays;

public class Marsh {
    public final FontData font = new FontData();
    public final LocaleData locale = new LocaleData();

    private final StatDef[] statDefs = new StatDef[StatType.length];
    private final ObjectMap<String, Proficiency> proficiencyMap = new ObjectMap<>();
    private final ObjectMap<String, Skill> skillMap = new ObjectMap<>();

    public Marsh() {
        Arrays.fill(statDefs, StatDef.NONE);
    }

    public StatDef getStatDef(int index) {
        return statDefs[index];
    }

    public Proficiency getProficiency(String key) {
        return proficiencyMap.get(key);
    }

    public Skill getSkill(String key) {
        return skillMap.get(key);
    }

    public void load() {
        FileHandle[] jsonFiles = getFiles("assets/data/", "json", true);
        Json json = new Json();

        for (FileHandle f : jsonFiles) {
            readJson(json, f);
        }
    }

    private FileHandle[] getFiles(String path, String ext, Boolean recursive) {
        Array<FileHandle> files = new Array<>();
        getFilesImpl(path, ext, recursive, files, false);
        try {
            getFilesImpl(path, ext, recursive, files, true);
        } catch (Exception ignore) {
        }

        return files.toArray(FileHandle.class);
    }

    private void getFilesImpl(String path, String ext, Boolean recursive, Array<FileHandle> files, boolean useZip) {
        FileHandle dir = Sky.scout.locate(path, useZip);

        if (!dir.isDirectory()) {
            throw new RuntimeException("$path is not a directory.");
        }

        for (FileHandle f : dir.list()) {
            if (f.isDirectory() && recursive) {
                getFilesImpl(f.path(), ext, true, files, useZip);
            } else if (f.extension().equals(ext)) {
                files.add(f);
            }
        }
    }

    private void readJson(Json json, FileHandle file) {
        JsonValue root = new JsonReader().parse(file);
        if (!root.isObject()) {
            throw new RuntimeException("${file.path()} is not a json object.");
        }

        for (JsonValue j : root) {
            switch (j.name) {
                case "fonts":
                    font.readJson(json, j);
                    break;
                case "stats":
                    for (JsonValue statDefJson : j) {
                        StatDef def = json.readValue(StatDef.class, statDefJson);
                        StatType stat = StatType.valueOf(statDefJson.name.toUpperCase());
                        statDefs[stat.ordinal()] = def;
                    }
                    break;
                case "proficiencies":
                    for (JsonValue next : j) {
                        String name = next.asString();
                        proficiencyMap.put(name, new Proficiency(name));
                    }
                    break;
                case "skills":
                    for (JsonValue next : j) {
                        Skill skill = json.readValue(Skill.class, next);
                        skillMap.put(skill.name, skill);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}

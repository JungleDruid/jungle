package net.natruid.jungle.data;

import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ObjectMap;
import net.natruid.jungle.core.Sky;

public class LocaleData {
    private final ObjectMap<String, I18NBundle> i18nMap = new ObjectMap<>();

    public I18NBundle get(String key) {
        I18NBundle ret = i18nMap.get(key);
        if (ret == null) {
            ret = I18NBundle.createBundle(Sky.scout.locate(key));
            if (ret == null) throw new RuntimeException("Cannot find bundle " + key);
            i18nMap.put(key, ret);
        }

        return ret;
    }
}

package net.natruid.jungle.utils;

import com.badlogic.gdx.utils.Disposable;

public final class Utils {
    public static void safelyDispose(Disposable disposable) {
        if (disposable != null) {
            try {
                disposable.dispose();
            } catch (Exception ignore) {
            }
        }
    }
}

package net.natruid.jungle.utils;

import java.util.Random;

public class Fate {
    private static Random instance;

    public static Random getInstance() {
        if (instance == null) {
            instance = new Random();
        }

        return instance;
    }
}

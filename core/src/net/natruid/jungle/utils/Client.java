package net.natruid.jungle.utils;

public interface Client {
    boolean resize(int width, int height);
    boolean setTitle(String title);
    boolean init();
    boolean isFocused();
}

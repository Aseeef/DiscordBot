package net.grandtheftmc.discordbot.utils;

@FunctionalInterface
public interface Callback<T> {
    void callback(T callback);
}

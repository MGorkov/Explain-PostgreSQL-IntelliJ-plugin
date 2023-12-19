package com.mgorkov.settings;

public interface Subscriber {
    void handleSettings(String key, String value);
}

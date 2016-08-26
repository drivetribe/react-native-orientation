package com.github.yamill.orientation;

import android.content.res.Configuration;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationChangeManager {

    private static ConfigurationChangeManager instance = null;

    private List<ConfigurationChangeListener> listeners = new ArrayList<>();

    public static ConfigurationChangeManager getInstance() {
        if (instance  == null) {
            instance = new ConfigurationChangeManager();
        }
        return instance;
    }

    private ConfigurationChangeManager() {
    }

    void addListener(ConfigurationChangeListener listener) {
        listeners.add(listener);
    }

    void removeListener(ConfigurationChangeListener listener) {
        listeners.remove(listener);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onConfigurationChange(newConfig);
        }
    }

}

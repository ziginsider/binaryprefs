package com.ironz.binaryprefs.event;

import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main thread preference change listener bridge
 */
public final class MainThreadEventBridgeImpl implements EventBridge {

    private final List<OnSharedPreferenceChangeListener> currentListeners;

    private final Handler handler = new Handler();

    public MainThreadEventBridgeImpl(String prefName, Map<String, List<OnSharedPreferenceChangeListener>> allListeners) {
        this.currentListeners = putIfAbsentListeners(prefName, allListeners);
    }

    private List<OnSharedPreferenceChangeListener> putIfAbsentListeners(String prefName, Map<String, List<OnSharedPreferenceChangeListener>> allListeners) {
        if (allListeners.containsKey(prefName)) {
            return allListeners.get(prefName);
        }
        List<OnSharedPreferenceChangeListener> listeners = new ArrayList<>();
        allListeners.put(prefName, listeners);
        return listeners;
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        currentListeners.add(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        currentListeners.remove(listener);
    }

    @Override
    public void notifyListenersUpdate(final String key, byte[] bytes) {
        notifyListeners(key);
    }

    @Override
    public void notifyListenersRemove(String key) {
        notifyListeners(key);
    }

    private void notifyListeners(final String key) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (OnSharedPreferenceChangeListener listener : currentListeners) {
                    listener.onSharedPreferenceChanged(null, key);
                }
            }
        });
    }
}
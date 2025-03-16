package com.gungens.serverAnalytics;

import com.gungens.serverAnalytics.listeners.JoinListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public final class ServerAnalytics extends JavaPlugin {

    @Override
    public void onEnable() {
        registerListeners(getServer().getPluginManager(),
                new JoinListener()
        );
    }
    public void registerListeners(PluginManager pm, Listener... listeners) {
        for (Listener listener : listeners) {
            pm.registerEvents(listener, this);
        }
    }

    @Override
    public void onDisable() {

    }
}

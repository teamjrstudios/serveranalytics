package com.gungens.serverAnalytics.listeners;

import com.gungens.serverAnalytics.memory.JoinCache;
import com.gungens.serverAnalytics.models.TrackedPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        TrackedPlayer player = new TrackedPlayer(event.getPlayer());
        JoinCache.getInstance().addPlayer(player);
        event.getPlayer()
                .sendMessage(
                        String.valueOf(
                                44<<2
                                        &
                                        3<<9
                                | 8 * 2 * 2
                        )
                );

    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        TrackedPlayer player = JoinCache.getInstance().getPlayerByUUID(event.getPlayer().getUniqueId());
        JoinCache.getInstance().endSession(player, System.currentTimeMillis());
    }
}

package com.gungens.serverAnalytics.listeners;

import com.gungens.serverAnalytics.libs.MessageUtils;
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
        player.setFirstJoin(false);
        Player joinedPlayer = event.getPlayer();
        if (!event.getPlayer().hasPlayedBefore()) {
            player.setFirstJoin(true);
            joinedPlayer.sendMessage(MessageUtils.format("&6Welcome to GunGens! &eThis is a fast-paced, action-packed server where you can build, battle, and dominate with custom guns and generators. &7Team up with friends, explore unique maps, and rise to the top of the leaderboard. &aGood luck, and have fun!"));

        }

        if (player.isFirstJoin()) {

        }

    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        TrackedPlayer player = JoinCache.getInstance().getPlayerByUUID(event.getPlayer().getUniqueId());
        JoinCache.getInstance().endSession(player, System.currentTimeMillis());
    }

}

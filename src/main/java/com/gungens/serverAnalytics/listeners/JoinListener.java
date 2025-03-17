package com.gungens.serverAnalytics.listeners;

import com.gungens.serverAnalytics.ServerAnalytics;
import com.gungens.serverAnalytics.database.StatisticsService;
import com.gungens.serverAnalytics.libs.MessageUtils;
import com.gungens.serverAnalytics.memory.JoinCache;
import com.gungens.serverAnalytics.models.TrackedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.logging.Level;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joinedPlayer = event.getPlayer();
        TrackedPlayer trackedPlayer = StatisticsService.getInstance().getTrackedPlayer(joinedPlayer.getUniqueId());
        if (trackedPlayer == null) {
            System.out.println("No player in db, adding...");
            trackedPlayer = new TrackedPlayer(joinedPlayer);
        }
        Bukkit.getLogger().log(Level.WARNING, "Player " + trackedPlayer.getPlayerName() + " tracked!");
        if (!joinedPlayer.hasPlayedBefore()) {
            joinedPlayer.sendMessage(MessageUtils.format("&6Welcome to GunGens! &eThis is a fast-paced, action-packed server where you can build, battle, and dominate with custom guns and generators. &7Team up with friends, explore unique maps, and rise to the top of the leaderboard. &aGood luck, and have fun!"));

        } else {
            joinedPlayer.sendMessage(MessageUtils.format("&6Welcome back to GunGens! "));
        }
        trackedPlayer.setTotalJoins(trackedPlayer.getTotalJoins() + 1);
        JoinCache.getInstance().addPlayer(trackedPlayer);
        ServerAnalytics.INSTANCE.getDatabaseManager().updatePlayer(trackedPlayer);
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        TrackedPlayer player = JoinCache.getInstance().getPlayerByUUID(event.getPlayer().getUniqueId());
        if (player == null) {
            Bukkit.getLogger().log(Level.WARNING, "Tried to end session for player " + event.getPlayer().getName() + " but they were not in the cache.");
            return;
        }
        JoinCache.getInstance().endSession(player, System.currentTimeMillis());
    }


}

package com.gungens.serverAnalytics.memory;

import com.gungens.serverAnalytics.models.TrackedPlayer;
import org.bukkit.Bukkit;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class JoinCache {
    private static JoinCache INSTANCE = new JoinCache();
    private final Map<TrackedPlayer, Long> trackedPlayerHashMap;
    private final Map<UUID, TrackedPlayer> uuidToPlayerMap; // Efficient UUID lookup

    public JoinCache() {
        trackedPlayerHashMap = new ConcurrentHashMap<>();
        uuidToPlayerMap = new ConcurrentHashMap<>();
    }
    public static JoinCache getInstance() {
        return INSTANCE;
    }
    public boolean hasPlayer(TrackedPlayer player) {
        return trackedPlayerHashMap.containsKey(player);
    }
    public TrackedPlayer getPlayerByUUID(UUID uuid) {
        return uuidToPlayerMap.get(uuid);
    }
    public void addPlayer(TrackedPlayer player) {
        trackedPlayerHashMap.put(player, 0L);
        uuidToPlayerMap.put(player.getUuid(), player);
    }
    public void removePlayer(TrackedPlayer player) {
        trackedPlayerHashMap.remove(player);
        uuidToPlayerMap.remove(player.getUuid());
    }
    public void endSession(TrackedPlayer player, long timestamp) {
        trackedPlayerHashMap.put(player, timestamp);
        double timePlayedSeconds = ((double) (timestamp - player.getTimestamp()) /1000);
        Bukkit.getLogger().log(Level.WARNING, formatTime(((int) (timestamp - player.getTimestamp()) /1000), player.getPlayerName()));
        removePlayer(player);
    }
    public static String formatTime(int seconds, String username) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        StringBuilder sb = new StringBuilder();
        sb.append("The user "+username+" has had a session for ");
        if (hours > 0) {
            sb.append(hours)
                    .append(" hrs ");
        }
        if (remainingMinutes > 0) {
            sb.append(remainingMinutes)
                    .append(" mins ");
        }
        if (remainingSeconds > 0) {
            sb.append(remainingSeconds)
                    .append(" seconds");
        }
        return sb.toString();
    }
    public Map<TrackedPlayer, Long> getCache() {
        return trackedPlayerHashMap;
    }
}

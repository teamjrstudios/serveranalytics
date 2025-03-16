package com.gungens.serverAnalytics.models;

import org.bukkit.entity.Player;

import java.util.UUID;

public class TrackedPlayer {
    private long timestamp;
    private String playerName;
    private UUID uuid;
    private boolean isFirstJoin;

    public TrackedPlayer(Player player) {
        this.timestamp = System.currentTimeMillis();
        this.playerName = player.getName();
        this.uuid = player.getUniqueId();
        this.isFirstJoin = true;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean isFirstJoin() {
        return isFirstJoin;
    }

    public void setFirstJoin(boolean firstJoin) {
        isFirstJoin = firstJoin;
    }
}

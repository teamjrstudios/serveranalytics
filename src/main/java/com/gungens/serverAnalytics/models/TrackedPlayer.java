package com.gungens.serverAnalytics.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.bukkit.entity.Player;

import java.util.UUID;
@DatabaseTable(tableName = "player_analytics")
public class TrackedPlayer {
    @DatabaseField(id = true)
    private UUID uuid;
    @DatabaseField
    private long timestamp;
    @DatabaseField
    private String playerName;
    @DatabaseField
    private boolean isFirstJoin;
    @DatabaseField
    private long timePlayed;
    public TrackedPlayer() {}

    public TrackedPlayer(Player player) {
        this.timestamp = System.currentTimeMillis();
        this.playerName = player.getName();
        this.uuid = player.getUniqueId();
        this.isFirstJoin = true;
    }

    /**
     *
     * @param timePlayed
     * Final time played from the player
     */
    public void setTimePlayed(long timePlayed) {
        this.timePlayed = timePlayed;
    }
    public long getTimePlayed() {
        return timePlayed;
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

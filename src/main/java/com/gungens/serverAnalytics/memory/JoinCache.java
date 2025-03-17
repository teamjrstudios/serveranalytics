package com.gungens.serverAnalytics.memory;

import com.gungens.serverAnalytics.ServerAnalytics;
import com.gungens.serverAnalytics.models.TrackedPlayer;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class JoinCache {
    private static final JoinCache INSTANCE = new JoinCache();
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
    public TrackedPlayer getPlayer(UUID uuid) {
        return uuidToPlayerMap.get(uuid);
    }
    public void addPlayer(TrackedPlayer player) {
        trackedPlayerHashMap.put(player, 0L);
        uuidToPlayerMap.put(player.getUuid(), player);
        ServerAnalytics.INSTANCE.getDatabaseManager().trackPlayer(player);
        sendJoinLeaveMessageWebhook("https://discordapp.com/api/webhooks/1351039518161109122/YMvH9DwpNtDfw7v19HoxHEEs-E00XVGPTEyp26-QssT6ZMalaQGKIIjgVzCWoajZrDLH", player, PLAYER_STATE.JOINED);
    }
    public void removePlayer(TrackedPlayer player) {
        trackedPlayerHashMap.remove(player);
        uuidToPlayerMap.remove(player.getUuid());
    }
    public void endSession(TrackedPlayer player, long timestamp) {
        trackedPlayerHashMap.put(player, timestamp);

        long timePlayed = timestamp - player.getTimestamp();
        player.setTimePlayed(timePlayed);
        ServerAnalytics.INSTANCE.getDatabaseManager().updatePlayer(player);

        Bukkit.getLogger().log(Level.WARNING, formatTime(((int) (timestamp - player.getTimestamp()) /1000), player.getPlayerName()));
        sendWebhook("https://discord.com/api/webhooks/1350801919345426452/emOxbCmtvddspC1gXwnsRYG4MAaXLfImWPSR5GxzRY_sUeWFJrh0lNCVgyjwQpSgxfk7", player, ((int) (timestamp - player.getTimestamp()) /1000));
        sendJoinLeaveMessageWebhook("https://discordapp.com/api/webhooks/1351039518161109122/YMvH9DwpNtDfw7v19HoxHEEs-E00XVGPTEyp26-QssT6ZMalaQGKIIjgVzCWoajZrDLH", player, PLAYER_STATE.QUIT);
        removePlayer(player);
    }
    public static void sendWebhook(String webhookUrl, TrackedPlayer player, int timePlayed) {
        var logger = Bukkit.getLogger();
        try {
            String playerHeadUrl = "https://minotar.net/avatar/" + player.getPlayerName() + "/128"; // 128x128 player head image

            String payload = String.format(
                    "{ \"content\": \"%s\", \"username\": \"%s\", \"embeds\": [{ \"title\": \"Player Session Ended\", \"description\": \"%s\", \"thumbnail\": { \"url\": \"%s\" } }] }",
                    formatTime(timePlayed, player.getPlayerName()), // Message content
                    player.getPlayerName(), // Webhook Username
                    formatTime(timePlayed, player.getPlayerName()), // Embed Description
                    playerHeadUrl // Player Head Image
            );

            HttpURLConnection connection = getUrlConnection(webhookUrl,  payload);

            int responseCode = connection.getResponseCode();
            if (responseCode == 204) {
                logger.info("Webhook sent successfully!");
            } else {
                logger.log(Level.SEVERE, "Failed to send webhook. Response code: " + responseCode + ", Message: " + connection.getResponseMessage());
            }

            connection.disconnect();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending webhook", e);
        }
    }
    enum PLAYER_STATE {
        JOINED, QUIT
    }
    private void sendJoinLeaveMessageWebhook(String webhookUrl, TrackedPlayer player, PLAYER_STATE state) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String playerName = player.getPlayerName();
            String playerHeadUrl = "https://minotar.net/avatar/" + playerName + "/128"; // ✅ Player's avatar
            String stateText = (state == PLAYER_STATE.JOINED) ? "joined the server" : "left the server";
            String color = (state == PLAYER_STATE.JOINED) ? "5763719" : "15548997"; // ✅ Green for join, red for quit

            // ✅ JSON payload with player avatar embed
            String payload = String.format(
                    "{ \"username\": \"%s\", \"embeds\": [{ \"title\": \"Player %s\", \"description\": \"**%s** %s!\", \"color\": %s, \"thumbnail\": { \"url\": \"%s\" }, \"avatar\": { \"url\": \"%s\" } }] }",
                    playerName, // Webhook Username
                    (state == PLAYER_STATE.JOINED) ? "Joined" : "Left", // Embed Title
                    playerName, // Player Name in description
                    stateText, // Join or leave message
                    color, // Embed color
                    playerHeadUrl,
                    playerHeadUrl
            );

            // ✅ Send request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 204) { // 204 means success with no content
                Bukkit.getLogger().log(Level.SEVERE, "Discord Webhook failed with response code: " + responseCode);
            }

            connection.disconnect();
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error sending Discord webhook: " + e.getMessage());
        }
    }
    private static HttpURLConnection getUrlConnection(String webhookUrl, String payload) throws IOException {
        URL url = new URL(webhookUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);



        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return connection;
    }

    public static String formatTime(int seconds, String username) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        StringBuilder sb = new StringBuilder();
        sb.append("The user ")
                .append(username)
                .append(" has had a session for ");
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

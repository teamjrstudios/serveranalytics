package com.gungens.serverAnalytics.memory;

import com.gungens.serverAnalytics.ServerAnalytics;
import com.gungens.serverAnalytics.models.TrackedPlayer;
import org.bukkit.Bukkit;

import javax.net.ssl.HttpsURLConnection;
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
        removePlayer(player);
    }
    public void sendWebhook(URL webhookUrl) throws IOException {

        HttpURLConnection connection = (HttpURLConnection) webhookUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String data = "{\n" +
                "  \"name\": \"test webhook\",\n" +
                "  \"type\": 1,\n" +
                "  \"channel_id\": \"199737254929760256\",\n" +
                "  \"token\": \"3d89bb7572e0fb30d8128367b3b1b44fecd1726de135cbe28a41f8b2f777c372ba2939e72279b94526ff5d1bd4358d65cf11\",\n" +
                "  \"avatar\": null,\n" +
                "  \"guild_id\": \"199737254929760256\",\n" +
                "  \"id\": \"223704706495545344\",\n" +
                "  \"application_id\": null,\n" +
                "  \"user\": {\n" +
                "    \"username\": \"test\",\n" +
                "    \"discriminator\": \"7479\",\n" +
                "    \"id\": \"190320984123768832\",\n" +
                "    \"avatar\": \"b004ec1740a63ca06ae2e14c5cee11f3\",\n" +
                "    \"public_flags\": 131328\n" +
                "  }\n" +
                "}\n";
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = data.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = connection.getResponseCode();
        if (responseCode != 204) {
            Bukkit.getLogger().log(Level.SEVERE, "Received response code " + responseCode + ": " + connection.getResponseMessage());
        }
        connection.disconnect();
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

package com.gungens.serverAnalytics.database;

import com.gungens.serverAnalytics.ServerAnalytics;
import com.gungens.serverAnalytics.memory.JoinCache;
import com.gungens.serverAnalytics.models.TrackedPlayer;
import org.bukkit.Bukkit;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class StatisticsService {
    private static final StatisticsService instance = new StatisticsService();
    private double getAveragePlayTime() {
        double totalTime = 0.0;
        int count = 0;

        try (Connection connection = ServerAnalytics.INSTANCE.getDatabaseManager().openConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT timePlayed FROM player_analytics WHERE COALESCE(totalJoins, 0) < 2;");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                totalTime += resultSet.getLong("timePlayed");
                count++;
            }

        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not connect to database", ex);
            return 0.0; // Return 0.0 if there is a database issue
        }

        return (count == 0) ? 0.0 : totalTime / count; // ✅ Prevent divide by zero
    }

    public TrackedPlayer getTrackedPlayer(UUID uuid) {
        TrackedPlayer trackedPlayer = null;
        try (Connection connection = ServerAnalytics.INSTANCE.getDatabaseManager().openConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_analytics WHERE uuid = ?");
            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();

            String playerName = "";
            int totalJoins = 0;
            while (resultSet.next()) {
                playerName = resultSet.getString("playerName");
                totalJoins = resultSet.getInt("totalJoins");
            }

            if (!playerName.isEmpty()) {
                trackedPlayer = new TrackedPlayer(uuid, playerName, totalJoins);
            }

        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not retrieve player from database", e);
        }
        return trackedPlayer;
    }

    public static StatisticsService getInstance() {
        return instance;
    }
    public void sendAverageTimeStat(String webhookUrl) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            double avgPlaytime = getAveragePlayTime();
            String payload = getAveragePlaytimePayload(avgPlaytime);

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


    private static String getAveragePlaytimePayload(double avgPlaytimeSeconds) {
        int minutes = (int) (avgPlaytimeSeconds / 60);
        int remainingSeconds = (int) (avgPlaytimeSeconds % 60);
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;

        String formattedTime = String.format(
                "%d hrs %d mins %d secs",
                hours, remainingMinutes, remainingSeconds
        );

        // ✅ JSON payload with average playtime
        return String.format(
                "{ \"username\": \"Server Stats\", \"embeds\": [{ \"title\": \"Average Player Playtime\", \"description\": \"📊 The average playtime is **%s**\", \"color\": 3447003 }] }",
                formattedTime // Formatted Playtime
        );
    }

}

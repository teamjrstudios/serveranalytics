package com.gungens.serverAnalytics.database;

import com.gungens.serverAnalytics.ServerAnalytics;
import com.gungens.serverAnalytics.models.TrackedPlayer;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class StatisticsService {
    private static final StatisticsService instance = new StatisticsService();
    public double getAveragePlayTime() {
        try (Connection connection = ServerAnalytics.INSTANCE.getDatabaseManager().openConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT timePlayed FROM player_analytics where total_joins < 2");

            return 0.0;
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not connect to database", ex);
        }
        return 0.0;
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
}

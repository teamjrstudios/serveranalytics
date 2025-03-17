package com.gungens.serverAnalytics.database;

import com.gungens.serverAnalytics.ServerAnalytics;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class StatisticsService {
    public double getAveragePlayTime() {
        try (Connection connection = ServerAnalytics.INSTANCE.getDatabaseManager().openConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT timePlayed FROM player_analytics");

        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not connect to database", ex);
        }
    }
}

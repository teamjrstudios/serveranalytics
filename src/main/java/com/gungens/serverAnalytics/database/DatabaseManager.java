package com.gungens.serverAnalytics.database;

import com.gungens.serverAnalytics.models.TrackedPlayer;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class DatabaseManager {
    private Dao<TrackedPlayer, String> playerDao;
    private final String databaseUrl = "jdbc:sqlite:plugins/ServerAnalytics/database.db";
    public DatabaseManager() {
        connect();
        this.playerDao = playerDao;
    }
    public void updatePlayer(TrackedPlayer player) {
        try {
            playerDao.update(player);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to update player data: " + e.getMessage());
        }
    }
    public void connect() {
        try {

            ConnectionSource connectionSource = new JdbcConnectionSource(databaseUrl);

            // Create the table if it doesn't exist
            TableUtils.createTableIfNotExists(connectionSource, TrackedPlayer.class);

            // Initialize DAO
            playerDao = com.j256.ormlite.dao.DaoManager.createDao(connectionSource, TrackedPlayer.class);
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to connect to database: " + e.getMessage());
        }
    }
    public Connection openConnection() {
        try {
            return DriverManager.getConnection(databaseUrl);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void trackPlayer(TrackedPlayer trackedPlayer) {
        try {
            playerDao.createIfNotExists(trackedPlayer);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to track player: " + e.getMessage());
        }
    }


}

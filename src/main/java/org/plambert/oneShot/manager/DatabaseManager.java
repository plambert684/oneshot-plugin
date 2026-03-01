package org.plambert.oneShot.manager;

import org.plambert.oneShot.OneShot;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {

    private final OneShot plugin;
    private Connection connection;

    public DatabaseManager(OneShot plugin) {
        this.plugin = plugin;
    }

    public void init() {
        String type = plugin.getConfig().getString("database.type", "sqlite").toLowerCase();
        try {
            if (type.equals("sqlite")) {
                File databaseFile = new File(plugin.getDataFolder(), "scores.db");
                if (!plugin.getDataFolder().exists()) {
                    plugin.getDataFolder().mkdirs();
                }
                connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            } else if (type.equals("mysql") || type.equals("postgresql")) {
                String host = plugin.getConfig().getString("database.host");
                int port = plugin.getConfig().getInt("database.port");
                String database = plugin.getConfig().getString("database.database");
                String user = plugin.getConfig().getString("database.username");
                String pass = plugin.getConfig().getString("database.password");
                
                String url = "";
                if (type.equals("mysql")) {
                    url = "jdbc:mysql://" + host + ":" + port + "/" + database;
                } else {
                    url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
                }
                connection = DriverManager.getConnection(url, user, pass);
            }
            
            createTable();
            plugin.getLogger().info("Database connected successfully (" + type + ")");
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not connect to database! " + e.getMessage());
        }
    }

    private void createTable() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS os_scores (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "kills INTEGER DEFAULT 0)");
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getKills(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT kills FROM os_scores WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("kills");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void updateKills(UUID uuid, int kills) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO os_scores (uuid, kills) VALUES (?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET kills = ?")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, kills);
            ps.setInt(3, kills);
            ps.executeUpdate();
        } catch (SQLException e) {
            // SQLite specific "ON CONFLICT" might not work on MySQL/Postgres if the syntax is different
            // Let's use a more compatible approach if needed, but for SQLite it's fine.
            // For MySQL: ON DUPLICATE KEY UPDATE kills = VALUES(kills)
            // For Postgres: ON CONFLICT(uuid) DO UPDATE SET kills = EXCLUDED.kills
            
            String type = plugin.getConfig().getString("database.type", "sqlite").toLowerCase();
            if (type.equals("mysql")) {
                try (PreparedStatement mysqlPs = connection.prepareStatement(
                        "INSERT INTO os_scores (uuid, kills) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE kills = ?")) {
                    mysqlPs.setString(1, uuid.toString());
                    mysqlPs.setInt(2, kills);
                    mysqlPs.setInt(3, kills);
                    mysqlPs.executeUpdate();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                 e.printStackTrace();
            }
        }
    }
}

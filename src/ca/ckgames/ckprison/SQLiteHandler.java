package ca.ckgames.ckprison;

import ca.ckgames.ckprison.ranks.Rank;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.logging.Logger;

public class SQLiteHandler {
    private Connection connection;
    private final CKPrisonPlugin plugin;

    public SQLiteHandler(CKPrisonPlugin plugin) {
        this.plugin = plugin;
        try {
            File file = new File(plugin.getDataFolder(), "storage.db");
            if (!file.exists()) {
                file.createNewFile();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + file);
        } catch (Exception e) {
            plugin.getLogger().severe("SQLite error:");
            plugin.getLogger().severe(e.getMessage());
        }
    }

    public void setupTables(String defaultRank) {
        try {
            PreparedStatement statement = connection.prepareStatement(String.format("""
                    CREATE TABLE IF NOT EXISTS player_data (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        uuid TEXT NOT NULL,
                        prison_rank TEXT NOT NULL DEFAULT '%s'
                    )""", defaultRank));
            statement.execute();
        } catch (SQLException e) {
            plugin.getLogger().severe("SQLite error:");
            plugin.getLogger().severe(e.getMessage());
        }
    }

    public void addNewPlayer(Player player) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO player_data (uuid) VALUES (?)");
            statement.setString(1, player.getUniqueId().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            Logger logger = player.getServer().getLogger();
            logger.severe("SQLite error:");
            logger.severe(e.getMessage());
        }
    }

    public String getPlayerRank(Player player) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT (prison_rank) FROM player_data WHERE uuid = ?");
            statement.setString(1, player.getUniqueId().toString());
            ResultSet results = statement.executeQuery();

            results.next();
            if (results.getRow() == 0) {
                return null;
            }

            return results.getString("prison_rank");
        } catch (SQLException e) {
            Logger logger = player.getServer().getLogger();
            logger.severe("SQLite error:");
            logger.severe(e.getMessage());
            return null;
        }
    }

    public void setPlayerRank(Player player, Rank rank) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE player_data SET prison_rank = ? WHERE uuid = ?");
            statement.setString(1, rank.name);
            statement.setString(2, player.getUniqueId().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            Logger logger = player.getServer().getLogger();
            logger.severe("SQLite error:");
            logger.severe(e.getMessage());
        }
    }
}

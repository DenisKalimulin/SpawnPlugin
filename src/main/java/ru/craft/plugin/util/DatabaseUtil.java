package ru.craft.plugin.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import ru.craft.plugin.SpawnPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class DatabaseUtil {
    private final SpawnPlugin plugin;
    @Getter
    private HikariDataSource dataSource;

    public DatabaseUtil(SpawnPlugin plugin) {
        this.plugin = plugin;
    }

    public void setupDatabase() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" +
                plugin.getConfig().getString("database.host") + ":" +
                plugin.getConfig().getInt("database.port") + "/" +
                plugin.getConfig().getString("database.name") +
                "?useSSL=false&autoReconnect=true&characterEncoding=utf8");

        config.setUsername(plugin.getConfig().getString("database.user"));
        config.setPassword(plugin.getConfig().getString("database.password"));
        config.setMaximumPoolSize(10);

        dataSource = new HikariDataSource(config);
    }

    public void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS homes (" +
                "uuid VARCHAR(36) NOT NULL," +
                "world VARCHAR(64) NOT NULL," +
                "x DOUBLE PRECISION NOT NULL," +
                "y DOUBLE PRECISION NOT NULL," +
                "z DOUBLE PRECISION NOT NULL," +
                "yaw FLOAT NOT NULL," +
                "pitch FLOAT NOT NULL," +
                "PRIMARY KEY (uuid)" +
                ")";



        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            getLogger().info("Homes table checked/created successfully.");
        } catch (SQLException e) {
            getLogger().severe("Could not create table 'homes': " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private Logger getLogger() {
        return plugin.getLogger();
    }
}

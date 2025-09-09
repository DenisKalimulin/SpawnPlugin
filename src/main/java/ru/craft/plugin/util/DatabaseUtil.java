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
        HikariConfig cfg = new HikariConfig();

        String host = plugin.getConfig().getString("database.host");
        int    port = plugin.getConfig().getInt("database.port");
        String db   = plugin.getConfig().getString("database.name");

        // Если используешь SSL, поменяй useSSL/sslMode ниже.
        String url = "jdbc:mysql://" + host + ":" + port + "/" + db
                + "?useUnicode=true"
                + "&characterEncoding=utf8"
                + "&serverTimezone=UTC"
                + "&useSSL=false"
                + "&allowPublicKeyRetrieval=true" // <-- фикс твоей ошибки
                + "&cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048";

        cfg.setJdbcUrl(url);
        cfg.setUsername(plugin.getConfig().getString("database.user"));
        cfg.setPassword(plugin.getConfig().getString("database.password"));

        // Параметры пула — разумные дефолты
        cfg.setPoolName("SpawnPluginPool");
        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(2);
        cfg.setConnectionTimeout(10000); // 10s
        cfg.setIdleTimeout(600000);      // 10 min
        cfg.setMaxLifetime(1800000);     // 30 min
        cfg.setValidationTimeout(5000);
        cfg.setConnectionTestQuery("SELECT 1");

        dataSource = new HikariDataSource(cfg);
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

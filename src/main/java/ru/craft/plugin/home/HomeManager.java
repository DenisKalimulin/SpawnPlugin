package ru.craft.plugin.home;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.craft.plugin.SpawnPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

// TODO сделать настройку, чтобы можно было сохранять как в БД, так и в файл

@AllArgsConstructor
public class HomeManager {

    private final SpawnPlugin plugin;

    public void setHome(Player player, Location location) {
        String sql = "REPLACE INTO homes (uuid, world, x, y, z, yaw, pitch) VALUES (?,?,?,?,?,?,?)";

        try (Connection c = plugin.getDatabaseUtil().getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, location.getWorld().getName());
            ps.setDouble(3, location.getX());
            ps.setDouble(4, location.getY());
            ps.setDouble(5, location.getZ());
            ps.setFloat(6, location.getYaw());
            ps.setFloat(7, location.getPitch());
            ps.executeUpdate();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save home: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Location getHome(Player player) {
        UUID uuid = player.getUniqueId();
        String sql = "SELECT world, x, y, z, yaw, pitch FROM homes WHERE uuid=?";

        try (Connection c = plugin.getDatabaseUtil().getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                World w = Bukkit.getWorld(rs.getString("world"));
                if (w == null) return null;
                return new Location(
                        w,
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z"),
                        rs.getFloat("yaw"),
                        rs.getFloat("pitch")
                );
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load home: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(""); // TODO Придумать описание ошибки
        }
    }
}

package ru.craft.plugin.home;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import ru.craft.plugin.SpawnPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@RequiredArgsConstructor
public class HomeStorageSql implements HomeStorage {

    private final SpawnPlugin plugin;

    @Override
    public void saveHome(UUID uuid, Location location) throws Exception {
        String sql = "REPLACE INTO homes (uuid, world, x, y, z, yaw, pitch) VALUES (?,?,?,?,?,?,?)";

        try (Connection c = plugin.getDatabaseUtil().getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
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

    @Override
    public Location loadHome(UUID uuid) {
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

    @Override
    public boolean deleteHome(UUID uuid) throws Exception {
        String sql = "DELETE FROM homes WHERE uuid=?";
        try (Connection c = plugin.getDatabaseUtil().getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            int affected = ps.executeUpdate();
            return affected > 0; // true — был дом и мы его удалили
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to delete home: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

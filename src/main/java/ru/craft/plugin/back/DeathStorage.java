package ru.craft.plugin.back;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.craft.plugin.SpawnPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathStorage {
    private final SpawnPlugin plugin;
    private final Map<UUID, Location> lastDeaths = new HashMap<>();
    private final File file;
    private YamlConfiguration yml;

    public DeathStorage(SpawnPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "lastdeaths.yml");
    }

    public void load() {
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException ignored) {}
        this.yml = YamlConfiguration.loadConfiguration(file);
        lastDeaths.clear();

        if (yml.isConfigurationSection("deaths")) {
            for (String key : yml.getConfigurationSection("deaths").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String base = "deaths." + key + ".";
                    String worldName = yml.getString(base + "world");
                    World w = worldName != null ? Bukkit.getWorld(worldName) : null;
                    if (w == null) continue;
                    double x = yml.getDouble(base + "x");
                    double y = yml.getDouble(base + "y");
                    double z = yml.getDouble(base + "z");
                    float yaw = (float) yml.getDouble(base + "yaw");
                    float pitch = (float) yml.getDouble(base + "pitch");
                    lastDeaths.put(uuid, new Location(w, x, y, z, yaw, pitch));
                } catch (Exception ignored) {}
            }
        }
    }

    public void save() {
        if (yml == null) yml = new YamlConfiguration();
        yml.set("deaths", null);
        for (Map.Entry<UUID, Location> e : lastDeaths.entrySet()) {
            Location l = e.getValue();
            if (l == null || l.getWorld() == null) continue;
            String base = "deaths." + e.getKey() + ".";
            yml.set(base + "world", l.getWorld().getName());
            yml.set(base + "x", l.getX());
            yml.set(base + "y", l.getY());
            yml.set(base + "z", l.getZ());
            yml.set(base + "yaw", l.getYaw());
            yml.set(base + "pitch", l.getPitch());
        }
        try { yml.save(file); } catch (IOException ex) {
            plugin.getLogger().severe("Cannot save lastdeaths.yml: " + ex.getMessage());
        }
    }

    public void setLastDeath(UUID uuid, Location loc) {
        lastDeaths.put(uuid, loc);
    }

    public Location getLastDeath(UUID uuid) {
        return lastDeaths.get(uuid);
    }

    public void clearLastDeath(UUID uuid) {
        lastDeaths.remove(uuid);
    }
}

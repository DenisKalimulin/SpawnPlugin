package ru.craft.plugin.home;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.craft.plugin.SpawnPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class HomeStorageYml implements HomeStorage {

    private final File file;
    private final YamlConfiguration cfg;

    public HomeStorageYml(SpawnPlugin plugin) {
        this.file = new File(plugin.getDataFolder(), "homes.yml");
        if (!file.exists()) try { file.getParentFile().mkdirs(); file.createNewFile(); } catch (IOException ignored) {}
        this.cfg = YamlConfiguration.loadConfiguration(file);
    }

    private void saveFile() throws IOException {
        cfg.save(file);
    }

    @Override
    public void saveHome(UUID uuid, Location loc) throws Exception {
        String str = "homes." + uuid;
        cfg.set(str + ".world", Objects.requireNonNull(loc.getWorld()).getName());
        cfg.set(str + ".x", loc.getX());
        cfg.set(str + ".y", loc.getY());
        cfg.set(str + ".z", loc.getZ());
        cfg.set(str + ".yaw", loc.getYaw());
        cfg.set(str + ".pitch", loc.getPitch());
        saveFile();
    }

    @Override
    public Location loadHome(UUID uuid) {
        String p = "homes." + uuid;
        if (!cfg.isConfigurationSection(p)) return null;
        World world = Bukkit.getWorld(cfg.getString(p + ".world", ""));
        if (world == null) return null;
        return new Location(
                world,
                cfg.getDouble(p + ".x"),
                cfg.getDouble(p + ".y"),
                cfg.getDouble(p + ".z"),
                (float) cfg.getDouble(p + ".yaw"),
                (float) cfg.getDouble(p + ".pitch")
        );
    }

    @Override
    public boolean deleteHome(UUID uuid) throws Exception {
        String str = "homes." + uuid;
        if (!cfg.isConfigurationSection(str)) return false;
        cfg.set(str, null);
        saveFile();
        return true;
    }
}

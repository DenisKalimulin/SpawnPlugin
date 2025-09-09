package ru.craft.plugin.home;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.craft.plugin.SpawnPlugin;

import java.util.Locale;


@AllArgsConstructor
public class HomeManager {

    private final SpawnPlugin plugin;

    private final HomeStorage storage;

    @Getter
    private boolean isSql;

    public HomeManager(SpawnPlugin plugin) {
        this.plugin = plugin;
        String mode = plugin.getConfig().getString("storage.homes", "db").toLowerCase(Locale.ROOT);
        if (mode.equals("file")) {
            storage = new HomeStorageYml(plugin);
            plugin.getLogger().info("Home storage: FILE");
            isSql = false;
        } else {
            storage = new HomeStorageSql(plugin);
            plugin.getLogger().info("Home storage: DB");
            isSql = true;
        }
    }

    public void setHome(Player player, Location location) {
        try {
            storage.saveHome(player.getUniqueId(), location);
        } catch (Exception e) {
            plugin.getLogger().warning("Home storage error: " + e.getMessage());
            player.sendMessage(plugin.getMessage().tr("errorSavingHome"));
        }
    }

    public Location getHome(Player player) {
        return storage.loadHome(player.getUniqueId());
    }

    public boolean deleteHome(Player player) {
        try { return storage.deleteHome(player.getUniqueId()); }
        catch (Exception e) {
            plugin.getLogger().severe("deleteHome failed: " + e.getMessage());
            return false;
        }
    }
}

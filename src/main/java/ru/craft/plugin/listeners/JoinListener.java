package ru.craft.plugin.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.craft.plugin.SpawnPlugin;

public class JoinListener implements Listener {
    private final SpawnPlugin plugin;

    public JoinListener(SpawnPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("teleportOnFirstJoin", true)) return;
        Player player = event.getPlayer();
        if (player.hasPlayedBefore()) return;

        Location spawn = plugin.getSpawnLocation();
        if (spawn == null || spawn.getWorld() == null) {
            return;
        }

        player.sendMessage(plugin.getMessage().tr("firstJoinTp"));
        plugin.getServer().getScheduler().runTask(plugin, () -> player.teleport(spawn));
    }
}

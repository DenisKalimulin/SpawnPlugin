package ru.spawn.plugin;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final SpawnPlugin plugin;

    public JoinListener(SpawnPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!plugin.getConfig().getBoolean("teleportOnFirstJoin", true)) return;
        Player player = e.getPlayer();
        if (player.hasPlayedBefore()) return;

        Location spawn = plugin.getSpawnLocation();
        if (spawn == null || spawn.getWorld() == null) {
            return;
        }

        player.sendMessage(plugin.getMessage().tr("firstJoinTp"));
        plugin.getServer().getScheduler().runTask(plugin, () -> player.teleport(spawn));
    }
}

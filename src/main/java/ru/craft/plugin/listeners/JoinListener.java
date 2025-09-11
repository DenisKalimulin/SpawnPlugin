package ru.craft.plugin.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.craft.plugin.SpawnPlugin;
import ru.craft.plugin.spawn.SpawnCommand;

public class JoinListener implements Listener {

    private final SpawnCommand spawnCommand;

    private final SpawnPlugin plugin;

    public JoinListener(SpawnPlugin plugin, SpawnCommand spawnCommand) {
        this.plugin = plugin;
        this.spawnCommand = spawnCommand;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("teleportOnFirstJoin", true)) return;
        Player player = event.getPlayer();
        if (player.hasPlayedBefore()) return;

        Location spawn = spawnCommand.getSpawnLocation();
        if (spawn == null || spawn.getWorld() == null) {
            return;
        }

        player.sendMessage(plugin.getMessage().tr("firstJoinTp"));
        plugin.getServer().getScheduler().runTask(plugin, () -> player.teleport(spawn));
    }
}

package ru.craft.plugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import ru.craft.plugin.SpawnPlugin;
import ru.craft.plugin.spawn.SpawnCommand;

public class RespawnListener implements Listener {

    private final SpawnPlugin plugin;
    private final SpawnCommand spawnCommand;

    public RespawnListener(SpawnPlugin plugin, SpawnCommand spawnCommand) {
        this.plugin = plugin;
        this.spawnCommand = spawnCommand;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent respawnEvent) {
        if (respawnEvent.isBedSpawn() || respawnEvent.isAnchorSpawn()) {
            return;
        }

        var home = plugin.getHomeManager().getHome(respawnEvent.getPlayer());
        if (home != null) {
            respawnEvent.setRespawnLocation(home);
            return;
        }


        var spawn = spawnCommand.getSpawnLocation();
        if (spawn != null && spawn.getWorld() != null) {
            respawnEvent.setRespawnLocation(spawn);
        } else {
            respawnEvent.setRespawnLocation(respawnEvent.getPlayer().getRespawnLocation());
        }
    }
}

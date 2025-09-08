package ru.spawn.plugin.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import ru.spawn.plugin.SpawnPlugin;

public class RespawnListener implements Listener {

    private final SpawnPlugin plugin;

    public RespawnListener(SpawnPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent respawnEvent) {
        if (respawnEvent.isBedSpawn() || respawnEvent.isAnchorSpawn()) {
            return;
        }

        Location spawn = plugin.getSpawnLocation();
        if (spawn != null && spawn.getWorld() != null) {
            respawnEvent.setRespawnLocation(spawn);
        } else {
            respawnEvent.setRespawnLocation(respawnEvent.getPlayer().getRespawnLocation());
        }
    }
}

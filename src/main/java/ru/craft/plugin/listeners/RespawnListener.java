package ru.craft.plugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import ru.craft.plugin.SpawnPlugin;

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

        var home =plugin.getHomeManager().getHome(respawnEvent.getPlayer());
        if (home != null) {
            respawnEvent.setRespawnLocation(home);
            return;
        }


        var spawn = plugin.getSpawnLocation();
        if (spawn != null && spawn.getWorld() != null) {
            respawnEvent.setRespawnLocation(spawn);
        } else {
            respawnEvent.setRespawnLocation(respawnEvent.getPlayer().getRespawnLocation());
        }
    }
}

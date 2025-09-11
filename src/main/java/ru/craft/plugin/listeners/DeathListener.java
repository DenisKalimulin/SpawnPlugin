package ru.craft.plugin.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import ru.craft.plugin.SpawnPlugin;

@RequiredArgsConstructor
public class DeathListener implements Listener {

    private final SpawnPlugin plugin;

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        player.sendMessage(plugin.getMessage().tr("death"));

        if (event.getEntity() == null || event.getEntity().getWorld() == null) {
            return;
        }
        plugin.getDeathStorage().setLastDeath(event.getEntity().getUniqueId(), event.getEntity().getLocation());
    }
}

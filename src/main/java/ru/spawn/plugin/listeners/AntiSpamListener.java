package ru.spawn.plugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.spawn.plugin.SpawnPlugin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiSpamListener implements Listener {
    private final SpawnPlugin plugin;
    private final Map<UUID, Deque<Long>> hist = new HashMap<>();

    public AntiSpamListener(SpawnPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent e) {
        if (!plugin.getConfig().getBoolean("antiSpam.enabled", true)) return;
        if (e.getPlayer().hasPermission(plugin.getConfig().getString("antiSpam.exemptPermission", "simplespawn.antispam.exempt")))
            return;

        int window = plugin.getConfig().getInt("antiSpam.windowSeconds", 5);
        int max = plugin.getConfig().getInt("antiSpam.maxCommands", 5);

        long now = System.currentTimeMillis();
        Deque<Long> q = hist.computeIfAbsent(e.getPlayer().getUniqueId(), k -> new ArrayDeque<>());

        long cutoff = now - window*1000L;
        while (!q.isEmpty() && q.peekFirst() < cutoff) q.pollFirst();

        q.addLast(now);
        if (q.size() > max) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(plugin.getMessage().tr("tooManyCommands"));
        }
    }
}

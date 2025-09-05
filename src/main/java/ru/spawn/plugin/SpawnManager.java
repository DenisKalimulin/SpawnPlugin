package ru.spawn.plugin;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpawnManager {
    private final SpawnPlugin plugin;

    private final Map<UUID, Long> lastUsed = new HashMap<>();

    private final Map<UUID, BukkitTask> pending = new HashMap<>();

    private final Map<UUID, Location> startLoc = new HashMap<>();

    public SpawnManager(SpawnPlugin plugin) {
        this.plugin = plugin;
    }

    public long getCooldownLeft(Player player, int cooldownSec) {
        long now = System.currentTimeMillis();
        long last = lastUsed.getOrDefault(player.getUniqueId(), 0L);
        long left = cooldownSec * 1000 - (now - last);
        return Math.max(0, left);
    }

    public boolean isPending(Player player) {
        return pending.containsKey(player.getUniqueId());
    }

    public void startTp(Player player, Location to, int warmupSec, boolean cancelOnMove) {
        startLoc.put(player.getUniqueId(), player.getLocation().clone());

        BukkitTask task = new BukkitRunnable() {
            @Override public void run() {
                pending.remove(player.getUniqueId());
                startLoc.remove(player.getUniqueId());
                player.teleport(to);
                player.sendMessage(plugin.getMessage().tr("spawnTp"));
                lastUsed.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }.runTaskLater(plugin, warmupSec * 20L);

        pending.put(player.getUniqueId(), task);

        if (cancelOnMove) {
            plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                if (!pending.containsKey(player.getUniqueId())) return;
                Location start = startLoc.get(player.getUniqueId());
                if (start == null) return;
                Location cur = player.getLocation();
                if (moved(start, cur)) {
                    cancelTeleport(player);
                    player.sendMessage(plugin.getMessage().tr("spawnWarmupCancelled"));
                }
            }, 2L, 2L);
        }
    }

    public void cancelTeleport(Player player) {
        BukkitTask t = pending.remove(player.getUniqueId());
        if (t != null) t.cancel();
        startLoc.remove(player.getUniqueId());
    }

    private boolean moved(Location a, Location b) {
        return a.getWorld() != b.getWorld()
                || a.distanceSquared(b) > 0.01;
    }
}

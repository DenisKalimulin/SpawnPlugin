package ru.craft.plugin.back;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import ru.craft.plugin.SpawnPlugin;

import java.util.List;

public class BackCommand implements CommandExecutor {

    private final SpawnPlugin plugin;

    public BackCommand(SpawnPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender snd, Command cmd, String label, String[] args) {
        if (!(snd instanceof Player p)) {
            snd.sendMessage(plugin.getMessage().tr("onlyPlayers"));
            return true;
        }
        if (!p.hasPermission("simplespawn.back")) {
            p.sendMessage(plugin.getMessage().tr("backNoPermission"));
            return true;
        }

        Location death = plugin.getDeathStorage().getLastDeath(p.getUniqueId());
        if (death == null || death.getWorld() == null) {
            p.sendMessage(plugin.getMessage().tr("backNoDeath"));
            return true;
        }

        List<String> blocked = plugin.getConfig().getStringList("back.blocked-worlds");
        String worldName = death.getWorld().getName();
        if (blocked != null && blocked.contains(worldName)) {
            p.sendMessage(String.format(plugin.getMessage().tr("backWorldBlocked"), worldName));
            return true;
        }

        int cd = plugin.getConfig().getInt("back.cooldown-seconds", 0);
        if (cd > 0) {
            String key = "back.cooldown." + p.getUniqueId();
            long now = System.currentTimeMillis();
            long last = plugin.getConfig().getLong(key, 0L);
            long left = cd * 1000L - (now - last);
            if (left > 0) {
                long leftSec = (left + 999) / 1000;
                p.sendMessage(String.format(plugin.getMessage().tr("backOnCooldown"), leftSec));
                return true;
            }
            plugin.getConfig().set(key, now);
            plugin.saveConfig();
        }

        // безопасный ТП
        World w = death.getWorld();
        Chunk ch = w.getChunkAt(death);
        if (!ch.isLoaded()) ch.load();
        p.setFallDistance(0);

        boolean ok = p.teleport(death);
        if (ok) {
            p.sendMessage(String.format(
                    plugin.getMessage().tr("backSuccess"),
                    worldName, death.getBlockX(), death.getBlockY(), death.getBlockZ()
            ));
            if (plugin.getConfig().getBoolean("back.one-time-use", true)) {
                plugin.getDeathStorage().clearLastDeath(p.getUniqueId());
            }
        } else {
            p.sendMessage(plugin.getMessage().tr("backFail"));
        }
        return true;
    }
}

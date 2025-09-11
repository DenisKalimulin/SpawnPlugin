package ru.craft.plugin.spawn;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.craft.plugin.SpawnPlugin;

@AllArgsConstructor
public class SpawnCommand implements CommandExecutor {
    @Getter
    private Location spawnLocation;

    private final SpawnPlugin plugin;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(plugin.getMessage().tr("onlyPlayers"));
            return true;
        }

        String name = command.getName().toLowerCase();
        return switch (name) {
            case "setspawn" -> handleSetSpawn(player);
            case "spawn" -> handleSpawn(player);
            default -> false;
        };
    }

    public void saveSpawn() {
        if (spawnLocation == null || spawnLocation.getWorld() == null) return;
        plugin.getConfig().set("spawn.world", spawnLocation.getWorld().getName());
        plugin.getConfig().set("spawn.x", spawnLocation.getX());
        plugin.getConfig().set("spawn.y", spawnLocation.getY());
        plugin.getConfig().set("spawn.z", spawnLocation.getZ());
        plugin.getConfig().set("spawn.yaw", spawnLocation.getYaw());
        plugin.getConfig().set("spawn.pitch", spawnLocation.getPitch());
        plugin.saveConfig();
    }

    public void loadSpawn() {
        if (plugin.getConfig().contains("spawn.world")) {
            World world = Bukkit.getWorld(plugin.getConfig().getString("spawn.world"));
            if (world != null) {
                double x = plugin.getConfig().getDouble("spawn.x");
                double y = plugin.getConfig().getDouble("spawn.y");
                double z = plugin.getConfig().getDouble("spawn.z");
                float yaw = (float) plugin.getConfig().getDouble("spawn.yaw");
                float pitch = (float) plugin.getConfig().getDouble("spawn.pitch");
                spawnLocation = new Location(world, x, y, z, yaw, pitch);
            }
        }
    }

    private boolean handleSetSpawn(Player player) {
        if (!player.hasPermission("simple.setspawn")) {
            player.sendMessage(plugin.getMessage().tr("noAccessToSetSpawn"));
            return true;
        }
        spawnLocation = player.getLocation();
        saveSpawn();
        player.sendMessage(plugin.getMessage().tr("spawnSet"));
        plugin.getLogger().info("Set spawn by " + player.getName() + " at " + locToString(spawnLocation));
        return true;
    }

    private boolean handleSpawn(Player player) {
        if (spawnLocation == null || spawnLocation.getWorld() == null) {
            player.sendMessage(plugin.getMessage().tr("spawnNotSet"));
            return true;
        }

        // Бери менеджер из плагина
        var spawnManager = plugin.getSpawnManager();

        int cd = plugin.getConfig().getInt("spawn.cooldownSeconds", 10);
        long leftMs = spawnManager.getCooldownLeft(player, cd);
        if (leftMs > 0) {
            long leftSec = (leftMs + 999) / 1000;
            player.sendMessage(String.format(plugin.getMessage().tr("spawnCooldown"), leftSec));
            return true;
        }

        int warm = plugin.getConfig().getInt("spawn.warmupSeconds", 0);
        boolean cancelOnMove = plugin.getConfig().getBoolean("spawn.cancelOnMove", true);

        if (warm <= 0) {
            player.teleport(spawnLocation);
            player.sendMessage(plugin.getMessage().tr("spawnTp"));
            spawnManager.startTp(player, spawnLocation, 0, false);
            plugin.getLogger().info(player.getName() + " teleported to spawn instantly");
        } else {
            player.sendMessage(String.format(plugin.getMessage().tr("spawnWarmup"), warm));
            spawnManager.startTp(player, spawnLocation, warm, cancelOnMove);
            plugin.getLogger().info(player.getName() + " started spawn warmup: " + warm + "s");
        }
        return true;
    }

    private String locToString(Location l) {
        return String.format("%s [%.1f, %.1f, %.1f, yaw=%.1f, pitch=%.1f]",
            l.getWorld() != null ? l.getWorld().getName() : "null",
            l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
    }
}
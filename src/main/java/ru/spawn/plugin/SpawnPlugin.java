package ru.spawn.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.spawn.plugin.listeners.JoinListener;
import ru.spawn.plugin.listeners.RespawnListener;

public class SpawnPlugin extends JavaPlugin {

    private Location spawnLocation;
    private SpawnManager spawnManager;
    private Message message;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSpawn();

        String locale = getConfig().getString("locale", "ru");
        this.message = new Message(this, locale);
        this.spawnManager = new SpawnManager(this);

        // Логи для диагностики
        getLogger().info(message.tr("spawnEnabled") + " (" + locale + ")");
        getLogger().info("Spawn loaded? " + (spawnLocation != null));

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new RespawnListener(this), this);
    }

    @Override
    public void onDisable() {
        saveSpawn();
        getLogger().info(message.tr("spawnDisable"));
    }

    public Message getMessage() {
        return message;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(message.tr("onlyPlayers"));
            return true;
        }

        String name = command.getName().toLowerCase();
        switch (name) {
            case "setspawn": return handleSetSpawn(player);
            case "spawn":    return handleSpawn(player);
            default:         return false;
        }
    }

    private boolean handleSetSpawn(Player player) {
        if (!player.hasPermission("simple.setspawn")) {
            player.sendMessage(message.tr("noAccessToSetSpawn"));
            return true;
        }
        spawnLocation = player.getLocation();
        saveSpawn();
        player.sendMessage(message.tr("spawnSet"));
        getLogger().info("Set spawn by " + player.getName() + " at " + locToString(spawnLocation));
        return true;
    }

    private boolean handleSpawn(Player player) {
        if (spawnLocation == null || spawnLocation.getWorld() == null) {
            player.sendMessage(message.tr("spawnNotSet"));
            return true;
        }

        int cd = getConfig().getInt("spawn.cooldownSeconds", 10);
        long leftMs = spawnManager.getCooldownLeft(player, cd);
        if (leftMs > 0) {
            long leftSec = (leftMs + 999) / 1000;
            player.sendMessage(String.format(message.tr("spawnCooldown"), leftSec));
            return true;
        }

        int warm = getConfig().getInt("spawn.warmupSeconds", 0);
        boolean cancelOnMove = getConfig().getBoolean("spawn.cancelOnMove", true);

        if (warm <= 0) {
            player.teleport(spawnLocation);
            player.sendMessage(message.tr("spawnTp"));

            spawnManager.startTp(player, spawnLocation, 0, false);
            getLogger().info(player.getName() + " teleported to spawn instantly");
        } else {
            player.sendMessage(String.format(message.tr("spawnWarmup"), warm));
            spawnManager.startTp(player, spawnLocation, warm, cancelOnMove);
            getLogger().info(player.getName() + " started spawn warmup: " + warm + "s");
        }
        return true;
    }

    private void loadSpawn() {
        if (getConfig().contains("spawn.world")) {
            World world = Bukkit.getWorld(getConfig().getString("spawn.world"));
            if (world != null) {
                double x = getConfig().getDouble("spawn.x");
                double y = getConfig().getDouble("spawn.y");
                double z = getConfig().getDouble("spawn.z");
                float yaw = (float) getConfig().getDouble("spawn.yaw");
                float pitch = (float) getConfig().getDouble("spawn.pitch");
                spawnLocation = new Location(world, x, y, z, yaw, pitch);
            }
        }
    }

    private void saveSpawn() {
        if (spawnLocation == null || spawnLocation.getWorld() == null) return;
        getConfig().set("spawn.world", spawnLocation.getWorld().getName());
        getConfig().set("spawn.x", spawnLocation.getX());
        getConfig().set("spawn.y", spawnLocation.getY());
        getConfig().set("spawn.z", spawnLocation.getZ());
        getConfig().set("spawn.yaw", spawnLocation.getYaw());
        getConfig().set("spawn.pitch", spawnLocation.getPitch());
        saveConfig();
    }

    private String locToString(Location l) {
        return String.format("%s [%.1f, %.1f, %.1f, yaw=%.1f, pitch=%.1f]",
                l.getWorld() != null ? l.getWorld().getName() : "null",
                l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
    }
}

package ru.spawn.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

//TODO Сделать антиспам командами
public class SpawnPlugin extends JavaPlugin {

    private Location spawnLocation;

    private Message message;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSpawn();

        String locale = getConfig().getString("locale", "ru");
        this.message = new Message(this, locale);

        getLogger().info("SimpleSpawn enabled (" + locale + ")");
        getLogger().info("Spawn loaded? " + (spawnLocation != null));

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
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

        if (command.getName().equalsIgnoreCase("setspawn")) {
            if (!player.hasPermission("simple.setspawn")) {
                player.sendMessage(message.tr("noAccessToSetSpawn"));
                return true;
            }
            spawnLocation = player.getLocation();
            saveSpawn();
            player.sendMessage(message.tr("spawnSet"));
            return true;
        }

        if (command.getName().equalsIgnoreCase("spawn")) {
            if (spawnLocation == null) {
                player.sendMessage(message.tr("spawnNotSet"));
                return true;
            }
            player.teleport(spawnLocation);
            player.sendMessage(message.tr("spawnTp"));
            return true;
        }
        return false;
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
        if (spawnLocation == null) {
            return;
        }
        getConfig().set("spawn.world", spawnLocation.getWorld().getName());
        getConfig().set("spawn.x", spawnLocation.getX());
        getConfig().set("spawn.y", spawnLocation.getY());
        getConfig().set("spawn.z", spawnLocation.getZ());
        getConfig().set("spawn.yaw", spawnLocation.getYaw());
        getConfig().set("spawn.pitch", spawnLocation.getPitch());
        saveConfig();
    }
}

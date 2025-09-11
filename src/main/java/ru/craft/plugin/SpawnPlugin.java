package ru.craft.plugin;

import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import ru.craft.plugin.back.BackCommand;
import ru.craft.plugin.listeners.DeathListener;
import ru.craft.plugin.back.DeathStorage;
import ru.craft.plugin.home.HomeCommand;
import ru.craft.plugin.home.HomeManager;
import ru.craft.plugin.listeners.AntiSpamListener;
import ru.craft.plugin.listeners.JoinListener;
import ru.craft.plugin.listeners.RespawnListener;
import ru.craft.plugin.spawn.SpawnCommand;
import ru.craft.plugin.spawn.SpawnManager;
import ru.craft.plugin.util.DatabaseUtil;

import java.util.Objects;

@Getter
public class SpawnPlugin extends JavaPlugin {

    private SpawnManager spawnManager;
    private Message message;
    private DatabaseUtil databaseUtil;
    private HomeManager homeManager;
    private SpawnCommand spawnCommand;
    private DeathStorage deathStorage;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String locale = getConfig().getString("locale", "ru");
        this.message = new Message(this, locale);

        this.spawnManager = new SpawnManager(this);
        this.homeManager = new HomeManager(this);

        this.deathStorage = new DeathStorage(this);
        this.deathStorage.load();

        // Инициализация БД
        if (homeManager.isSql()) {
            databaseUtil = new DatabaseUtil(this);
            databaseUtil.setupDatabase();
            databaseUtil.createTables();
        } else {
            getLogger().info("Запись в файл");
        }

        this.spawnCommand = new SpawnCommand(null, this);
        spawnCommand.loadSpawn();

        Objects.requireNonNull(getCommand("setspawn")).setExecutor(spawnCommand);
        Objects.requireNonNull(getCommand("spawn")).setExecutor(spawnCommand);
        Objects.requireNonNull(getCommand("sethome")).setExecutor(new HomeCommand(this));
        Objects.requireNonNull(getCommand("home")).setExecutor(new HomeCommand(this));
        Objects.requireNonNull(getCommand("delhome")).setExecutor(new HomeCommand(this));
        Objects.requireNonNull(getCommand("back")).setExecutor(new BackCommand(this));


        // Слушатели
        getServer().getPluginManager().registerEvents(new JoinListener(this, spawnCommand), this);
        getServer().getPluginManager().registerEvents(new RespawnListener(this, spawnCommand), this);
        getServer().getPluginManager().registerEvents(new AntiSpamListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);


        // Логи для диагностики
        getLogger().info(message.tr("spawnEnabled") + " (" + locale + ")");
        getLogger().info("Spawn loaded? " + (spawnCommand.getSpawnLocation() != null));
    }

    @Override
    public void onDisable() {
        if (spawnCommand != null) spawnCommand.saveSpawn();
        if (databaseUtil != null) {
            try { databaseUtil.close(); } catch (Exception ignored) {}
        }
        if (message != null) {
            getLogger().info(message.tr("spawnDisable"));
        } else {
            getLogger().info("Spawn disabled.");
        }
        if (deathStorage != null) {
            deathStorage.save();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
       return false;
    }
}

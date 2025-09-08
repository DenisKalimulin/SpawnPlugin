package ru.spawn.plugin;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Objects;

public class Message {
    private final FileConfiguration config;

    public Message(SpawnPlugin plugin, String locale) {
        plugin.saveResource("messages_en.yml", false);
        plugin.saveResource("messages_ru.yml", false);

        String fileName = "messages_" + (Objects.equals(locale, "ru") ? "ru" : "en") + ".yml";
        File file = new File(plugin.getDataFolder(), fileName);
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public String tr(String key) {
        String raw = config.getString(key);
        if (raw == null) raw = "&cMissing message: " + key;
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}

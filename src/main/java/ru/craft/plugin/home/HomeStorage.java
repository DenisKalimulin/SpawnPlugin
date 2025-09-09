package ru.craft.plugin.home;

import org.bukkit.Location;

import java.util.UUID;

public interface HomeStorage {
    /**
     * Сохранение дома
     */
    void saveHome(UUID uuid, Location location) throws Exception;

    /**
     * Телепортация домой
     */
    Location loadHome(UUID uuid);

    /**
     * Удаление дома
     */
    boolean deleteHome(UUID uuid) throws Exception;
}

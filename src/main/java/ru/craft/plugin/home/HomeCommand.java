package ru.craft.plugin.home;

import lombok.AllArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.craft.plugin.SpawnPlugin;

@AllArgsConstructor
public class HomeCommand implements CommandExecutor {

    private final SpawnPlugin plugin;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(plugin.getMessage().tr("onlyPlayers"));
            return true;
        }

        switch (command.getName().toLowerCase()) {
            case "sethome":
                plugin.getHomeManager().setHome(player, player.getLocation());
                player.sendMessage(plugin.getMessage().tr("homeSet"));
                return true;
            case "home":
                var home = plugin.getHomeManager().getHome(player);
                if (home == null) {
                    player.sendMessage(plugin.getMessage().tr("homeNotSet"));
                    return true;
                }
                player.teleport(home);
                player.sendMessage(plugin.getMessage().tr("homeTeleport"));
                return true;
        }
        return false;
    }
}


package com.skle.creepercataclysm.commands.debug;

import com.skle.creepercataclysm.api.CreeperCataclysmPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetTimeCommand implements CommandExecutor {
    private final CreeperCataclysmPlugin plugin;

    public SetTimeCommand(CreeperCataclysmPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) {
            return false;
        }
        plugin.getGameManager().setTimeLeft(Integer.parseInt(args[0]));
        sender.sendMessage(ChatColor.RED + "[DEBUG] GAME TIME SET TO " + args[0] + " SECONDS");
        return true;
    }
}

package com.skle.creepercataclysm.commands;

import com.skle.creepercataclysm.api.CreeperCataclysmPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreeperCatCommands implements CommandExecutor {
    private final CreeperCataclysmPlugin plugin;

    public CreeperCatCommands(CreeperCataclysmPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) {
            return false;
        }
        player.sendMessage(ChatColor.GREEN + "CreeperCataclysm commands:\n- /play : Join the current game queue\n- /queue : Gets current queue\n- /leave : Leave queue. Only works if you are in the current queue\n- /setplayers : Set the max number of players for the game\n- /settime <Int> : Sets the time of the game to <Int>. Only works when in game\n- /abort : Ends the current game\n- /forcestart : Starts the game no matter the amount of players in the queue\n- /addgold <Int> : Adds <Int> amount of gold to the player\n - /zonelobby : Give player a zoning stick to zone the lobby \n- /zonemap : Gives player a zoning stick to zone new maps\n- /reloadconfig : Reloads config for when manual changes are made\n- /cancelzone : Cancels zoning if player currently zoning\n- /setspecial : Sets gray concrete to healing block");
        return true;
    }
}
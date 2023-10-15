package com.skle.creepercataclysm.commands.debug;

import com.skle.creepercataclysm.api.CreeperCataclysmPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SwitchTeamsCommand implements CommandExecutor {
    private final CreeperCataclysmPlugin plugin;

    public SwitchTeamsCommand(CreeperCataclysmPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) {
            return false;
        }
        if(!plugin.getGameManager().isGameStarted()) {
            player.sendMessage(ChatColor.RED + "The game has not started yet.");
            return false;
        }
        if(!plugin.getGameManager().getPlayers().contains(player)) {
            player.sendMessage(ChatColor.RED + "You are not a player in the current game.");
            return false;
        }
        if(plugin.getGameManager().getAttackers().contains(player)){
            plugin.getGameManager().getAttackers().remove(player);
            plugin.getGameManager().getDefenders().add(player);
            Bukkit.getScoreboardManager().getMainScoreboard().getTeam("attackers").removeEntry(player.getName());
            Bukkit.getScoreboardManager().getMainScoreboard().getTeam("defenders").addEntry(player.getName());
            player.setBedSpawnLocation(plugin.getGameManager().getCurrentMap().defenderspawn, true);
            plugin.getGameManager().setDefaultInventory(player, 0);
            player.teleport(plugin.getGameManager().getCurrentMap().defenderspawn);
            player.setGameMode(GameMode.ADVENTURE);
            player.setFoodLevel(20);
            player.setHealth(20);
            player.setSaturation(0);
            plugin.getGameManager().getKillMap().put(player, 0);
            player.sendMessage(ChatColor.AQUA + "You have been switched to the defenders team.");
            return true;
        } else {
            plugin.getGameManager().getDefenders().remove(player);
            plugin.getGameManager().getAttackers().add(player);
            Bukkit.getScoreboardManager().getMainScoreboard().getTeam("defenders").removeEntry(player.getName());
            Bukkit.getScoreboardManager().getMainScoreboard().getTeam("attackers").addEntry(player.getName());
            player.setBedSpawnLocation(plugin.getGameManager().getCurrentMap().attackerspawn, true);
            plugin.getGameManager().setDefaultInventory(player, 1);
            player.teleport(plugin.getGameManager().getCurrentMap().attackerspawn);
            player.setGameMode(GameMode.ADVENTURE);
            player.setFoodLevel(20);
            player.setHealth(20);
            player.setSaturation(0);
            plugin.getGameManager().getKillMap().put(player, 0);
            player.sendMessage(ChatColor.AQUA + "You have been switched to the attackers team.");
            return true;
        }

    }
}

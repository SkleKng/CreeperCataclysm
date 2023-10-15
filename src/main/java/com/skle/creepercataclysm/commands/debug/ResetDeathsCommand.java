package com.skle.creepercataclysm.commands.debug;

import com.skle.creepercataclysm.api.CreeperCataclysmPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class ResetDeathsCommand implements CommandExecutor {
    private final CreeperCataclysmPlugin plugin;

    public ResetDeathsCommand(CreeperCataclysmPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Get the scoreboard
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        scoreboard.getObjective("deaths").unregister();
        scoreboard.registerNewObjective("deaths", "deathCount", ChatColor.RED + "Deaths");
        scoreboard.getObjective("deaths").setDisplaySlot(DisplaySlot.SIDEBAR);
        for (Player player : Bukkit.getOnlinePlayers()) {
            Score deathsScore = scoreboard.getObjective("deaths").getScore(player.getName());
            deathsScore.setScore(0);
        }
        return true;
    }
}
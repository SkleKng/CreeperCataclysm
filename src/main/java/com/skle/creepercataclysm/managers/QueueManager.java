package com.skle.creepercataclysm.managers;

import com.skle.creepercataclysm.api.CreeperCataclysmPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class QueueManager {
    private List<Player> queue;
    private final CreeperCataclysmPlugin plugin;

    public QueueManager(CreeperCataclysmPlugin plugin) {
        this.plugin = plugin;
        this.queue = new ArrayList<>();
    }

    public void addToQueue(Player player) {
        if(plugin.getGameManager().isGameStarted()) {
            player.sendMessage(ChatColor.RED + "The game has already started!");
            return;
        }
        if (queue.size() >= plugin.getMaxPlayers()) {
            player.sendMessage(ChatColor.RED + "The queue is full!");
            return;
        }
        if (queue.contains(player)) {
            player.sendMessage(ChatColor.RED + "You are already in the queue!");
            return;
        }
        queue.add(player);
        for (Player p : queue) {
            p.sendMessage(ChatColor.AQUA + player.getName() + " has joined the queue! (" + queue.size() + "/" + plugin.getMaxPlayers() + ")");
        }

        if(queue.size() == plugin.getMaxPlayers()) {
            plugin.getGameManager().startGame();
        }
    }

    public void removeFromQueue(Player player) {
        if(!queue.contains(player)) {
            player.sendMessage(ChatColor.RED + "You are not in the queue!");
            return;
        }
        for (Player p : queue) {
            p.sendMessage(ChatColor.AQUA + player.getName() + " has left the queue! (" + queue.size() + "/" + plugin.getMaxPlayers() + ")");
        }
        queue.remove(player);
    }

    public List<Player> getQueue() {
        return queue;
    }

    public void resetQueue() {
        queue = new ArrayList<>();
    }
}

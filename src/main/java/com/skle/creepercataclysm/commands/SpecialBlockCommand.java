package com.skle.creepercataclysm.commands;

import com.skle.creepercataclysm.api.CreeperCataclysmPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

public class SpecialBlockCommand implements CommandExecutor {
    private final CreeperCataclysmPlugin plugin;

    public SpecialBlockCommand(CreeperCataclysmPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) {
            return false;
        }
        if(player.getInventory().getItemInMainHand().getType() == Material.GRAY_CONCRETE) {
            ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
            meta.setDisplayName("Healing Station Block");
            player.getInventory().getItemInMainHand().setItemMeta(meta);
        } else {
            player.sendMessage(ChatColor.RED + "Gray Concrete Only!");
        }
        return true;
    }
}
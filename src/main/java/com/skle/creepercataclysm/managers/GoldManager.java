package com.skle.creepercataclysm.managers;

import com.skle.creepercataclysm.api.CreeperCataclysmPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class GoldManager {
    private final CreeperCataclysmPlugin plugin;
    private final HashMap<Player, Integer> goldMap = new HashMap<>();

    public GoldManager(CreeperCataclysmPlugin plugin) {
        this.plugin = plugin;
    }

    public void initGame() {
        for (Player player : plugin.getGameManager().getPlayers()) {
            goldMap.put(player, 0);
        }
    }

    public void resetGame() {
        goldMap.clear();
    }

    /**
     * Returns the amount of gold a player has in their inventory
     * @param player
     * @return gold amount
     */
    public int getGoldInInventory(Player player) {
        //return the total amount of gold ingots in the players inventory
        int gold = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.GOLD_INGOT) {
                gold += item.getAmount();
            }
        }
        Bukkit.getLogger().info("Gold in " + player.getName() + "'s inventory: " + gold);
        return gold;
    }

    /**
     * Returns the amount of gold nuggets a player has in their inventory
     * @param player
     * @return gold nugget amount
     */
    public int getGoldNugsInInventory(Player player) {
        //return the total amount of gold ingots in the players inventory
        int goldNugs = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.GOLD_NUGGET) {
                goldNugs += item.getAmount();
            }
        }
        Bukkit.getLogger().info("Gold in " + player.getName() + "'s inventory: " + goldNugs);
        return goldNugs;
    }

    /**
     * Sets the amount of gold a player has
     * @param player player to set gold for
     * @param amount amount of gold to set
     */
    public void addGold(Player player, int amount) {
        goldMap.put(player, goldMap.get(player) + amount);
        setPlayerVisibleGold(player, amount);
    }

    public void removeGold(Player player, int amount) {
        goldMap.put(player, goldMap.get(player) - amount);
        setPlayerVisibleGold(player, -amount);
    }

    /**
     * Sets the amount of gold a player has
     * @param player player to set gold for
     * @param amount amount of gold to set
     */
    public void addGoldNug(Player player, int amount) {
        if(getGoldNugsInInventory(player) + amount == 2){
            removeGoldNug(player, 2);
            addGold(player, 1);
        }
        else{
            setPlayerVisibleGoldNugs(player, amount);
        }
    }

    public void removeGoldNug(Player player, int amount) {
        setPlayerVisibleGoldNugs(player, -amount);
    }

    private void setPlayerVisibleGold(Player player, int amount) {
        if(amount > 0)
            player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, amount));
        else if(amount < 0)
            player.getInventory().removeItem(new ItemStack(Material.GOLD_INGOT, -amount));
    }

    private void setPlayerVisibleGoldNugs(Player player, int amount) {
        if(amount > 0)
            player.getInventory().addItem(new ItemStack(Material.GOLD_NUGGET, amount));
        else if(amount < 0)
            player.getInventory().removeItem(new ItemStack(Material.GOLD_NUGGET, -amount));
    }
}

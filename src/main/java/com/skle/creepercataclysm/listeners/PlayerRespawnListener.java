package com.skle.creepercataclysm.listeners;

import com.skle.creepercataclysm.api.CreeperCataclysmPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerRespawnListener implements Listener {
    private final CreeperCataclysmPlugin plugin;

    public PlayerRespawnListener(CreeperCataclysmPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        if(!plugin.getGameManager().isGameStarted()) return;
        if(!plugin.getGameManager().getPlayers().contains(event.getEntity())) return;
        Player victim = event.getEntity();
        //Set the victim's steak to 8 and arrows to 5
        ItemStack steak = new ItemStack(Material.COOKED_BEEF, 8);
        ItemStack arrows;
        if(plugin.getGameManager().getDefenders().contains(victim)){
            arrows = new ItemStack(Material.ARROW, 3);
        }
        else{
            arrows = new ItemStack(Material.ARROW, 5);
        }
        for(ItemStack item : victim.getInventory().getContents()){
            if(item == null) continue;
            if(item.getType() == Material.COOKED_BEEF) {
                steak.setAmount(steak.getAmount() - item.getAmount());
            }
            else if(item.getType() == Material.ARROW) {
                arrows.setAmount(arrows.getAmount() - item.getAmount());
            }
        }
        victim.getInventory().addItem(steak);
        victim.getInventory().addItem(arrows);

    }
}

package com.skle.creepercataclysm.listeners;

import com.skle.creepercataclysm.api.CreeperCataclysmPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;

public class PlayerRespawnListener implements Listener {
    private final CreeperCataclysmPlugin plugin;

    public PlayerRespawnListener(CreeperCataclysmPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        if(!plugin.getGameManager().isGameStarted()) return;
        if(!plugin.getGameManager().getPlayers().contains(event.getEntity())) return;
        if(plugin.getGameManager().getCurrentMap().name.equals("Scorched Earth")) return;

        Player victim = event.getEntity();
        //Set the victim's steak to 8 and arrows to 5
        ItemStack arrows;
        arrows = new ItemStack(Material.ARROW, 3);
        victim.teleport(plugin.getGameManager().getCurrentMap().defenderspawn);
        for(ItemStack item : victim.getInventory().getContents()){
            if(item == null) continue;
            if(item.getType() == Material.ARROW) {
                arrows.setAmount(arrows.getAmount() - item.getAmount());
            }
        }
        victim.getInventory().addItem(arrows);

    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        plugin.getGameManager().getPlayerKillMap().put(event.getPlayer(), 0);
        if(!plugin.getGameManager().isGameStarted()){
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    if(!event.getPlayer().getInventory().contains(Material.STICK)){
                        ItemStack knockbackstick = new ItemStack(Material.STICK);
                        ItemMeta stickmeta = knockbackstick.getItemMeta();
                        stickmeta.setDisplayName(ChatColor.RED + "Knockback Stick");
                        stickmeta.addEnchant(Enchantment.KNOCKBACK, 10, true);
                        knockbackstick.setItemMeta(stickmeta);
                        event.getPlayer().getInventory().addItem(knockbackstick);
                    }
                    event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, PotionEffect.INFINITE_DURATION, 3));
                }
            }, 1L);
            return;
        }
        if(!plugin.getGameManager().getPlayers().contains(event.getPlayer())) return;
        Player victim = event.getPlayer();
        if(plugin.getGameManager().getDefenders().contains(victim)){
            victim.teleport(plugin.getGameManager().getCurrentMap().defenderspawn);
        }
        else{
            victim.teleport(plugin.getGameManager().getCurrentMap().attackerspawn);
        }
        victim.setHealth(victim.getHealth() + (victim.getHealth() > 16 ? (20 - victim.getHealth()) : 4));
        for (ItemStack item : victim.getInventory().getContents()) {
            if (item == null) continue;
            if (item.getType() == Material.GOAT_HORN) {
                item.setAmount(item.getAmount() - 1);
            }
        }
        if(plugin.getGameManager().getDefenders().contains(victim)){
            plugin.getGameManager().getKillMap().put(victim, plugin.getGameManager().getDefenderGoldStart());
        }
        else{
            plugin.getGameManager().getKillMap().put(victim, plugin.getGameManager().getAttackerGoldStart());
        }
        plugin.getGameManager().getDamageMap().put(victim, new HashMap<>());
    }
}

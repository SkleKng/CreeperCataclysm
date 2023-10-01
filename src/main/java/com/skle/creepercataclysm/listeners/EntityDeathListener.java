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

public class EntityDeathListener implements Listener {
    private final CreeperCataclysmPlugin plugin;

    public EntityDeathListener(CreeperCataclysmPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event){
        if(!plugin.getGameManager().isGameStarted()) return;
        if(event.getEntity() == plugin.getGameManager().getCreeper()) {
            event.getDrops().clear(); // ending game is handled elsewhere
            return;
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        if(!plugin.getGameManager().isGameStarted()) return;
        EntityDamageEvent e = event.getEntity().getLastDamageCause();
        Player attacker = event.getEntity().getKiller();
        Player victim = event.getEntity();
        if(attacker.equals(victim)) return;
        if(!(plugin.getGameManager().getPlayers().contains(attacker) && plugin.getGameManager().getPlayers().contains(victim))) return;
        if(plugin.getGameManager().getKillMap().get(attacker) < 3){
            plugin.getGameManager().getKillMap().put(attacker, plugin.getGameManager().getKillMap().get(attacker) + 1);
        }
        plugin.getGoldManager().addGold(attacker, plugin.getGameManager().getKillMap().get(attacker));
        plugin.getGameManager().getKillMap().put(victim, 0);
    }
}

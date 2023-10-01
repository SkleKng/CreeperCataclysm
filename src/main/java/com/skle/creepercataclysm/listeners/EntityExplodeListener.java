package com.skle.creepercataclysm.listeners;

import com.skle.creepercataclysm.api.CreeperCataclysmPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.util.Vector;

import java.util.Random;

public class EntityExplodeListener implements Listener {
    private final CreeperCataclysmPlugin plugin;

    public EntityExplodeListener(CreeperCataclysmPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void EntityExplode(EntityExplodeEvent event){
        if(event.getEntity() instanceof Creeper creeper) {
            if(!(plugin.getGameManager().isGameStarted())) return;
            event.blockList().clear();
            for (Entity entity : creeper.getNearbyEntities(creeper.getExplosionRadius(), creeper.getExplosionRadius(), creeper.getExplosionRadius())) {
                if(entity instanceof Player player) {
                    Vector direction = player.getLocation().toVector().subtract(creeper.getLocation().toVector());
                    double launchStrength = 30;
                    player.setVelocity(direction.normalize().multiply(launchStrength));
                }
            }
        }
        if(!(event.getEntity() instanceof Fireball fireball)) return;
        event.blockList().clear();
        if(!(plugin.getGameManager().isGameStarted())) return;
        for (Entity entity : fireball.getNearbyEntities(fireball.getYield(), fireball.getYield(), fireball.getYield())) {
            if(entity instanceof Player player) {
                Vector direction = player.getLocation().toVector().subtract(fireball.getLocation().toVector());
                double launchStrength = 1.4;
                player.setVelocity(direction.normalize().multiply(launchStrength));
            }
        }
    }

    @EventHandler
    public void BlockChangedEvent(EntityChangeBlockEvent event) {
        if(!(plugin.getGameManager().isGameStarted())) return;
        if(event.getBlock().getType() == Material.POWDER_SNOW && event.getTo() == Material.AIR) {
            event.setCancelled(true);
        }
        if(event.getBlock().getType() == Material.ITEM_FRAME && event.getTo() == Material.AIR) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void noFireballFire(BlockIgniteEvent event){
        if(event.getCause().equals(BlockIgniteEvent.IgniteCause.FIREBALL)) event.setCancelled(true);
    }
}

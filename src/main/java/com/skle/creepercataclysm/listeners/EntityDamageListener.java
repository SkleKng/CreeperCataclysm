package com.skle.creepercataclysm.listeners;

import com.skle.creepercataclysm.api.CreeperCataclysmPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class EntityDamageListener implements Listener {
    private final CreeperCataclysmPlugin plugin;

    public EntityDamageListener(CreeperCataclysmPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) throws InterruptedException {
        if(!plugin.getGameManager().isGameStarted()) return;

        if(event.getEntity().equals(plugin.getGameManager().getCreeper())) {
            if(event.getDamager() instanceof Player player) {
                if(plugin.getGameManager().getAttackers().contains(player)) {
                    plugin.getGameManager().notifyCreeperHit();
                    return;
                }
            }
            else if(event.getDamager() instanceof Arrow arrow) {
                if(!(arrow.getShooter() instanceof Player player)) return;
                if(!plugin.getGameManager().getDefenders().contains(player)) return;
            }
        }

        if(event.getEntity() instanceof Player attacked) {
            if(event.getDamager() instanceof Creeper){
                event.setCancelled(true);
            }
            Player attacker;
            if(event.getDamager() instanceof Player) {
                attacker = (Player) event.getDamager();
            }
            else if(event.getDamager() instanceof Arrow arrow) {
                if(!(arrow.getShooter() instanceof Player player)) return;
                attacker = player;
                if((!(plugin.getGameManager().getAttackers().contains(attacker) && plugin.getGameManager().getAttackers().contains(attacked))) && (!(plugin.getGameManager().getDefenders().contains(attacker) && plugin.getGameManager().getDefenders().contains(attacked))) ){
                    attacker.getInventory().addItem(new ItemStack(Material.ARROW, 1));
                    updateDamage(attacker, attacked, event.getDamage());
                }

            }
            else if(event.getDamager() instanceof Fireball) {
                Bukkit.getLogger().info("Fireball damage");
                if(((Fireball) event.getDamager()).getShooter() == attacked) {
                    event.setDamage(1.0);
                }
                else {
                    event.setDamage(event.getDamage()/2.5);
                }
                return;
            }
            else return;
            if(plugin.getGameManager().getDefenders().contains(attacked) && plugin.getGameManager().getAttackers().contains(attacker)) return;
            if(plugin.getGameManager().getAttackers().contains(attacked) && plugin.getGameManager().getDefenders().contains(attacker)) return;

        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event)
    {
        Bukkit.getLogger().info("Projectile hit");
        Projectile p = event.getEntity();
        if(p instanceof Arrow) {
            p.remove();
        }
        if(p instanceof Fireball fireball) {
            Bukkit.getLogger().info("Fireball hit");
            Block blockhit = event.getHitBlock();
            if(blockhit != null && blockhit.getType() == Material.POWDER_SNOW) {
                event.setCancelled(true);
                Bukkit.getLogger().info("Fireball hit powder snow");
            }
        }
        return;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) throws InterruptedException {
        if(event.getEntity() instanceof Player player) {
            if(event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
                if(player.getLocation().getY() == -60 && player.getLocation().getBlock().getType() == Material.LAVA){
                    player.setHealth(0);
                }
            }
        }

        if(!plugin.getGameManager().isGameStarted()) return;
        if(event.getEntity().equals(plugin.getGameManager().getCreeper())) {
            if(plugin.getGameManager().getCreeper().getHealth() - event.getDamage() <= 0) {
                event.setCancelled(true);
                Location loc = plugin.getGameManager().getCreeper().getLocation();
                int i = 1;
                double z = loc.getZ();
                loc.setX(loc.getX() - 7);
                loc.setY(loc.getY() - 1);
                loc.setYaw(-90);
                for(Player p: plugin.getGameManager().getAttackers()) {
                    loc.setZ(z + i);
                    i++;
                    p.teleport(loc);
                    p.sendTitle(ChatColor.RED + "Attackers Win!", "", 10, 40, 10);
                }
                i = 1;
                for(Player p: plugin.getGameManager().getDefenders()) {
                    loc.setZ(z - i);
                    p.teleport(loc);
                    p.sendTitle(ChatColor.RED + "Attackers Win!", "", 10, 40, 10);
                    i++;
                }
                plugin.getGameManager().setGameEnded(true);
                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        plugin.getGameManager().getCreeper().ignite();
                    }
                }, 30L);

            }
        }
    }

    @EventHandler
    public void onPlayerHitPlayerEnd(EntityDamageByEntityEvent event){
        if(!plugin.getGameManager().isGameStarted()) return;
        if(!plugin.getGameManager().isGameEnded()) return;
        if(event.getEntity() instanceof Player a && event.getDamager() instanceof Player b){
            if(plugin.getGameManager().getPlayers().contains(a) && plugin.getGameManager().getPlayers().contains(b)){
                event.setCancelled(true);
            }
        }
    }

    public void updateDamage(Player attacker, Player victim, double damage) {
        if (plugin.getGameManager().getDamageMap().containsKey(victim)) {
            HashMap<Player, Double> victimDamageMap = plugin.getGameManager().getDamageMap().get(victim);

            if (victimDamageMap.containsKey(attacker)) {
                double currentDamage = victimDamageMap.get(attacker);
                victimDamageMap.put(attacker, currentDamage + damage);
            } else {
                victimDamageMap.put(attacker, damage);
            }
        } else {
            HashMap<Player, Double> victimDamageMap = new HashMap<>();
            victimDamageMap.put(attacker, damage);

            plugin.getGameManager().getDamageMap().put(victim, victimDamageMap);
        }
    }

    @EventHandler
    public void onPlayerHitPlayer(EntityDamageByEntityEvent event){
        if(!plugin.getGameManager().isGameStarted()) return;
        if(event.getDamager().equals(event.getEntity())) return;
        if(event.getEntity().equals(plugin.getGameManager().getCreeper()) && event.getDamager() instanceof Player attacker) {
            plugin.getGameManager().getTotalCreeperDamage().put(attacker, plugin.getGameManager().getTotalCreeperDamage().get(attacker) + event.getDamage());
        }
        if(event.getEntity() instanceof Player victim && event.getDamager() instanceof Player attacker){
            if(plugin.getGameManager().getPlayers().contains(victim) && plugin.getGameManager().getPlayers().contains(attacker)){
                updateDamage(attacker, victim, event.getDamage());
            }
        }
    }
}

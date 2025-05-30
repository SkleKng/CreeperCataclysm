package com.skle.creepercataclysm.listeners;

import com.skle.creepercataclysm.api.CreeperCataclysmPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import static org.bukkit.event.player.PlayerFishEvent.State.FAILED_ATTEMPT;
import static org.bukkit.event.player.PlayerFishEvent.State.IN_GROUND;

public class EntityInteractListener implements Listener {
    private final CreeperCataclysmPlugin plugin;

    public EntityInteractListener(CreeperCataclysmPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void PlayerInteractWithEntity(PlayerInteractEntityEvent event){
        if(!(plugin.getGameManager().isGameStarted())) return;
        if(!(event.getRightClicked() instanceof Villager villager)) return;
        if(villager.equals(plugin.getShopManager().getDefenderVillager())) {
            event.getPlayer().openInventory(plugin.getShopManager().getDefenderShop());
        }
        else if(villager.equals(plugin.getShopManager().getAttackerVillager())) {
            event.getPlayer().openInventory(plugin.getShopManager().getAttackerShop());
        }
        else return;
    }

    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent event) {
        //See if the player is zoning
        if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(event.getItem() == null) return;
            Player player = event.getPlayer();
            if(event.getItem().equals(plugin.getZoneManager().getLobbyZoneWand())) {
                plugin.getZoneManager().setLobbyZone(player);
            }
            else if (event.getItem().equals(plugin.getZoneManager().getMapZoneWand())) {
                plugin.getZoneManager().setMapZone(player);
            }
            else if(plugin.getGameManager().isGameStarted() && plugin.getGameManager().getPlayers().contains(player) && event.getItem().getType().equals(Material.FIRE_CHARGE)) {
                Fireball fireball = player.launchProjectile(Fireball.class);
                fireball.setYield(2.5f);
                Vector direction = player.getLocation().getDirection();
                fireball.setVelocity(direction.multiply(1.75));
                fireball.setIsIncendiary(false);
                event.getItem().setAmount(event.getItem().getAmount() - 1);
            }
            else if(plugin.getGameManager().isGameStarted() && plugin.getGameManager().getPlayers().contains(player) && event.getItem().getType().equals(Material.GOAT_HORN)){
                event.getItem().setAmount(event.getItem().getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 0));
                player.stopAllSounds();
                for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
                    if(entity instanceof Player nearbyPlayer) {
                        if(plugin.getGameManager().getDefenders().contains(player)) {
                            if(plugin.getGameManager().getDefenders().contains(nearbyPlayer)) {
                                nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 0));
                            }
                        }
                        else if(plugin.getGameManager().getAttackers().contains(player)) {
                            if(plugin.getGameManager().getAttackers().contains(nearbyPlayer)) {
                                nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 0));
                            }
                        }
                        nearbyPlayer.stopAllSounds();
                    }
                }
                player.stopAllSounds();
                if(plugin.getGameManager().getAttackers().contains(player)){
                    for(Player p : plugin.getGameManager().getPlayers()){
                        p.playSound(p.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_5, 5, 1);
                    }
                }
                else{
                    for(Player p : plugin.getGameManager().getPlayers()){
                        p.playSound(p.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_0, 5, 1);
                    }
                }
            }
         }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        FishHook hook = event.getHook();
        if (!(player.getInventory().getItemInMainHand().equals(plugin.getZoneManager().getGrapplingHook()))) {
            return;
        }

        if (event.getState() == PlayerFishEvent.State.REEL_IN || event.getState() == IN_GROUND) {
            Vector hookLocation = hook.getLocation().toVector();
            Vector playerLocation = player.getEyeLocation().toVector();
            Vector direction = hookLocation.subtract(playerLocation);

            direction.setY(Math.abs(direction.multiply(0.4).getY()));
            player.setVelocity(direction);
            hook.remove();
            player.sendMessage(ChatColor.GOLD + "Speed (delta) X" + direction.getX());
            player.sendMessage(ChatColor.GOLD + "Speed (delta) Y" + direction.getY());
            player.sendMessage(ChatColor.GOLD + "Speed (delta) Z" + direction.getZ());
            player.sendMessage(ChatColor.GOLD + "Player Current Speed X" + player.getVelocity().getX());
            player.sendMessage(ChatColor.GOLD + "Player Current Speed Y" + player.getVelocity().getY());
            player.sendMessage(ChatColor.GOLD + "Player Current Speed Z" + player.getVelocity().getZ());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Block block = player.getLocation().subtract(0, 1, 0).getBlock();
        String blockName = "Block" + block.getLocation().getBlockX() + block.getLocation().getBlockY() + block.getLocation().getBlockZ();
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("specialBlocks");
        if(config.contains(blockName) && player.getWorld().getName().equals(config.getString(blockName + ".world"))) {
            if(config.getBoolean(blockName + ".enabled")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 2));
                block.setType(Material.RED_CONCRETE);
                config.set(blockName + ".enabled", false);
                plugin.saveConfig();
                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        if(block.getType().equals(Material.RED_CONCRETE)){
                            block.setType(Material.LIME_CONCRETE);
                            config.set(blockName + ".enabled", true);
                            plugin.saveConfig();
                        }
                    }
                }, 300L);
            }
        }
    }
}

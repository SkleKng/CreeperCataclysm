package com.skle.creepercataclysm.listeners;

import com.skle.creepercataclysm.api.CreeperCataclysmPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public class InventoryListener implements Listener {
    private final CreeperCataclysmPlugin plugin;

    private InventoryAction[] actions = {
        InventoryAction.HOTBAR_MOVE_AND_READD,
        InventoryAction.HOTBAR_SWAP,
        InventoryAction.MOVE_TO_OTHER_INVENTORY,
        InventoryAction.PICKUP_ALL,
        InventoryAction.PICKUP_HALF,
        InventoryAction.PICKUP_ONE,
        InventoryAction.PICKUP_SOME,
        InventoryAction.PLACE_ALL,
        InventoryAction.PLACE_ONE,
        InventoryAction.PLACE_SOME,
        InventoryAction.SWAP_WITH_CURSOR
    };

    public InventoryListener(CreeperCataclysmPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void InventoryAction(InventoryClickEvent event){
        if(!(plugin.getGameManager().isGameStarted())) return;
        if(!(event.getWhoClicked() instanceof Player player)) return;
        if(!(plugin.getGameManager().getPlayers().contains(player))) return;
        Inventory actionInventory = event.getClickedInventory();
        if(actionInventory == null) return;
        if(!(actionInventory.equals(plugin.getShopManager().getDefenderShop())) && !(actionInventory.equals(plugin.getShopManager().getAttackerShop()))) return;
        for(InventoryAction action : actions) {
            if(event.getAction().equals(action)) {
                event.setCancelled(true);
                plugin.getShopManager().attemptBuy(player, actionInventory, event.getSlot());
                return;
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (event.getItemInHand().getType().equals(Material.GRAY_CONCRETE) && event.getItemInHand().getItemMeta().getDisplayName().equals("Healing Station Block")) {
            event.getBlock().setType(Material.LIME_CONCRETE);
            saveBlockToConfig("specialBlocks." + "Block" + event.getBlock().getLocation().getBlockX() + event.getBlock().getLocation().getBlockY() + event.getBlock().getLocation().getBlockZ(), event.getBlock());
        }
    }

    public void saveBlockToConfig(String key, Block block){
        plugin.getConfig().set(key + ".world", block.getWorld().getName());
        plugin.getConfig().set(key + ".enabled", true);
        plugin.reloadPluginConfig();
    }

    @EventHandler
    public void onBlockDestroy(BlockBreakEvent event){
        Block block = event.getBlock();
        String blockName = "Block" + block.getLocation().getBlockX() + block.getLocation().getBlockY() + block.getLocation().getBlockZ();
        ConfigurationSection specialBlocks = plugin.getConfig().getConfigurationSection("specialBlocks");
        if(specialBlocks.contains(blockName) && specialBlocks.getString(blockName + ".world").equals(event.getBlock().getWorld().getName())){
            specialBlocks.set(blockName, null);
            plugin.reloadPluginConfig();
        }
    }
}

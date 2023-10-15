package com.skle.creepercataclysm.managers;

import com.skle.creepercataclysm.api.CreeperCataclysmPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

public class ShopManager {
    private final CreeperCataclysmPlugin plugin;

    private final Inventory defenderShop;
    private final Inventory attackerShop;
//    private final Location defenderShopLocation = new Location(Bukkit.getWorlds().get(0), 21.5, -59, 83.5, -90, 0);
//    private final Location attackerShopLocation = new Location(Bukkit.getWorlds().get(0), 21.5, -59, 57.5, -90, 0);
    private Villager defenderVillager;
    private Villager attackerVillager;
    private final ShopItem[] defenderShopItems = {
            new ShopItem(Material.STONE_SWORD,
                    5, 0, "Stone Sword", "A sword made of stone.",
                    new Material[]{ // Items to override
                            Material.WOODEN_SWORD
                    }),
            new ShopItem(Material.IRON_SWORD,
                    15, 1, "Iron Sword", "A sword made of iron.",
                    new Material[]{ // Items to override
                            Material.WOODEN_SWORD,
                            Material.STONE_SWORD
                    }),
            new ShopItem(Material.FIRE_CHARGE,
                    2, 2, "Fireball", "A fireball that can be thrown at enemies, yourself, or barriers.",
                    new Material[]{}),
            new ShopItem(Material.GOLDEN_APPLE,
                    1, 3, "Golden Apple", "An apple that gives you regeneration",
                    new Material[]{}),
            new ShopItem(Material.SHIELD,
                    15, 4, "Shield", "A shield that can be used to block attacks.",
                    new Material[]{}),
            new ShopItem(Material.GOAT_HORN,
                    5, 5, "Goat Horn", "When played it gives ally players near you a strength boost. Try not die with it :)",
                    new Material[]{})
    };

    private final ShopItem[] attackerShopItems = {
            new ShopItem(Material.STONE_SWORD,
                    5, 0, "Stone Sword", "A sword made of stone.",
                    new Material[]{ // Items to override
                            Material.WOODEN_SWORD
                    }),
            new ShopItem(Material.IRON_SWORD,
                    15, 1, "Iron Sword", "A sword made of iron.",
                    new Material[]{ // Items to override
                            Material.WOODEN_SWORD,
                            Material.STONE_SWORD
                    }),
            new ShopItem(Material.FIRE_CHARGE,
                    2, 2, "Fireball", "A fireball that can be thrown at enemies, yourself, or barriers.",
                    new Material[]{}),
            new ShopItem(Material.GOLDEN_APPLE,
                    1, 3, "Golden Apple", "An apple that gives you regeneration",
                    new Material[]{}),
            new ShopItem(Material.SHIELD,
                    15, 4, "Shield", "A shield that can be used to block attacks.",
                    new Material[]{}),
            new ShopItem(Material.GOAT_HORN,
                    5, 5, "Goat Horn", "When played it gives ally players near you a strength boost. Try not die with it :)",
                    new Material[]{})
    };

    private static class ShopItem {
        public Material item;
        public int cost;
        public int slot;
        public String name;
        public String description;
        public Material[] itemToReplace;

        public ItemStack displayStack;
        public ItemStack purchaseStack;

        public ShopItem(Material item, int cost, int slot, String name, String description) {
            this(item, cost, slot, name, description, null);
        }

        public ShopItem(Material item, int cost, int slot, String name, String description, Material[] itemToReplace) {
            this.item = item;
            this.cost = cost;
            this.slot = slot;
            this.name = name;
            this.description = description;
            this.itemToReplace = itemToReplace;

            displayStack = new ItemStack(item);
            ItemMeta displayMeta = displayStack.getItemMeta();
            displayMeta.setDisplayName("§r" + name);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Cost: " + ChatColor.GOLD + cost + " Gold");
            lore.add("\n");
            lore.add(ChatColor.GRAY + description);
            displayMeta.setLore(lore);
            displayMeta.setUnbreakable(true);
            displayStack.setItemMeta(displayMeta);

            purchaseStack = new ItemStack(item);
            ItemMeta purchaseMeta = purchaseStack.getItemMeta();
            purchaseMeta.setDisplayName("§r" + name);
            purchaseMeta.setUnbreakable(true);
            purchaseStack.setItemMeta(purchaseMeta);
        }
    }

    public ShopManager(CreeperCataclysmPlugin plugin) {
        this.plugin = plugin;

        defenderShop = plugin.getServer().createInventory(null, 9, ChatColor.BLUE + "Defender Shop");
        for(ShopItem item : defenderShopItems) {
            defenderShop.setItem(item.slot, item.displayStack);
        }

        attackerShop = plugin.getServer().createInventory(null, 9, ChatColor.RED + "Attacker Shop");
        for(ShopItem item : attackerShopItems) {
            attackerShop.setItem(item.slot, item.displayStack);
        }
    }

    public void initShop() {
        defenderVillager = plugin.getGameManager().getCurrentMap().defendervillagerspawn.getWorld().spawn(plugin.getGameManager().getCurrentMap().defendervillagerspawn, Villager.class);
        defenderVillager.setAI(false);
        defenderVillager.setInvulnerable(true);

        attackerVillager = plugin.getGameManager().getCurrentMap().attackervillagerspawn.getWorld().spawn(plugin.getGameManager().getCurrentMap().attackervillagerspawn, Villager.class);
        attackerVillager.setAI(false);
        attackerVillager.setInvulnerable(true);
    }

    public void attemptBuy(Player player, Inventory inventory, int slot) {
        ShopItem[] shopItems = inventory == defenderShop ? defenderShopItems : attackerShopItems;
        ShopItem item = null;
        for(ShopItem shopItem : shopItems) {
            if(shopItem.slot == slot) {
                item = shopItem;
                break;
            }
        }
        if(item == null) { return; }
        if (plugin.getGoldManager().getGoldInInventory(player) >= item.cost) {
            plugin.getGoldManager().removeGold(player, item.cost);
            if(item.itemToReplace != null) {
                Bukkit.getLogger().info("Replacing items");
                for(Material material : item.itemToReplace) {
                    Bukkit.getLogger().info("Replacing " + material.name());
                    player.getInventory().remove(material);
                }
            }
            player.getInventory().addItem(item.purchaseStack);
            player.sendMessage(ChatColor.GREEN + "You bought " + item.name + " for " + item.cost + " gold!");
        }
        else {
            player.sendMessage(ChatColor.RED + "You don't have enough gold to buy " + item.name + "! You need " + (item.cost - plugin.getGoldManager().getGoldInInventory(player)) + " more gold!");
        }
    }

    public void resetShop() {
        defenderVillager.remove();
        attackerVillager.remove();
    }

    public Villager getDefenderVillager() {
        return defenderVillager;
    }

    public Villager getAttackerVillager() {
        return attackerVillager;
    }

    public Inventory getDefenderShop() {
        return defenderShop;
    }

    public Inventory getAttackerShop() {
        return attackerShop;
    }
}

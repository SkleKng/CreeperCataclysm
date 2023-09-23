package com.skle.creepercataclysm.managers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.skle.creepercataclysm.api.CreeperCataclysmPlugin;
import com.skle.creepercataclysm.packets.WrapperPlayServerEntityMetadata;
import org.bukkit.*;

import java.util.*;

import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;

import java.text.DecimalFormat;
import java.util.Collections;

public class GameManager {
    private final CreeperCataclysmPlugin plugin;

    private Location lobbySpawn;

    private boolean gameStarted = false;

    private boolean gameEnded = false;

    private List<Player> players = new ArrayList<>();
    private List<Player> defenders = new ArrayList<>();
    private List<Player> attackers = new ArrayList<>();

    private HashMap<Player, Integer> killMap = new HashMap<>();
    private ScoreboardManager manager;

    private Scoreboard board;
    private Team scoreAttackers;
    private Team scoreDefenders;

    private Team seeGlow;


    private Creeper creeper;
    private BossBar bossBar;

    private int timeLeft;
    private int totalTime;

    private int lastCreeperHitTime;

    private List<GameMap> maps;
    private GameMap currentMap;

    public GameManager(CreeperCataclysmPlugin plugin){
        this.plugin = plugin;
        manager = Bukkit.getScoreboardManager();
        board = manager.getMainScoreboard();
        initConfig();
    }

    public void notifyCreeperHit() {
        int lastHit = lastCreeperHitTime;
        lastCreeperHitTime = timeLeft;
        if(lastHit - lastCreeperHitTime > 5) {
            for (Player player : getDefenders()) {
                player.sendMessage(ChatColor.RED + "§lThe creeper is under attack!");
                soundAlarm(player);
            }
        }
    }
    public void soundAlarm(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if(lastCreeperHitTime - timeLeft > 2 || !isGameStarted()) {
                    cancel();
                    return;
                }
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 9);
            }
        }.runTaskTimer(plugin,0, 10);
    }

    public class GameMap {
        public final String name;
        public final Location attackerspawn;
        public final Location attackervillagerspawn;
        public final Location defenderspawn;
        public final Location defendervillagerspawn;
        public final Location creeperspawn;

        public GameMap(String name, Location attackerspawn, Location attackervillager, Location defenderspawn, Location defendervillager, Location creeper) {
            this.name = name;
            this.attackerspawn = attackerspawn;
            this.attackervillagerspawn = attackervillager;
            this.defenderspawn = defenderspawn;
            this.defendervillagerspawn = defendervillager;
            this.creeperspawn = creeper;
        }
    }

    public void initConfig() {
        maps = new ArrayList<>();
        FileConfiguration config = plugin.getPluginConfig();
        ConfigurationSection lobby = config.getConfigurationSection("lobby");
        ConfigurationSection maps = config.getConfigurationSection("maps");
        ConfigurationSection specialBlocks = config.getConfigurationSection("specialBlocks");
        if(maps == null || lobby == null) {
            Bukkit.getLogger().severe("No maps found in config!");
            Bukkit.getLogger().severe("No maps found in config!");
            Bukkit.getLogger().severe("No maps found in config!");
            return;
        }
        lobbySpawn = new Location(Bukkit.getWorld(lobby.getString("world")), lobby.getDouble("x"), lobby.getDouble("y"), lobby.getDouble("z"), (float)lobby.getDouble("yaw"), (float)lobby.getDouble("pitch"));
        Bukkit.getLogger().info("Lobby spawn set to " + lobbySpawn.toString());
        for(String mapKey : maps.getKeys(false)) { // HOLY SHIT THIS IS AWFUL PLEASE PLEASE PLEASE FIGURE OUT A BETTER WAY
            ConfigurationSection mapData = maps.getConfigurationSection(mapKey);
            String mapName = mapKey;
            Location attackerspawn = new Location(Bukkit.getWorld(mapData.getString("attackerspawn.world")), mapData.getDouble("attackerspawn.x"), mapData.getDouble("attackerspawn.y"), mapData.getDouble("attackerspawn.z"), (float)mapData.getDouble("attackerspawn.yaw"), (float)mapData.getDouble("attackerspawn.pitch"));
            Location attackervillager = new Location(Bukkit.getWorld(mapData.getString("attackervillager.world")), mapData.getDouble("attackervillager.x"), mapData.getDouble("attackervillager.y"), mapData.getDouble("attackervillager.z"), (float)mapData.getDouble("attackervillager.yaw"), (float)mapData.getDouble("attackervillager.pitch"));
            Location defenderspawn = new Location(Bukkit.getWorld(mapData.getString("defenderspawn.world")), mapData.getDouble("defenderspawn.x"), mapData.getDouble("defenderspawn.y"), mapData.getDouble("defenderspawn.z"), (float)mapData.getDouble("defenderspawn.yaw"), (float)mapData.getDouble("defenderspawn.pitch"));
            Location defendervillager = new Location(Bukkit.getWorld(mapData.getString("defendervillager.world")), mapData.getDouble("defendervillager.x"), mapData.getDouble("defendervillager.y"), mapData.getDouble("defendervillager.z"), (float)mapData.getDouble("defendervillager.yaw"), (float)mapData.getDouble("defendervillager.pitch"));
            Location creeper = new Location(Bukkit.getWorld(mapData.getString("creeper.world")), mapData.getDouble("creeper.x"), mapData.getDouble("creeper.y"), mapData.getDouble("creeper.z"), (float)mapData.getDouble("creeper.yaw"), (float)mapData.getDouble("creeper.pitch"));
            GameMap gameMap = new GameMap(mapName, attackerspawn, attackervillager, defenderspawn, defendervillager, creeper);
            this.maps.add(gameMap);
        }
    }

    public void startGame() {
        gameEnded = false;
        killMap = new HashMap<>();
        if(board.getTeam("attackers") != null) {
            scoreAttackers = board.getTeam("attackers");
        }
        else {
            scoreAttackers = board.registerNewTeam("attackers");
        }
        if(board.getTeam("defenders") != null) {
            scoreDefenders = board.getTeam("defenders");
        }
        else {
            scoreDefenders = board.registerNewTeam("defenders");
        }
        scoreDefenders.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
        scoreDefenders.setColor(ChatColor.BLUE);
        scoreAttackers.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
        scoreAttackers.setColor(ChatColor.RED);
        Bukkit.getLogger().info("Amount of maps: " + maps.size());
        if(maps.size() <= 0) {
            plugin.getQueueManager().getQueue().forEach(player -> player.sendMessage(ChatColor.RED + "No maps found in config!"));
            Bukkit.getLogger().severe("No maps found in config!");
            return;
        }
        currentMap = maps.get(new Random().nextInt(maps.size()));
        Bukkit.getLogger().info("Game has begun with map " + currentMap.name + "!");
        this.gameStarted = true;
        initPlayers();
        initGold();
        initShop();
        timeLeft = (60 * 5) + ((attackers.size() - 1) * 60);
        totalTime = (60 * 5) + ((attackers.size() - 1) * 60);
        lastCreeperHitTime = timeLeft;
    }

    private void initGold() {
        plugin.getGoldManager().initGame();
    }

    private void initShop() {
        plugin.getShopManager().initShop();
    }

    private void initCreeper() {
        creeper = currentMap.creeperspawn.getWorld().spawn(currentMap.creeperspawn, Creeper.class);
        creeper.setPowered(true);
        creeper.setAI(false);
        int creeperhealth = 500 + (100 * attackers.size());
        creeper.setMaxHealth(creeperhealth);
        creeper.setMaxFuseTicks(20);
        creeper.setExplosionRadius(30);
        creeper.setHealth(creeperhealth);
        creeper.setCustomName(ChatColor.GREEN + "CORE");
        creeper.setRemoveWhenFarAway(false);
    }

    private void initPlayers() {
        plugin.getQueueManager().notifyGameStart();
        players.clear();
        players.addAll(plugin.getQueueManager().getQueue());
        Collections.shuffle(players);
        for (int i = 0; i < players.size(); i++) {
            if (i % 2 == 0) {
                attackers.add(players.get(i));
                scoreAttackers.addEntry(players.get(i).getName());
                players.get(i).setBedSpawnLocation(currentMap.attackerspawn, true);
                players.get(i).teleport(currentMap.attackerspawn);
                players.get(i).sendTitle(ChatColor.RED + "You are an attacker!", ChatColor.RED + "Map: " + currentMap.name, 10, 40, 10);
                players.get(i).sendMessage(
                        ChatColor.YELLOW + "§l============================================\n" +
                                ChatColor.GOLD + "You are an" + ChatColor.RED + " §lAttacker!\n" +
                                ChatColor.GOLD + "Kill the " + ChatColor.BLUE + "Defenders' " + ChatColor.GOLD + "creeper before time runs out!\n" +
                                ChatColor.GOLD + "You can buy items from the shop using gold!\n" +
                                ChatColor.GOLD + "Obtain gold by slaying " + ChatColor.BLUE + "Defenders!\n" +
                                ChatColor.YELLOW + "§l============================================");
                setDefaultInventory(players.get(i), 1);
            } else {
                defenders.add(players.get(i));
                scoreDefenders.addEntry(players.get(i).getName());
                players.get(i).setBedSpawnLocation(currentMap.defenderspawn, true);
                players.get(i).teleport(currentMap.defenderspawn);
                players.get(i).sendTitle(ChatColor.BLUE + "You are a defender!", ChatColor.BLUE + "Map: " + currentMap.name, 10, 40, 10);
                players.get(i).sendMessage(
                        ChatColor.YELLOW + "§l============================================\n" +
                                ChatColor.GOLD + "You are a" + ChatColor.BLUE + " §lDefender!\n" +
                                ChatColor.GOLD + "Defend your creeper from the attackers until time ends!\n" +
                                ChatColor.GOLD + "You can buy items from the shop using gold!\n" +
                                ChatColor.GOLD + "Obtain gold by slaying " + ChatColor.RED + "Attackers!\n" +
                                ChatColor.YELLOW + "§l============================================");
                setDefaultInventory(players.get(i), 0);
            }
            for (PotionEffect effect : players.get(i).getActivePotionEffects()) {
                players.get(i).removePotionEffect(effect.getType());
            }
            players.get(i).setGameMode(GameMode.ADVENTURE);
            players.get(i).setFoodLevel(20);
            players.get(i).setHealth(20);
            players.get(i).setSaturation(0);
            killMap.put(players.get(i), 0);
        }
        initBossBar();
        initTimer();
        initCreeper();
    }

    private void setDefaultInventory(Player player, int team) { // 0 - Defender, 1 - Attacker
        player.getInventory().clear();
        player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
        player.getInventory().setItem(0, new ItemStack(Material.WOODEN_SWORD));
        player.getInventory().setItem(1, new ItemStack(Material.BOW));
        player.getInventory().setItem(2, new ItemStack(Material.FISHING_ROD));
        player.getInventory().setItem(3, new ItemStack(Material.COOKED_BEEF, 8));
        player.getInventory().setItem(4, new ItemStack(Material.ARROW, (team == 0 ? 3 : 5)));

        ItemStack goldIngot = new ItemStack(Material.GOLD_INGOT, 1); // TODO: This doesn't work, from looks of it not possible anymore
        goldIngot.setAmount(0);
        player.getInventory().addItem(goldIngot);
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta helmetItemMeta = (LeatherArmorMeta) helmet.getItemMeta();
        helmetItemMeta.setColor(team == 0 ? Color.BLUE : Color.RED);
        helmet.setItemMeta(helmetItemMeta);
        LeatherArmorMeta chestplateItemMeta = (LeatherArmorMeta) chestplate.getItemMeta();
        chestplateItemMeta.setColor(team == 0 ? Color.BLUE : Color.RED);
        chestplate.setItemMeta(chestplateItemMeta);
        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);

        for(ItemStack item : player.getInventory().getContents()) {
            if(item != null && item.getData() != null && item.getType() != Material.ARROW && item.getType() != Material.COOKED_BEEF) {
                ItemMeta meta = item.getItemMeta();
                meta.setUnbreakable(true);
                item.setItemMeta(meta);
            }
        }
    }

    private void initBossBar() {
        bossBar = Bukkit.createBossBar(ChatColor.RED + "Creeper Health", BarColor.RED, BarStyle.SOLID);
        new BukkitRunnable() {
            @Override
            public void run() {
                if(creeper.isDead()) {
                    bossBar.setProgress(0 / creeper.getMaxHealth());
                    bossBar.setVisible(false);
                    bossBar.removeAll();
                    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                        @Override
                        public void run() {
                            endGame(1);
                        }
                    }, 40L);
                    cancel();
                    return;
                }
                bossBar.setProgress(creeper.getHealth() / creeper.getMaxHealth());
                DecimalFormat df = new DecimalFormat("#.0");
                bossBar.setTitle(ChatColor.RED + "Creeper Health: " + df.format(creeper.getHealth()) + "/" + df.format(creeper.getMaxHealth()));
                bossBar.setVisible(true);
                for (Player p : players) {
                    bossBar.addPlayer(p);
                }
            }
        }.runTaskTimer(plugin,0, 3);
    }

    private void initTimer() {
        //Show the current time as the XP bar
        new BukkitRunnable() {
            @Override
            public void run() {
                checkPowerups();
                notifyTimeLeft();
                if(timeLeft == 0 || !isGameStarted()) {
                    endGame(0);
                    cancel();
                    return;
                }
                for(Player p : players) {
                    p.setLevel(timeLeft);
                    p.setExp(0);
                }
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void checkPowerups() {
        if(timeLeft <= totalTime && timeLeft > (totalTime / 2)){
            for(Player p : attackers) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
            }
        }
        if(timeLeft <= (totalTime / 2)){
            for(Player p : attackers) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
            }
            for(Player p : defenders) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
            }
        }
    }

    private void notifyTimeLeft() {
        if(timeLeft == 60){
            for(Player p : players) {
                p.sendTitle(ChatColor.RED + "1 Minute Remaining!", "", 10, 40, 10);
            }
        }
    }

    public void showGlow() {
        var protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA, PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if(isGameStarted()){
                    for (Player player : getPlayers()) {
                        Team theGlow = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
                        if (theGlow != null && theGlow.getEntries().contains(event.getPlayer().getName())) {
                            if (/*has a player with*/player.getEntityId() == event.getPacket().getIntegers().read(0)) {
                                WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(event.getPlayer());
                                if(player.getName().equals(event.getPlayer().getName())){
                                    return;
                                }
                                if (watcher.getWatchableObjects().stream()
                                        .map(WrappedWatchableObject::getValue)
                                        .filter(Byte.class::isInstance)
                                        .map(Byte.class::cast)
                                        .filter(Objects::nonNull)
                                        .anyMatch(b -> b == ((byte) 0x40))){
                                    return;
                                }
                                if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
                                    WrapperPlayServerEntityMetadata wrapper = new WrapperPlayServerEntityMetadata();
                                    // Collect if the entity is already glowing.
                                    byte data = watcher.getByte(0);
                                    data |= 1 << 6;
                                    wrapper.addToDataValueCollection(new WrappedDataValue(0, Registry.get(Byte.class), data));
                                    wrapper.setEntityID(event.getPlayer().getEntityId());
                                    wrapper.sendPacket(player);
                                }
                                else {
                                    WrapperPlayServerEntityMetadata newwrapper = new WrapperPlayServerEntityMetadata();
                                    newwrapper.addToDataValueCollection(new WrappedDataValue(0, Registry.get(Byte.class), (byte) 0));
                                    newwrapper.setEntityID(event.getPlayer().getEntityId());
                                    newwrapper.sendPacket(player);
                                }
                            }
                        }
                    }
                }

            }
        });
    }

    public void endGame(int winner) { // 0 - Defenders, 1 - Attackers
        gameEnded = false;
        gameStarted = false;
        for(Player p : players) {
            p.setLevel(0);
            p.setExp(0);
            p.getInventory().clear();
            p.teleport(lobbySpawn);
            p.setBedSpawnLocation(lobbySpawn, true);
            for(PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            p.setGameMode(GameMode.ADVENTURE);
        }
        for (String playerName : scoreAttackers.getEntries()) {
            scoreAttackers.removeEntry(playerName);
        }
        for (String playerName : scoreDefenders.getEntries()) {
            scoreDefenders.removeEntry(playerName);
        }
        players.clear();
        defenders.clear();
        attackers.clear();
        creeper.remove();
        bossBar.setVisible(false);
        bossBar.removeAll();
        plugin.getQueueManager().notifyGameEnd(winner);
        plugin.getQueueManager().resetQueue();
        plugin.getGoldManager().resetGame();
        plugin.getShopManager().resetShop();
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public void setGameEnded(boolean gameEnded) {
        this.gameEnded = gameEnded;
    }

    public HashMap<Player, Integer> getKillMap() {
        return killMap;
    }

    public Creeper getCreeper() {
        return creeper;
    }


    public List<Player> getDefenders() {
        return defenders;
    }

    public List<Player> getAttackers() {
        return attackers;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public GameMap getCurrentMap() {
        return currentMap;
    }
}

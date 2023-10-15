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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
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

    private HashMap<Player, Integer> playerKillMap = new HashMap<>();
    private HashMap<Player, Integer> killMap = new HashMap<>();
    private HashMap<Player, HashMap<Player, Double>> damageMap = new HashMap<>();

    private HashMap<Player, Integer> totalKills = new HashMap<>();
    private HashMap<Player, Double> totalCreeperDamage = new HashMap<>();

    private int attackerGoldStart = 0;
    private int defenderGoldStart = 0;

    private int creeperhealth;
    private ScoreboardManager manager;

    private Scoreboard board;
    private Team scoreAttackers;
    private Team scoreDefenders;

    private int resets;

    private Team seeGlow;


    private Creeper creeper;
    private BossBar bossBar;

    private int timeLeft;
    private int totalTime;

    private int lastCreeperHitTime;

    private List<GameMap> maps;
    private GameMap currentMap;

    private final static int CENTER_PX = 154;

    public GameManager(CreeperCataclysmPlugin plugin){
        this.plugin = plugin;
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

    public class Pair<K, V> {
        private K key;
        private V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "(" + key + ", " + value + ")";
        }
    }

    public enum DefaultFontInfo {

        A('A', 5),
        a('a', 5),
        B('B', 5),
        b('b', 5),
        C('C', 5),
        c('c', 5),
        D('D', 5),
        d('d', 5),
        E('E', 5),
        e('e', 5),
        F('F', 5),
        f('f', 4),
        G('G', 5),
        g('g', 5),
        H('H', 5),
        h('h', 5),
        I('I', 3),
        i('i', 1),
        J('J', 5),
        j('j', 5),
        K('K', 5),
        k('k', 4),
        L('L', 5),
        l('l', 1),
        M('M', 5),
        m('m', 5),
        N('N', 5),
        n('n', 5),
        O('O', 5),
        o('o', 5),
        P('P', 5),
        p('p', 5),
        Q('Q', 5),
        q('q', 5),
        R('R', 5),
        r('r', 5),
        S('S', 5),
        s('s', 5),
        T('T', 5),
        t('t', 4),
        U('U', 5),
        u('u', 5),
        V('V', 5),
        v('v', 5),
        W('W', 5),
        w('w', 5),
        X('X', 5),
        x('x', 5),
        Y('Y', 5),
        y('y', 5),
        Z('Z', 5),
        z('z', 5),
        NUM_1('1', 5),
        NUM_2('2', 5),
        NUM_3('3', 5),
        NUM_4('4', 5),
        NUM_5('5', 5),
        NUM_6('6', 5),
        NUM_7('7', 5),
        NUM_8('8', 5),
        NUM_9('9', 5),
        NUM_0('0', 5),
        EXCLAMATION_POINT('!', 1),
        AT_SYMBOL('@', 6),
        NUM_SIGN('#', 5),
        DOLLAR_SIGN('$', 5),
        PERCENT('%', 5),
        UP_ARROW('^', 5),
        AMPERSAND('&', 5),
        ASTERISK('*', 5),
        LEFT_PARENTHESIS('(', 4),
        RIGHT_PERENTHESIS(')', 4),
        MINUS('-', 5),
        UNDERSCORE('_', 5),
        PLUS_SIGN('+', 5),
        EQUALS_SIGN('=', 5),
        LEFT_CURL_BRACE('{', 4),
        RIGHT_CURL_BRACE('}', 4),
        LEFT_BRACKET('[', 3),
        RIGHT_BRACKET(']', 3),
        COLON(':', 1),
        SEMI_COLON(';', 1),
        DOUBLE_QUOTE('"', 3),
        SINGLE_QUOTE('\'', 1),
        LEFT_ARROW('<', 4),
        RIGHT_ARROW('>', 4),
        QUESTION_MARK('?', 5),
        SLASH('/', 5),
        BACK_SLASH('\\', 5),
        LINE('|', 1),
        TILDE('~', 5),
        TICK('`', 2),
        PERIOD('.', 1),
        COMMA(',', 1),
        SPACE(' ', 3),
        DEFAULT('a', 4);

        private char character;
        private int length;

        DefaultFontInfo(char character, int length) {
            this.character = character;
            this.length = length;
        }

        public char getCharacter() {
            return this.character;
        }

        public int getLength() {
            return this.length;
        }

        public int getBoldLength() {
            if (this == DefaultFontInfo.SPACE) return this.getLength();
            return this.length + 1;
        }

        public static DefaultFontInfo getDefaultFontInfo(char c) {
            for (DefaultFontInfo dFI : DefaultFontInfo.values()) {
                if (dFI.getCharacter() == c) return dFI;
            }
            return DefaultFontInfo.DEFAULT;
        }
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
            Bukkit.getLogger().info("Loaded Map: " + mapName);
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
        resets = 0;
        manager = Bukkit.getScoreboardManager();
        board = manager.getMainScoreboard();
        board.clearSlot(DisplaySlot.SIDEBAR);
        gameEnded = false;
        damageMap = new HashMap<>();
        killMap = new HashMap<>();
        totalKills = new HashMap<>();
        totalCreeperDamage = new HashMap<>();
        playerKillMap = new HashMap<>();
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
        updateSidebarScoreboard();
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
        creeperhealth = 500 + (100 * attackers.size());
        creeper.setMaxHealth(creeperhealth);
        creeper.setMaxFuseTicks(20);
        creeper.setExplosionRadius(30);
        creeper.setHealth(creeperhealth);
        creeper.setCustomName(ChatColor.GREEN + "CORE");
    }

    private void initPlayers() {
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
            totalKills.put(players.get(i), 0);
            totalCreeperDamage.put(players.get(i), 0.0);
        }
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                if(isGameStarted()) {
                    initBossBar();
                    initTimer();
                    initCreeper();
                }
            }
        }, 20L);
    }

    public static String secondsToTimeString(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }

    public void setDefaultInventory(Player player, int team) { // 0 - Defender, 1 - Attacker
        int i = 0;
        player.getInventory().clear();
        player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
        player.getInventory().setItem(i, new ItemStack(Material.WOODEN_SWORD));
        i++;
        player.getInventory().setItem(i, new ItemStack(Material.FISHING_ROD));
        i++;
        if(!currentMap.name.equals("Scorched Earth")){
            player.getInventory().setItem(i, new ItemStack(Material.BOW));
            i++;
            player.getInventory().setItem(i, new ItemStack(Material.ARROW, 3));
        }


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
                if(item.getType().equals(Material.LEATHER_CHESTPLATE) || item.getType().equals(Material.LEATHER_HELMET) || item.getType().equals(Material.IRON_LEGGINGS) || item.getType().equals(Material.IRON_BOOTS)) {
                    meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                }
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
                            if(isGameStarted()) {
                                endGame(1);
                            }
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
                checkTimeLeft();
                updateSidebarScoreboard();
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
        if(timeLeft <= totalTime){
            for(Player p : attackers) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
            }
            for(Player p : defenders) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
            }
        }
    }

    private void checkTimeLeft() {
        if((plugin.getGameManager().getTimeLeft() <= (.75 * plugin.getGameManager().getTotalTime())) && (plugin.getGameManager().getCreeperhealth() > (0.875 * plugin.getGameManager().getMaxCreeperHealth())) && (plugin.getGameManager().getAttackerGoldStart() < 1 || plugin.getGameManager().getAttackerGoldStart() == 1)){
            if(plugin.getGameManager().getAttackerGoldStart() == 1){

            }
            else{
                plugin.getGameManager().setAttackerGoldStart(1);
                for(Player player : plugin.getGameManager().getAttackers()){
                    player.sendMessage(ChatColor.RED + "Comeback buff activated +1 gold per kill!");
                    plugin.getGameManager().getKillMap().put(player, plugin.getGameManager().getKillMap().get(player) + 1);
                }
            }

        }
        else if((plugin.getGameManager().getTimeLeft() <= (.5 * plugin.getGameManager().getTotalTime())) && (plugin.getGameManager().getCreeperhealth() > (0.6 * plugin.getGameManager().getMaxCreeperHealth())) && (plugin.getGameManager().getCreeperhealth() <= (0.875 * plugin.getGameManager().getMaxCreeperHealth())) && (plugin.getGameManager().getAttackerGoldStart() < 1 || plugin.getGameManager().getAttackerGoldStart() == 1)){
            if(plugin.getGameManager().getAttackerGoldStart() == 1){

            }
            else{
                plugin.getGameManager().setAttackerGoldStart(1);
                for(Player player : plugin.getGameManager().getAttackers()){
                    player.sendMessage(ChatColor.RED + "Comeback buff activated +1 gold per kill!");
                    plugin.getGameManager().getKillMap().put(player, plugin.getGameManager().getKillMap().get(player) + 1);
                }
            }

        }
        else if((plugin.getGameManager().getTimeLeft() <= (.2 * plugin.getGameManager().getTotalTime())) && (plugin.getGameManager().getCreeperhealth() >= (0.35 * plugin.getGameManager().getMaxCreeperHealth())) && (plugin.getGameManager().getAttackerGoldStart() < 1 || plugin.getGameManager().getAttackerGoldStart() == 1)){
            if(plugin.getGameManager().getAttackerGoldStart() == 1){

            }
            else{
                plugin.getGameManager().setAttackerGoldStart(1);
                for(Player player : plugin.getGameManager().getAttackers()){
                    player.sendMessage(ChatColor.RED + "Comeback buff activated +1 gold per kill!");
                    plugin.getGameManager().getKillMap().put(player, plugin.getGameManager().getKillMap().get(player) + 1);
                }
            }

        }
        else if(plugin.getGameManager().getAttackerGoldStart() == 1){
            plugin.getGameManager().setAttackerGoldStart(0);
            for(Player player : plugin.getGameManager().getAttackers()){
                player.sendMessage(ChatColor.RED + "Comeback buff deactivated regular gold per kill!");
                plugin.getGameManager().getKillMap().put(player, plugin.getGameManager().getKillMap().get(player) - 1);
            }
        }

        if((plugin.getGameManager().getTimeLeft() >= (.5 * plugin.getGameManager().getTotalTime())) && (plugin.getGameManager().getCreeperhealth() < (0.5 * plugin.getGameManager().getMaxCreeperHealth())) && (plugin.getGameManager().getDefenderGoldStart() < 1 || plugin.getGameManager().getDefenderGoldStart() == 1)){
            if(plugin.getGameManager().getDefenderGoldStart() == 1){

            }
            else{
                plugin.getGameManager().setDefenderGoldStart(1);
                for(Player player : plugin.getGameManager().getDefenders()){
                    player.sendMessage(ChatColor.BLUE + "Comeback buff activated +1 gold per kill!");
                    plugin.getGameManager().getKillMap().put(player, plugin.getGameManager().getKillMap().get(player) + 1);
                }
            }
        }
        else if((plugin.getGameManager().getTimeLeft() >= (.3 * plugin.getGameManager().getTotalTime())) && (plugin.getGameManager().getCreeperhealth() < (0.2 * plugin.getGameManager().getMaxCreeperHealth())) && (plugin.getGameManager().getDefenderGoldStart() < 1 || plugin.getGameManager().getDefenderGoldStart() == 1)){
            if(plugin.getGameManager().getDefenderGoldStart() == 1){

            }
            else{
                plugin.getGameManager().setDefenderGoldStart(1);
                for(Player player : plugin.getGameManager().getDefenders()){
                    player.sendMessage(ChatColor.BLUE + "Comeback buff activated +1 gold per kill!");
                    plugin.getGameManager().getKillMap().put(player, plugin.getGameManager().getKillMap().get(player) + 1);
                }
            }
        }
        else if(plugin.getGameManager().getDefenderGoldStart() == 1){
            plugin.getGameManager().setDefenderGoldStart(0);
            for(Player player : plugin.getGameManager().getDefenders()){
                player.sendMessage(ChatColor.BLUE + "Comeback buff deactivated regular gold per kill!");
                plugin.getGameManager().getKillMap().put(player, plugin.getGameManager().getKillMap().get(player) - 1);
            }
        }

        if(timeLeft == 60){
            for(Player p : players) {
                p.sendTitle(ChatColor.RED + "1 Minute Remaining!", "", 10, 40, 10);
            }
        }
    }

    public void set(int row, String text, Objective objective) {
        for(String entry : board.getEntries()) {
            if(objective.getScore(entry).getScore() == row) {
                board.resetScores(entry);
                break;
            }
        }

        objective.getScore(text).setScore(row);
    }

    public void updateSidebarScoreboard() {
        if(isGameStarted()){
            Objective objective = board.getObjective("CreeperCat");
            if(resets == 0){
                board.getObjective("CreeperCat").unregister();
                objective = board.registerNewObjective("CreeperCat", "dummy", ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "Creeper Cataclysm");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            }


            int maxKills = 0;
            Player maxKillsPlayer = null;
            ChatColor maxKillsColor = null;
            if(!totalKills.isEmpty()){
                maxKills = Collections.max(totalKills.values());
                for(Player player : totalKills.keySet()){
                    if(totalKills.get(player) == maxKills){
                        maxKillsPlayer = player;
                        maxKillsColor = board.getEntryTeam(maxKillsPlayer.getName()).getColor();
                    }
                }
            }
            double highCreeperDMG = 0;
            Player highCreeperDMGPlayer = null;
            if(!totalCreeperDamage.isEmpty()){
                highCreeperDMG = Collections.max(totalCreeperDamage.values());
                for(Player player : totalCreeperDamage.keySet()){
                    if(totalCreeperDamage.get(player) == highCreeperDMG){
                        highCreeperDMGPlayer = player;
                    }
                }
            }

            if(resets == 0){
                Score score12 = objective.getScore("");
                Score score11 = objective.getScore(ChatColor.BOLD +  "Map: " + ChatColor.RESET + currentMap.name);
                Score score10 = objective.getScore(" ");
                Score score9 = objective.getScore(ChatColor.YELLOW + "" + ChatColor.BOLD + "Time Remaining: " + ChatColor.RESET + secondsToTimeString(timeLeft));
                Score score8 = objective.getScore("  ");
                Score score7 = objective.getScore(ChatColor.BLUE + "" + ChatColor.BOLD + "Defender Buff: " + ChatColor.RESET + (defenderGoldStart == 1 ? ChatColor.GREEN + "Active" : ChatColor.RED + "Inactive"));
                Score score6 = objective.getScore(ChatColor.RED + "" + ChatColor.BOLD + "Attacker Buff: " + ChatColor.RESET + (attackerGoldStart == 1 ? ChatColor.GREEN + "Active" : ChatColor.RED + "Inactive"));
                Score score5 = objective.getScore("   ");
                Score score4 = objective.getScore(ChatColor.BOLD +  "Most Kills: " + ChatColor.RESET + (maxKills == 0 ? "None" : maxKillsColor + maxKillsPlayer.getName() + ChatColor.WHITE + "" + ChatColor.BOLD + " [" + maxKills + "]"));
                Score score3 = objective.getScore(ChatColor.BOLD +  "Most DMG: " + ChatColor.RESET + (highCreeperDMG == 0 ? "None" : ChatColor.RED + highCreeperDMGPlayer.getName() + ChatColor.WHITE + "" + ChatColor.BOLD + " [" + ((int)highCreeperDMG) + "]"));
                Score score2 = objective.getScore("    ");
                Score score1 = objective.getScore("     ");

                score1.setScore(1);
                score2.setScore(2);
                score3.setScore(3);
                score4.setScore(4);
                score5.setScore(5);
                score6.setScore(6);
                score7.setScore(7);
                score8.setScore(8);
                score9.setScore(9);
                score10.setScore(10);
                score11.setScore(11);
                score12.setScore(12);

                resets = 1;
            }
            else{
                set(9, ChatColor.YELLOW + "" + ChatColor.BOLD + "Time Remaining: " + ChatColor.RESET + secondsToTimeString(timeLeft), objective);
                set(7, ChatColor.BLUE + "" + ChatColor.BOLD + "Defender Buff: " + ChatColor.RESET + (defenderGoldStart == 1 ? ChatColor.GREEN + "Active" : ChatColor.RED + "Inactive"), objective);
                set(6, ChatColor.RED + "" + ChatColor.BOLD + "Attacker Buff: " + ChatColor.RESET + (attackerGoldStart == 1 ? ChatColor.GREEN + "Active" : ChatColor.RED + "Inactive"), objective);
                set(4, ChatColor.BOLD +  "Most Kills: " + ChatColor.RESET + (maxKills == 0 ? "None" : maxKillsColor + maxKillsPlayer.getName() + ChatColor.WHITE + "" + ChatColor.BOLD + " [" + maxKills + "]"), objective);
                set(3, ChatColor.BOLD +  "Most DMG: " + ChatColor.RESET + (highCreeperDMG == 0 ? "None" : ChatColor.RED + highCreeperDMGPlayer.getName() + ChatColor.WHITE + "" + ChatColor.BOLD + " [" + ((int)highCreeperDMG) + "]"), objective);
            }
        }
    }

    public static void sendCenteredMessage(Player player, String message){
        if(message == null || message.equals("")) player.sendMessage("");
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for(char c : message.toCharArray()){
            if(c == '§'){
                previousCode = true;
                continue;
            }else if(previousCode == true){
                previousCode = false;
                if(c == 'l' || c == 'L'){
                    isBold = true;
                    continue;
                }else isBold = false;
            }else{
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate){
            sb.append(" ");
            compensated += spaceLength;
        }
        player.sendMessage(sb.toString() + message);
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
                                    byte data = watcher.getByte(0);
                                    data |= 1 << 6;
                                    wrapper.addToDataValueCollection(new WrappedDataValue(0, Registry.get(Byte.class), data));
                                    wrapper.setEntityID(event.getPlayer().getEntityId());
                                    wrapper.sendPacket(player);
                                }
//                                else {
//                                    WrapperPlayServerEntityMetadata newwrapper = new WrapperPlayServerEntityMetadata();
//                                    newwrapper.addToDataValueCollection(new WrappedDataValue(0, Registry.get(Byte.class), (byte) 0));
//                                    newwrapper.setEntityID(event.getPlayer().getEntityId());
//                                    newwrapper.sendPacket(player);
//                                }
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
        PriorityQueue<Pair<Integer, Player>> defendersKills =
                new PriorityQueue<>((a, b) -> b.getKey() - a.getKey());
        PriorityQueue<Pair<Integer, Player>> attackersKills =
                new PriorityQueue<>((a, b) -> b.getKey() - a.getKey());
        for(Player p: defenders){
            defendersKills.add(new Pair<>(totalKills.get(p), p));
        }
        for(Player p: attackers){
            attackersKills.add(new Pair<>(totalKills.get(p), p));
        }
        double highCreeperDMG = 0;
        Player highCreeperDMGPlayer = null;
        if(!totalCreeperDamage.isEmpty()){
            highCreeperDMG = Collections.max(totalCreeperDamage.values());
            for(Player player : totalCreeperDamage.keySet()){
                if(totalCreeperDamage.get(player) == highCreeperDMG){
                    highCreeperDMGPlayer = player;
                }
            }
        }
        Player topDefender = null;
        Player topAttacker = null;
        int topDefenderKills = 0;
        int topAttackerKills = 0;
        if(!defendersKills.isEmpty()){
            topDefender = defendersKills.peek().getValue();
            topDefenderKills = totalKills.get(topDefender);
        }

        if(!attackersKills.isEmpty()){
            topAttacker = attackersKills.peek().getValue();
            topAttackerKills = totalKills.get(topAttacker);
        }



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
            ItemStack knockbackstick = new ItemStack(Material.STICK);
            ItemMeta stickmeta = knockbackstick.getItemMeta();
            stickmeta.setDisplayName(ChatColor.RED + "Knockback Stick");
            stickmeta.addEnchant(Enchantment.KNOCKBACK, 10, true);
            knockbackstick.setItemMeta(stickmeta);
            p.getInventory().addItem(knockbackstick);
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, PotionEffect.INFINITE_DURATION, 3));
            sendCenteredMessage(p,ChatColor.GREEN + "§l============================================");
            sendCenteredMessage(p,ChatColor.WHITE + "§lCreeper Cataclysm");
            sendCenteredMessage(p,"");
            if(winner == 0){
                sendCenteredMessage(p,"§lWinner" + ChatColor.GRAY + " - " + ChatColor.BLUE + "§lDefenders");
            } else {
                sendCenteredMessage(p,"§lWinner" + ChatColor.GRAY + " - " + ChatColor.RED + "§lAttackers");
            }
            sendCenteredMessage(p,"");
            sendCenteredMessage(p,ChatColor.YELLOW + "§lDeadliest Defender" + ChatColor.GRAY + " - " + ChatColor.BLUE + (topDefenderKills == 0 ? "None" : topDefender.getName()) + ChatColor.WHITE + " [" + topDefenderKills + "]");
            sendCenteredMessage(p,ChatColor.GOLD + "§lDeadliest Attacker" + ChatColor.GRAY + " - " + ChatColor.RED + (topAttackerKills == 0 ? "None" : topAttacker.getName()) + ChatColor.WHITE + " [" + topAttackerKills + "]");
            if(p.equals(topDefender) || p.equals(topAttacker)){
                sendCenteredMessage(p,ChatColor.WHITE + "§lYou were the " +  ChatColor.RED + "§lDEADLIEST" + ChatColor.WHITE + "§l on your team!");
            }
            else{
                sendCenteredMessage(p, ChatColor.WHITE + "§lYour Kills" + ChatColor.GRAY + " - " + ChatColor.WHITE + totalKills.get(p));
            }
            sendCenteredMessage(p,"");
            sendCenteredMessage(p,ChatColor.DARK_GREEN + "§lMost Creeper Damage" + ChatColor.GRAY + " - " + ChatColor.GREEN + (highCreeperDMG == 0 ? "None" : highCreeperDMGPlayer.getName()) + ChatColor.WHITE + " [" + (int)highCreeperDMG + "]");
            if(attackers.contains(p)){
                if(p.equals(highCreeperDMGPlayer)){
                    sendCenteredMessage(p,ChatColor.WHITE + "§lYou did the " +  ChatColor.GREEN + "§lMOST CREEPER DAMAGE" + ChatColor.WHITE + "§l!");
                }
                else{
                    double playerCreeperDMG = totalCreeperDamage.get(p);
                    sendCenteredMessage(p, ChatColor.WHITE + "§lYour Creeper Damage" + ChatColor.GRAY + " - " + ChatColor.WHITE + (int)(playerCreeperDMG));
                }
            }
            sendCenteredMessage(p,ChatColor.GREEN + "§l============================================");
        }
        for (String playerName : scoreAttackers.getEntries()) {
            scoreAttackers.removeEntry(playerName);
        }
        for (String playerName : scoreDefenders.getEntries()) {
            scoreDefenders.removeEntry(playerName);
        }
        playerKillMap = new HashMap<>();
        players.clear();
        defenders.clear();
        attackers.clear();
        if(creeper == null){
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    if(isGameStarted()) {
                        creeper.remove();
                        bossBar.setVisible(false);
                        bossBar.removeAll();
                    }
                }
            }, 20L);
        }else{
            creeper.remove();
            bossBar.setVisible(false);
            bossBar.removeAll();
        }
        plugin.getQueueManager().resetQueue();
        plugin.getGoldManager().resetGame();
        plugin.getShopManager().resetShop();
        board.getObjective("deaths").unregister();
        board.registerNewObjective("deaths", "deathCount", ChatColor.RED + "Deaths");
        board.getObjective("deaths").setDisplaySlot(DisplaySlot.SIDEBAR);
        for (Player player : Bukkit.getOnlinePlayers()) {
            Score deathsScore = board.getObjective("deaths").getScore(player.getName());
            deathsScore.setScore(0);
        }
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

    public HashMap<Player, Integer> getPlayerKillMap() {
        return playerKillMap;
    }

    public HashMap<Player, HashMap<Player, Double>> getDamageMap() {
        return damageMap;
    }

    public HashMap<Player, Double> getTotalCreeperDamage() {
        return totalCreeperDamage;
    }

    public HashMap<Player, Integer> getTotalKills() {
        return totalKills;
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

    public int getAttackerGoldStart() {
        return attackerGoldStart;
    }

    public void setAttackerGoldStart(int attackerGoldStart) {
        this.attackerGoldStart = attackerGoldStart;
    }

    public int getDefenderGoldStart() {
        return defenderGoldStart;
    }

    public void setDefenderGoldStart(int defenderGoldStart) {
        this.defenderGoldStart = defenderGoldStart;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public int getMaxCreeperHealth() {
        return creeperhealth;
    }

    public double getCreeperhealth() {
        return creeper.getHealth();
    }

    public GameMap getCurrentMap() {
        return currentMap;
    }
}

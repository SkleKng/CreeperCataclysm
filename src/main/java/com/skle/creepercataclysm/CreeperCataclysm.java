package com.skle.creepercataclysm;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.skle.creepercataclysm.api.CreeperCataclysmPlugin;
import com.skle.creepercataclysm.commands.*;
import com.skle.creepercataclysm.commands.debug.*;
import com.skle.creepercataclysm.listeners.*;
import com.skle.creepercataclysm.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;




//TODO: add shop items, custom enchants

public final class CreeperCataclysm extends JavaPlugin implements CreeperCataclysmPlugin {

    private FileConfiguration config = getConfig();

    private final GameManager gameManager = new GameManager(this);
    private final QueueManager queueManager = new QueueManager(this);
    private final GoldManager goldManager = new GoldManager(this);
    private final ShopManager shopManager = new ShopManager(this);
    private final ZoneManager zoneManager = new ZoneManager(this);
    private StatueManager statueManager;

   private final ProtocolManager manager = ProtocolLibrary.getProtocolManager();

    private int MAX_PLAYERS = 2;
    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        // Register commands
        statueManager = new StatueManager(this);
        getCommand("play").setExecutor(new PlayCommand(this));
        getCommand("queue").setExecutor(new QueueCommand(this));
        getCommand("leave").setExecutor(new LeaveCommand(this));
        getCommand("setplayers").setExecutor(new SetPlayersCommand(this));
        getCommand("settime").setExecutor(new SetTimeCommand(this));
        getCommand("abort").setExecutor(new AbortCommand(this));
        getCommand("forcestart").setExecutor(new ForceStartCommand(  this));
        getCommand("addgold").setExecutor(new AddGoldCommand(  this));
        getCommand("zonelobby").setExecutor(new ZoneLobbyCommand(  this));
        getCommand("zonemap").setExecutor(new ZoneMapCommand(  this));
        getCommand("reloadconfig").setExecutor(new ReloadConfigCommand(  this));
        getCommand("cancelzone").setExecutor(new CancelZoneCommand(  this));
        getCommand("setSpecial").setExecutor(new SpecialBlockCommand(  this));
        getCommand("creepercommands").setExecutor(new CreeperCatCommands(this));
        getCommand("resetdeaths").setExecutor(new ResetDeathsCommand(this));
        getCommand("switchteam").setExecutor(new SwitchTeamsCommand(this));
        getCommand("tpsnow").setExecutor(new TeleportSnowCommand(this));
        getCommand("tplobby").setExecutor(new TeleportLobbyCommand(this));
        getCommand("grapplinghook").setExecutor(new GrapplingHookCommand(this));
        getCommand("hook").setExecutor(new GrapplingHookCommand(this));

        getCommand("statue").setExecutor(new StatueCommand(this));
        
        getCommand("captureprogress").setExecutor(new CaptureProgressCommand(this));

        // Register listeners
        Bukkit.getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new EntityDeathListener(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new EntityInteractListener(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new EntityExplodeListener(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerRespawnListener(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new ProjectileListener(this), this);

        gameManager.showGlow();
        gameManager.initConfig();
    }

    @Override
    public void onDisable() {
        ConfigurationSection specialBlocks = config.getConfigurationSection("specialBlocks");
        specialBlocks.getKeys(false).forEach(key -> {
            specialBlocks.set(key + ".enabled", true);
        });
        this.saveConfig();
        if(gameManager.isGameStarted()) {
            gameManager.endGame(0);
        }
    }

    @Override
    public GameManager getGameManager() {
        return gameManager;
    }

    @Override
    public QueueManager getQueueManager() {
        return queueManager;
    }

    public ProtocolManager getProtocolManager() {
        return manager;
    }

    @Override
    public GoldManager getGoldManager() { return goldManager;}

    @Override
    public ShopManager getShopManager() { return shopManager; }

    @Override
    public ZoneManager getZoneManager() { return zoneManager; }

    public StatueManager getStatueManager() { return statueManager; }

    @Override
    public int getMaxPlayers() {
        return MAX_PLAYERS;
    }

    @Override
    public void setMaxPlayers(int maxPlayers) {
        this.MAX_PLAYERS = maxPlayers;
    }

    @Override
    public FileConfiguration getPluginConfig() {
        return config;
    }

    public void reloadPluginConfig() {
        saveConfig();
        reloadConfig();
        config = getConfig();
        this.getGameManager().initConfig();
        Bukkit.getLogger().info("Configs reloaded.");
    }

    public void reloadConfigFromDisk() {
        reloadConfig();
        config = getConfig();
        this.getGameManager().initConfig();
        Bukkit.getLogger().info("Configs reloaded from disk.");
    }
}
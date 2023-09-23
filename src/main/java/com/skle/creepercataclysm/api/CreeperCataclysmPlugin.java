package com.skle.creepercataclysm.api;

import com.skle.creepercataclysm.managers.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public interface CreeperCataclysmPlugin extends Plugin {
    GameManager getGameManager();
    QueueManager getQueueManager();
    GoldManager getGoldManager();
    ShopManager getShopManager();
    ZoneManager getZoneManager();

    FileConfiguration getPluginConfig();
    void reloadPluginConfig();
    void reloadConfigFromDisk();

    int getMaxPlayers();
    void setMaxPlayers(int maxPlayers);

}

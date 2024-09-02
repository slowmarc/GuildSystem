package com.mineledge.guilds;

import org.bukkit.plugin.java.JavaPlugin;

public final class Guilds extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("@ Guilds has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("@ Guilds has been disabled!");
    }
}

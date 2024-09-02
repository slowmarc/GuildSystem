package com.mineledge.guilds;

import com.mineledge.guilds.repositories.GuildRepository;
import com.mineledge.guilds.repositories.LocalRepository;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Guilds extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("@ Guilds has been enabled!");

        saveResource("config.yml", false);
        saveResource("messages.yml", false);

        GuildRepository repository = new LocalRepository(new File(getDataFolder(), "local.json"));
        repository.load();
    }

    @Override
    public void onDisable() {
        getLogger().info("@ Guilds has been disabled!");
    }
}

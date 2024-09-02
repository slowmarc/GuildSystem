package com.mineledge.guilds;

import com.mineledge.guilds.commands.GuildCommand;
import com.mineledge.guilds.managers.GuildManager;
import com.mineledge.guilds.repositories.LocalRepository;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Guilds extends JavaPlugin {
    GuildManager manager;

    @Override
    public void onEnable() {
        getLogger().info("@ Guilds has been enabled!");

        saveResource("config.yml", false);
        saveResource("messages.yml", false);

        manager = new GuildManager(new LocalRepository(new File(getDataFolder(), "local.json")));
        manager.getRepository().load();

        getCommand("guild").setExecutor(new GuildCommand(manager));
    }

    @Override
    public void onDisable() {
        getLogger().info("@ Guilds has been disabled!");
        manager.getRepository().save(manager.getGuilds());
    }
}

package com.mineledge.guilds;

import com.mineledge.guilds.commands.GuildCommand;
import com.mineledge.guilds.configurations.GuildMessages;
import com.mineledge.guilds.managers.GuildManager;
import com.mineledge.guilds.repositories.LocalRepository;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class GuildSystem extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("@ Guilds has been enabled!");

        saveResource("config.yml", false);
        saveResource("messages.yml", false);

        GuildManager manager = new GuildManager(new LocalRepository(new File(getDataFolder(), "local.json")));
        manager.getRepository().load();

        GuildMessages messages = new GuildMessages(new File(getDataFolder(), "messages.yml"));

        LifecycleEventManager<Plugin> lifecycleEventManager = getLifecycleManager();
        lifecycleEventManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("guild", "some help description string", new GuildCommand(manager, messages));
        });
    }

    @Override
    public void onDisable() {
        getLogger().info("@ Guilds has been disabled!");
    }
}

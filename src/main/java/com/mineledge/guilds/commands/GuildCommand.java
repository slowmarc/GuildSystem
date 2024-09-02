package com.mineledge.guilds.commands;

import com.mineledge.guilds.managers.GuildManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuildCommand implements CommandExecutor {
    private GuildManager manager;
    private Map<String, GuildSubCommand> subCommands = new HashMap<>();

    public GuildCommand(GuildManager manager) {
        this.manager = manager;
        subCommands.put("create", new CreateSubCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        if (!(sender instanceof Player)) {
            return true;
        }

        if (!subCommands.containsKey(label.equalsIgnoreCase(label))) {
            return true;
        }

        if (subCommands.get(label).onSubCommand(((Player) sender).getUniqueId(), Arrays.copyOfRange(arguments, 1, arguments.length))) {
            manager.getRepository().save(manager.getGuilds());
        }

        return true;
    }

    interface GuildSubCommand {
        boolean onSubCommand(UUID sender, String[] arguments);
    }

    class CreateSubCommand implements GuildSubCommand {
        @Override
        public boolean onSubCommand(UUID sender, String[] arguments) {
            if (arguments.length != 2) {
                //Invalid syntax
                return false;
            }

            if (manager.isPlayerAffiliated(sender)) {
                //Player already in a guild
                return false;
            }

            if (manager.isGuildTagTaken(arguments[0])) {
                //Guild tag already exists
                return false;
            } else if (manager.isGuildNameTaken(arguments[1])) {
                //Guild name already exists
                return false;
            }

            manager.addGuild(arguments[0], arguments[1], sender);
            return true;
        }
    }
}

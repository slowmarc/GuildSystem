package com.mineledge.guilds.commands;

import com.mineledge.guilds.managers.GuildManager;
import com.mineledge.guilds.models.Guild;
import org.bukkit.Bukkit;
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
        subCommands.put("invite", new InviteSubCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        if (!(sender instanceof Player)) {
            //Sender not a player
            return true;
        }

        GuildSubCommand subCommand = subCommands.get(label);
        if (subCommand == null) {
            //Invalid subcommand
            return true;
        } else {
            if (subCommand.onSubCommand(((Player) sender).getUniqueId(), Arrays.copyOfRange(arguments, 1, arguments.length))) {
                manager.getRepository().save(manager.getGuilds());
            }
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
                //Invalid arguments
                return false;
            }

            if (manager.isPlayerAffiliated(sender)) {
                //Player already in a guild
                return false;
            }

            String tag = arguments[0];
            String name = arguments[1];

            if (tag.matches(".*[^a-zA-Z0-9].*")) {
                //Tag contains non-alphanumeric characters
                return false;
            } else if (name.matches(".*[^a-zA-Z0-9].*")) {
                //Name contains non-alphanumeric characters
                return false;
            }

            if (manager.isGuildTagTaken(tag)) {
                //Guild tag already exists
                return false;
            } else if (manager.isGuildNameTaken(name)) {
                //Guild name already exists
                return false;
            }

            manager.addGuild(arguments[0], arguments[1], sender);
            return true;
        }
    }

    class InviteSubCommand implements GuildSubCommand {

        @Override
        public boolean onSubCommand(UUID sender, String[] arguments) {
            if (arguments.length != 1) {
                //Invalid arguments
                return false;
            }

            if (!manager.isPlayerAffiliated(sender)) {
                //Player is not in a guild
                return false;
            }

            if (!manager.isPlayerMaster(sender) || !manager.isPlayerJourneyman(sender)) {
                //Player is an apprentice.
                return false;
            }

            Guild guild = manager.getGuildByPlayer(sender);
            UUID target = Bukkit.getOfflinePlayer(arguments[0]).getUniqueId();

            if (target == null) {
                //Target doesn't exist
                return false;
            } else if (sender.equals(target)) {
                //Sender is target
                return false;
            }

            if (guild.isAffiliated(target)) {
                //Target already in guild
                return false;
            } else if (guild.isRecipient(target)) {
                //Target already invited
                return false;
            }

            guild.addRecipient(target);
            return true;
        }
    }

    class JoinSubCommand implements GuildSubCommand {

        @Override
        public boolean onSubCommand(UUID sender, String[] arguments) {
            if (arguments.length != 1) {
                //Invalid arguments
                return false;
            }

            if (manager.isPlayerAffiliated(sender)) {
                //Sender already in guild
                return false;
            }

            Guild guild = manager.getGuildByName(arguments[0]);
            if (guild == null) {
                //Guild doesn't exist.
                return false;
            } else if (!guild.isRecipient(sender)) {
                //Sender is not invited
                return false;
            }

            guild.addApprentice(sender);
            return true;
        }
    }
}

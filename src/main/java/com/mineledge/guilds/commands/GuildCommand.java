package com.mineledge.guilds.commands;

import com.mineledge.guilds.managers.GuildManager;
import com.mineledge.guilds.models.Guild;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuildCommand implements BasicCommand {
    private GuildManager manager;
    private Map<String, GuildSubCommand> subCommands = new HashMap<>();

    public GuildCommand(GuildManager manager) {
        this.manager = manager;
        subCommands.put("create", new CreateSubCommand());
        subCommands.put("disband", new DisbandSubCommand());
        subCommands.put("invite", new InviteSubCommand());
        subCommands.put("uninvite", new UninviteSubCommand());
        subCommands.put("join", new JoinSubCommand());
        subCommands.put("leave", new LeaveSubCommand());
        subCommands.put("kick", new KickSubCommand());
        subCommands.put("promote", new PromoteSubCommand());
        //subCommands.put("demote", new DemoteSubCommand());
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] arguments) {
        CommandSender sender = stack.getSender();
        if (!(sender instanceof Player)) {
            //Sender not a player
            return;
        }

        if (arguments.length == 0) {
            //Invalid arguments
            return;
        }

        GuildSubCommand subCommand = subCommands.get(arguments[0]);
        if (subCommand == null) {
            //Invalid subcommand
        } else {
            if (subCommand.execute(((Player) sender).getUniqueId(), Arrays.copyOfRange(arguments, 1, arguments.length))) {
                manager.getRepository().save(manager.getGuilds());
            }
        }
    }

    interface GuildSubCommand {
        boolean execute(UUID sender, String[] arguments);
    }

    class CreateSubCommand implements GuildSubCommand {

        @Override
        public boolean execute(UUID sender, String[] arguments) {
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

    class DisbandSubCommand implements GuildSubCommand {

        @Override
        public boolean execute(UUID sender, String[] arguments) {
            if (arguments.length != 0) {
                //Invalid arguments
                return false;
            }

            if (!manager.isPlayerAffiliated(sender)) {
                //Sender not in a guild
                return false;
            } else if (!manager.isPlayerMaster(sender)) {
                //Sender not the master
                return false;
            }

            manager.removeGuild(manager.getGuildByPlayer(sender));
            return true;
        }
    }

    class InviteSubCommand implements GuildSubCommand {

        @Override
        public boolean execute(UUID sender, String[] arguments) {
            if (arguments.length != 1) {
                //Invalid arguments
                return false;
            }

            if (!manager.isPlayerAffiliated(sender)) {
                //Player is not in a guild
                return false;
            }

            if (!manager.isPlayerMaster(sender) && !manager.isPlayerJourneyman(sender)) {
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
                //Target already in the guild
                return false;
            } else if (guild.isRecipient(target)) {
                //Target already invited
                return false;
            }

            guild.addRecipient(target);
            return true;
        }
    }

    class UninviteSubCommand implements GuildSubCommand {

        @Override
        public boolean execute(UUID sender, String[] arguments) {
            if (arguments.length != 1) {
                //Invalid arguments
                return false;
            }

            if (!manager.isPlayerAffiliated(sender)) {
                //Player is not in a guild
                return false;
            }

            if (!manager.isPlayerMaster(sender) && !manager.isPlayerJourneyman(sender)) {
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

            if (!guild.isRecipient(target)) {
                //Target not invited
                return false;
            }

            guild.removeRecipient(target);
            return true;
        }
    }

    class JoinSubCommand implements GuildSubCommand {

        @Override
        public boolean execute(UUID sender, String[] arguments) {
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

    class LeaveSubCommand implements GuildSubCommand {

        @Override
        public boolean execute(UUID sender, String[] arguments) {
            if (arguments.length != 0) {
                //Invalid arguments
                return false;
            }

            if (!manager.isPlayerAffiliated(sender)) {
                //Sender not in a guild
                return false;
            }

            Guild guild = manager.getGuildByPlayer(sender);
            if (guild.isMaster(sender)) {
                //Sender is master
                return false;
            }

            if (guild.isJourneyman(sender)) {
                guild.removeRecipient(sender);
            }
            guild.removeApprentice(sender);
            return true;
        }
    }

    class KickSubCommand implements GuildSubCommand {

        @Override
        public boolean execute(UUID sender, String[] arguments) {
            if (arguments.length != 1) {
                //Invalid arguments
                return false;
            }

            if (!manager.isPlayerAffiliated(sender)) {
                //Sender not in a guild
                return false;
            } else if (!manager.isPlayerMaster(sender) && !manager.isPlayerJourneyman(sender)) {
                //Sender is apprentice
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
            } else if (!guild.isAffiliated(target)) {
                //Target not in the guild
                return false;
            }

            if (guild.isMaster(sender)) {
                if (guild.isJourneyman(sender)) {
                    guild.removeJourneyman(sender);
                }
                guild.removeApprentice(target);
            } else {
                if (guild.isJourneyman(target)) {
                    //Sender is the same rank as target
                    return false;
                } else {
                    guild.removeApprentice(target);
                }
            }

            return true;
        }
    }

    class PromoteSubCommand implements GuildSubCommand {

        @Override
        public boolean execute(UUID sender, String[] arguments) {
            if (arguments.length != 1) {
                //Invalid arguments
                return false;
            }

            if (!manager.isPlayerAffiliated(sender)) {
                //Sender not in a guild
                return false;
            }

            if (manager.isPlayerApprentice(sender)) {
                //Sender is an apprentice
                return false;
            }

            Guild guild = manager.getGuildByPlayer(sender);
            UUID target = Bukkit.getOfflinePlayer(arguments[0]).getUniqueId();
            if (target == null) {
                //Sender doesn't exist
                return false;
            } else if (sender.equals(target)) {
                //Sender is target
                return false;
            } else if (!guild.isAffiliated(target)) {
                //Target not a in the guild
                return false;
            }

            if (manager.isPlayerApprentice(target)) {
                guild.addJourneyman(target);
            } else {
                guild.setMaster(target);
            }

            return true;
        }
    }
}
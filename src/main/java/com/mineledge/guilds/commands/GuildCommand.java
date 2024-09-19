package com.mineledge.guilds.commands;

import com.mineledge.guilds.configurations.GuildMessages;
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

import static org.bukkit.Bukkit.getOfflinePlayer;
import static org.bukkit.Bukkit.getPlayer;

public class GuildCommand implements BasicCommand {
    private GuildManager manager;
    private GuildMessages messages;
    private Map<String, GuildSubCommand> subCommands = new HashMap<>();

    public GuildCommand(GuildManager manager, GuildMessages messages) {
        this.manager = manager;
        this.messages = messages;

        subCommands.put("help", new HelpSubCommand());
        subCommands.put("create", new CreateSubCommand());
        subCommands.put("disband", new DisbandSubCommand());
        subCommands.put("invite", new InviteSubCommand());
        subCommands.put("uninvite", new UninviteSubCommand());
        subCommands.put("join", new JoinSubCommand());
        subCommands.put("leave", new LeaveSubCommand());
        subCommands.put("kick", new KickSubCommand());
        subCommands.put("promote", new PromoteSubCommand());
        subCommands.put("demote", new DemoteSubCommand());
        subCommands.put("retag", new RetagSubCommand());
        //subCommands.put("rename", new RenameSubCommand());
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] arguments) {
        CommandSender sender = stack.getSender();
        try {
            if (!(sender instanceof Player)) {
                throw new Exception(messages.getGuildCommandErrorMessage("sender-not-player"));
            }

            if (arguments.length == 0) {
                throw new Exception(messages.getGuildCommandErrorMessage("invalid-syntax"));
            }

            GuildSubCommand subCommand = subCommands.get(arguments[0]);
            if (subCommand == null) {
                throw new Exception(messages.getGuildCommandErrorMessage("invalid-syntax"));
            } else {
                if (subCommand.execute(((Player) sender).getUniqueId(), Arrays.copyOfRange(arguments, 1, arguments.length))) {
                    manager.getRepository().save(manager.getGuilds());
                }
            }
        } catch (Exception exception) {
            sender.sendMessage(exception.getMessage());
        }
    }

    interface GuildSubCommand {
        boolean execute(UUID sender, String[] arguments);
    }

    class HelpSubCommand implements GuildSubCommand {
        @Override
        public boolean execute(UUID sender, String[] arguments) {
            try {
                if (arguments.length != 0) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("help", "illegal-syntax"));
                }

                getPlayer(sender).sendMessage(messages.getGuildSubCommandSuccessMessage("help", "sender-message"));
                return true;
            } catch (Exception exception) {
                getPlayer(sender).sendMessage(exception.getMessage());
                return false;
            }
        }
    }

    class CreateSubCommand implements GuildSubCommand {
        @Override
        public boolean execute(UUID sender, String[] arguments) {
            try {
                if (arguments.length != 2) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("create", "illegal-syntax"));
                }

                if (manager.isPlayerAffiliated(sender)) {
                    Guild guild = manager.getGuildByPlayer(sender);
                    throw new Exception(messages.getGuildSubCommandGuildErrorMessage("create", "sender-is-affiliated", guild.getTag(), guild.getName()));
                }

                String tag = arguments[0];
                String name = arguments[1];

                if (tag.matches(".*[^a-zA-Z0-9].*")) {
                    throw new Exception(messages.getGuildSubCommandTagErrorMessage("create", "illegal-tag", tag));
                } else if (name.matches(".*[^a-zA-Z0-9].*")) {
                    throw new Exception(messages.getGuildSubCommandNameErrorMessage("create", "illegal-name", name));
                }

                if (manager.isGuildTagTaken(tag)) {
                    throw new Exception(messages.getGuildSubCommandTagErrorMessage("create", "tag-is-taken", tag));
                } else if (manager.isGuildNameTaken(name)) {
                    throw new Exception(messages.getGuildSubCommandNameErrorMessage("create", "name-is-taken", name));
                }


                manager.addGuild(tag, name, sender);
                getPlayer(sender).sendMessage(messages.getGuildSubCommandGuildSuccessMessage("create", "sender-message", tag, name));
                return true;
            } catch (Exception exception) {
                getPlayer(sender).sendMessage(exception.getMessage());
                return false;
            }
        }
    }

    class DisbandSubCommand implements GuildSubCommand {
        @Override
        public boolean execute(UUID sender, String[] arguments) {
            try {
                if (arguments.length != 0) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("disband", "illegal-syntax"));
                }

                if (!manager.isPlayerAffiliated(sender)) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("disband", "sender-not-affiliated"));
                } else if (!manager.isPlayerMaster(sender)) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("disband", "sender-not-master"));
                }

                Guild guild = manager.getGuildByPlayer(sender);
                String tag = guild.getTag();
                String name = guild.getName();

                getPlayer(sender).sendMessage(messages.getGuildSubCommandGuildSuccessMessage("disband", "sender-message", tag, name));
                for (UUID journeyman : guild.getJourneymen()) {
                    Player player = getPlayer(journeyman);
                    if (player != null) {
                        player.sendMessage(messages.getGuildSubCommandGuildSenderSuccessMessage("disband", "affiliated-message", tag, name, getOfflinePlayer(sender).getName()));
                    }
                }
                for (UUID apprentice : guild.getApprentices()) {
                    Player player = getPlayer(apprentice);
                    if (player != null) {
                        player.sendMessage(messages.getGuildSubCommandGuildSenderSuccessMessage("disband", "affiliated-message", tag, name, getOfflinePlayer(sender).getName()));
                    }
                }

                manager.removeGuild(guild);
                return true;
            } catch (Exception exception) {
                getPlayer(sender).sendMessage(exception.getMessage());
                return false;
            }
        }
    }

    class InviteSubCommand implements GuildSubCommand {
        @Override
        public boolean execute(UUID sender, String[] arguments) {
            try {
                if (arguments.length != 1) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("invite", "illegal-syntax"));
                }

                if (!manager.isPlayerAffiliated(sender)) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("invite", "sender-not-affiliated"));
                }

                Guild guild = manager.getGuildByPlayer(sender);
                String tag = guild.getTag();
                String name = guild.getName();
                if (!manager.isPlayerMaster(sender) && !manager.isPlayerJourneyman(sender)) {
                    throw new Exception(messages.getGuildSubCommandGuildErrorMessage("invite", "sender-apprentice", tag, name));
                }

                UUID target = Bukkit.getOfflinePlayer(arguments[0]).getUniqueId();

                if (target == null) {
                    throw new Exception(messages.getGuildSubCommandTargetErrorMessage("invite", "target-doesn't-exist", arguments[0]));
                } else if (sender.equals(target)) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("invite", "sender-is-target"));
                }

                if (guild.isAffiliated(target)) {
                    throw new Exception(messages.getGuildSubCommandGuildTargetErrorMessage("invite", "target-is-affiliated", tag, name, Bukkit.getOfflinePlayer(target).getName()));
                } else if (guild.isRecipient(target)) {
                    throw new Exception(messages.getGuildSubCommandGuildTargetErrorMessage("invite", "target-is-recipient", tag, name, Bukkit.getOfflinePlayer(target).getName()));
                }

                getPlayer(sender).sendMessage(messages.getGuildSubCommandGuildTargetSuccessMessage("invite", "sender-message", tag, name, getOfflinePlayer(target).getName()));
                if (getPlayer(target) != null) {
                    getPlayer(target).sendMessage(messages.getGuildSubCommandGuildSenderSuccessMessage("invite", "target-message", tag, name, getOfflinePlayer(sender).getName()));
                }
                for (UUID journeyman : guild.getJourneymen()) {
                    Player player = getPlayer(journeyman);
                    if (player != null) {
                        player.sendMessage(messages.getGuildSubCommandAllSuccessMessage("invite", "affiliated-message", tag, name, getOfflinePlayer(sender).getName(), getOfflinePlayer(target).getName()));
                    }
                }
                for (UUID apprentice : guild.getApprentices()) {
                    Player player = getPlayer(apprentice);
                    if (player != null) {
                        player.sendMessage(messages.getGuildSubCommandAllSuccessMessage("invite", "affiliated-message", tag, name, getOfflinePlayer(sender).getName(), getOfflinePlayer(target).getName()));
                    }
                }

                guild.addRecipient(target);
                return true;
            } catch (Exception exception) {
                getPlayer(sender).sendMessage(exception.getMessage());
                return false;
            }

        }
    }

    class UninviteSubCommand implements GuildSubCommand {
        @Override
        public boolean execute(UUID sender, String[] arguments) {
            try {
                if (arguments.length != 1) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("uninvite", "illegal-syntax"));
                }

                if (!manager.isPlayerAffiliated(sender)) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("uninvite", "sender-not-affiliated"));
                }

                Guild guild = manager.getGuildByPlayer(sender);
                String tag = guild.getTag();
                String name = guild.getName();

                if (manager.isPlayerApprentice(sender)) {
                    throw new Exception(messages.getGuildSubCommandGuildErrorMessage("uninvite", "sender-apprentice", tag, name));
                }

                UUID target = Bukkit.getOfflinePlayer(arguments[0]).getUniqueId();

                if (target == null) {
                    throw new Exception(messages.getGuildSubCommandTargetErrorMessage("uninvite", "target-doesn't-exist", arguments[0]));
                } else if (sender.equals(target)) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("uninvite", "sender-is-target"));
                }

                if (guild.isAffiliated(target)) {
                    throw new Exception(messages.getGuildSubCommandGuildTargetErrorMessage("uninvite", "target-is-affiliated", tag, name, Bukkit.getOfflinePlayer(target).getName()));
                } else if (!guild.isRecipient(target)) {
                    throw new Exception(messages.getGuildSubCommandGuildTargetErrorMessage("uninvite", "target-not-recipient", tag, name, Bukkit.getOfflinePlayer(target).getName()));
                }

                getPlayer(sender).sendMessage(messages.getGuildSubCommandGuildTargetSuccessMessage("uninvite", "sender-message", tag, name, getOfflinePlayer(target).getName()));
                if (getPlayer(target) != null) {
                    getPlayer(target).sendMessage(messages.getGuildSubCommandGuildSenderSuccessMessage("uninvite", "target-message", tag, name, getOfflinePlayer(sender).getName()));
                }
                for (UUID journeyman : guild.getJourneymen()) {
                    Player player = getPlayer(journeyman);
                    if (player != null) {
                        player.sendMessage(messages.getGuildSubCommandAllSuccessMessage("uninvite", "affiliated-message", tag, name, getOfflinePlayer(sender).getName(), getOfflinePlayer(target).getName()));
                    }
                }
                for (UUID apprentice : guild.getApprentices()) {
                    Player player = getPlayer(apprentice);
                    if (player != null) {
                        player.sendMessage(messages.getGuildSubCommandAllSuccessMessage("uninvite", "affiliated-message", tag, name, getOfflinePlayer(sender).getName(), getOfflinePlayer(target).getName()));
                    }
                }

                guild.removeRecipient(target);
                return true;
            } catch (Exception exception) {
                getPlayer(sender).sendMessage(exception.getMessage());
                return false;
            }
        }
    }

    class JoinSubCommand implements GuildSubCommand {
        @Override
        public boolean execute(UUID sender, String[] arguments) {
            try {
                if (arguments.length != 1) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("join", "illegal-syntax"));
                }

                if (manager.isPlayerAffiliated(sender)) {
                    Guild guild = manager.getGuildByPlayer(sender);
                    String tag = guild.getTag();
                    String name = guild.getName();
                    throw new Exception(messages.getGuildSubCommandGuildErrorMessage("join", "sender-is-affiliated", tag, name));
                }

                Guild guild = manager.getGuildByName(arguments[0]);
                if (guild == null) {
                    throw new Exception(messages.getGuildSubCommandNameErrorMessage("join", "guild-doesn't-exist", arguments[0]));
                } else if (!guild.isRecipient(sender)) {
                    throw new Exception(messages.getGuildSubCommandGuildErrorMessage("join", "sender-not-recipient", guild.getTag(), guild.getName()));
                }

                getPlayer(sender).sendMessage(messages.getGuildSubCommandGuildSuccessMessage("join", "sender-message", guild.getTag(), guild.getName()));
                if (getPlayer(guild.getMaster()) != null) {
                    getPlayer(guild.getMaster()).sendMessage(messages.getGuildSubCommandGuildSenderSuccessMessage("join", "affiliated-message", guild.getTag(), guild.getName(), Bukkit.getOfflinePlayer(sender).getName()));
                }
                for (UUID journeyman : guild.getJourneymen()) {
                    Player player = getPlayer(journeyman);
                    if (player != null) {
                        player.sendMessage(messages.getGuildSubCommandGuildSenderSuccessMessage("join", "affiliated-message", guild.getTag(), guild.getName(), Bukkit.getOfflinePlayer(sender).getName()));
                    }
                }
                for (UUID apprentice : guild.getApprentices()) {
                    Player player = getPlayer(apprentice);
                    if (player != null) {
                        player.sendMessage(messages.getGuildSubCommandGuildSenderSuccessMessage("join", "affiliated-message", guild.getTag(), guild.getName(), Bukkit.getOfflinePlayer(sender).getName()));
                    }
                }

                guild.addApprentice(sender);
                return true;
            } catch (Exception exception) {
                getPlayer(sender).sendMessage(exception.getMessage());
                return false;
            }
        }
    }

    class LeaveSubCommand implements GuildSubCommand {
        @Override
        public boolean execute(UUID sender, String[] arguments) {
            try {
                if (arguments.length != 0) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("leave", "illegal-syntax"));
                }

                if (!manager.isPlayerAffiliated(sender)) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("leave", "sender-not-affiliated"));
                }

                Guild guild = manager.getGuildByPlayer(sender);
                if (guild.isMaster(sender)) {
                    throw new Exception(messages.getGuildSubCommandGuildErrorMessage("leave", "sender-is-master", guild.getTag(), guild.getName()));
                }

                getPlayer(sender).sendMessage(messages.getGuildSubCommandGuildSuccessMessage("leave", "sender-message", guild.getTag(), guild.getName()));

                if (guild.isJourneyman(sender)) {
                    guild.removeRecipient(sender);
                }
                guild.removeApprentice(sender);

                if (getPlayer(guild.getMaster()) != null) {
                    getPlayer(guild.getMaster()).sendMessage(messages.getGuildSubCommandGuildSenderSuccessMessage("leave", "affiliated-message", guild.getTag(), guild.getName(), Bukkit.getOfflinePlayer(sender).getName()));
                }
                for (UUID journeyman : guild.getJourneymen()) {
                    Player player = getPlayer(journeyman);
                    if (player != null) {
                        player.sendMessage(messages.getGuildSubCommandGuildSenderSuccessMessage("leave", "affiliated-message", guild.getTag(), guild.getName(), Bukkit.getOfflinePlayer(sender).getName()));
                    }
                }
                for (UUID apprentice : guild.getApprentices()) {
                    Player player = getPlayer(apprentice);
                    if (player != null) {
                        player.sendMessage(messages.getGuildSubCommandGuildSenderSuccessMessage("leave", "affiliated-message", guild.getTag(), guild.getName(), Bukkit.getOfflinePlayer(sender).getName()));
                    }
                }

                return true;
            } catch (Exception exception) {
                getPlayer(sender).sendMessage(exception.getMessage());
                return false;
            }
        }
    }

    class KickSubCommand implements GuildSubCommand {
        @Override
        public boolean execute(UUID sender, String[] arguments) {
            try {
                if (arguments.length != 1) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("kick", "illegal-syntax"));
                }

                if (!manager.isPlayerAffiliated(sender)) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("kick", "sender-not-affiliated"));
                }

                Guild guild = manager.getGuildByPlayer(sender);
                String tag = guild.getTag();
                String name = guild.getName();
                if (manager.isPlayerApprentice(sender)) {
                    throw new Exception(messages.getGuildSubCommandGuildErrorMessage("kick", "sender-is-apprentice", tag, name));
                }

                UUID target = Bukkit.getOfflinePlayer(arguments[0]).getUniqueId();
                if (target == null) {
                    throw new Exception(messages.getGuildSubCommandTargetErrorMessage("kick", "target-doesn't-exist", arguments[0]));
                } else if (sender.equals(target)) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("kick", "sender-is-target"));
                } else if (!guild.isAffiliated(target)) {
                    throw new Exception(messages.getGuildSubCommandGuildTargetErrorMessage("kick", "target-not-affiliated", tag, name, Bukkit.getOfflinePlayer(target).getName()));
                }

                if (guild.isMaster(sender)) {
                    if (guild.isJourneyman(sender)) {
                        guild.removeJourneyman(sender);
                    }
                    guild.removeApprentice(target);
                } else {
                    if (guild.isMaster(target) || guild.isJourneyman(target)) {
                        throw new Exception(messages.getGuildSubCommandGuildTargetErrorMessage("kick", "target-not-inferior", tag, name, Bukkit.getOfflinePlayer(target).getName()));
                    } else {
                        guild.removeApprentice(target);
                    }
                }

                getPlayer(sender).sendMessage(messages.getGuildSubCommandGuildTargetSuccessMessage("kick", "sender-message", tag, name, Bukkit.getOfflinePlayer(target).getName()));
                if (getPlayer(target) != null) {
                    getPlayer(target).sendMessage(messages.getGuildSubCommandGuildSenderSuccessMessage("kick", "target-message", tag, name, Bukkit.getOfflinePlayer(sender).getName()));
                }

                if (getPlayer(guild.getMaster()) != null && sender != guild.getMaster()) {
                    getPlayer(guild.getMaster()).sendMessage(messages.getGuildSubCommandAllSuccessMessage("kick", "affiliated-message", tag, name, Bukkit.getOfflinePlayer(sender).getName(), Bukkit.getOfflinePlayer(target).getName()));
                }
                for (UUID journeyman : guild.getJourneymen()) {
                    Player player = getPlayer(journeyman);
                    if (player != null && !player.getUniqueId().equals(sender)) {
                        player.sendMessage(messages.getGuildSubCommandAllSuccessMessage("kick", "affiliated-message", guild.getTag(), guild.getName(), Bukkit.getOfflinePlayer(sender).getName(), Bukkit.getOfflinePlayer(target).getName()));
                    }
                }
                for (UUID apprentice : guild.getApprentices()) {
                    Player player = getPlayer(apprentice);
                    if (player != null) {
                        player.sendMessage(messages.getGuildSubCommandAllSuccessMessage("kick", "affiliated-message", guild.getTag(), guild.getName(), Bukkit.getOfflinePlayer(sender).getName(), Bukkit.getOfflinePlayer(target).getName()));
                    }
                }

                return true;
            } catch (Exception exception) {
                getPlayer(sender).sendMessage(exception.getMessage());
                return false;
            }
        }
    }

    class PromoteSubCommand implements GuildSubCommand {
        @Override
        public boolean execute(UUID sender, String[] arguments) {
            try {
                if (arguments.length != 1) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("promote", "illegal-syntax"));
                }

                if (!manager.isPlayerAffiliated(sender)) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("promote", "sender-not-affiliated"));
                }

                Guild guild = manager.getGuildByPlayer(sender);
                String tag = guild.getTag();
                String name = guild.getName();
                if (!manager.isPlayerMaster(sender)) {
                    throw new Exception(messages.getGuildSubCommandGuildErrorMessage("promote", "sender-not-master", guild.getTag(), guild.getName()));
                }

                UUID target = Bukkit.getOfflinePlayer(arguments[0]).getUniqueId();
                if (target == null) {
                    throw new Exception(messages.getGuildSubCommandTargetErrorMessage("promote", "target-doesn't-exist", arguments[0]));
                } else if (sender.equals(target)) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("promote", "sender-is-target"));
                } else if (!guild.isAffiliated(target)) {
                    throw new Exception(messages.getGuildSubCommandGuildTargetErrorMessage("promote", "target-not-affiliated", guild.getTag(), guild.getName(), Bukkit.getOfflinePlayer(target).getName()));
                }

                if (manager.isPlayerApprentice(target)) {
                    guild.addJourneyman(target);
                } else {
                    guild.setMaster(target);
                }

                getPlayer(sender).sendMessage(messages.getGuildSubCommandGuildTargetSuccessMessage("promote", "sender-message", guild.getTag(), guild.getName(), Bukkit.getOfflinePlayer(target).getName()));
                if (getPlayer(target) != null) {
                    getPlayer(target).sendMessage(messages.getGuildSubCommandGuildSenderSuccessMessage("promote", "target-message", tag, name, getOfflinePlayer(sender).getName()));
                }
                for (UUID journeyman : guild.getJourneymen()) {
                    Player player = getPlayer(journeyman);
                    if (player != null && player.getUniqueId() != sender && player.getUniqueId() != target) {
                        player.sendMessage(messages.getGuildSubCommandAllSuccessMessage("promote", "affiliated-message", tag, name, getOfflinePlayer(sender).getName(), getOfflinePlayer(target).getName()));
                    }
                }
                for (UUID apprentice : guild.getApprentices()) {
                    Player player = getPlayer(apprentice);
                    if (player != null && player.getUniqueId() != sender && player.getUniqueId() != target) {
                        player.sendMessage(messages.getGuildSubCommandAllSuccessMessage("promote", "affiliated-message", tag, name, getOfflinePlayer(sender).getName(), getOfflinePlayer(target).getName()));
                    }
                }

                return true;
            } catch (Exception exception) {
                getPlayer(sender).sendMessage(exception.getMessage());
                return false;
            }
        }
    }

    class DemoteSubCommand implements GuildSubCommand {
        @Override
        public boolean execute(UUID sender, String[] arguments) {
            try {
                if (arguments.length != 1) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("demote", "illegal-syntax"));
                }

                if (!manager.isPlayerAffiliated(sender)) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("demote", "sender-not-affiliated"));
                }

                Guild guild = manager.getGuildByPlayer(sender);
                String tag = guild.getTag();
                String name = guild.getName();

                if (!manager.isPlayerMaster(sender)) {
                    throw new Exception(messages.getGuildSubCommandGuildErrorMessage("demote", "sender-not-master", tag, name));
                }

                UUID target = Bukkit.getOfflinePlayer(arguments[0]).getUniqueId();

                if (target == null) {
                    throw new Exception(messages.getGuildSubCommandTagErrorMessage("demote", "target-doesn't-exist", arguments[0]));
                } else if (sender.equals(target)) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("demote", "sender-is-target"));
                } else if (!guild.isAffiliated(target)) {
                    throw new Exception(messages.getGuildSubCommandGuildTargetErrorMessage("demote", "target-not-affiliated", tag, name, Bukkit.getOfflinePlayer(target).getName()));
                }

                if (manager.isPlayerApprentice(target)) {
                    throw new Exception(messages.getGuildSubCommandGuildTargetErrorMessage("demote", "target-is-apprentice", tag, name, Bukkit.getOfflinePlayer(target).getName()));
                }

                getPlayer(sender).sendMessage(messages.getGuildSubCommandGuildTargetSuccessMessage("demote", "sender-message", guild.getTag(), guild.getName(), Bukkit.getOfflinePlayer(target).getName()));
                if (getPlayer(target) != null) {
                    getPlayer(target).sendMessage(messages.getGuildSubCommandGuildSenderSuccessMessage("promote", "target-message", tag, name, getOfflinePlayer(sender).getName()));
                }
                for (UUID journeyman : guild.getJourneymen()) {
                    Player player = getPlayer(journeyman);
                    if (player != null && player.getUniqueId() != sender && player.getUniqueId() != target) {
                        player.sendMessage(messages.getGuildSubCommandAllSuccessMessage("promote", "affiliated-message", tag, name, getOfflinePlayer(sender).getName(), getOfflinePlayer(target).getName()));
                    }
                }
                for (UUID apprentice : guild.getApprentices()) {
                    Player player = getPlayer(apprentice);
                    if (player != null && player.getUniqueId() != sender && player.getUniqueId() != target) {
                        player.sendMessage(messages.getGuildSubCommandAllSuccessMessage("promote", "affiliated-message", tag, name, getOfflinePlayer(sender).getName(), getOfflinePlayer(target).getName()));
                    }
                }
                guild.removeJourneyman(target);
                return true;
            } catch (Exception exception) {
                getPlayer(sender).sendMessage(exception.getMessage());
                return false;
            }
        }
    }

    class RetagSubCommand implements GuildSubCommand {
        @Override
        public boolean execute(UUID sender, String[] arguments) {
            try {
                if (arguments.length != 1) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("retag", "illegal-syntax"));
                }

                if (!manager.isPlayerAffiliated(sender)) {
                    throw new Exception(messages.getGuildSubCommandErrorMessage("retag", "sender-not-affiliated"));
                }

                Guild guild = manager.getGuildByPlayer(sender);
                String tag = guild.getTag();
                String name = guild.getName();
                if (!manager.isPlayerMaster(sender)) {
                    throw new Exception(messages.getGuildSubCommandGuildErrorMessage("retag", "sender-not-master", tag, name));
                }

                String newTag = arguments[0];
                if (newTag.matches(".*[^a-zA-Z0-9].*")) {
                    throw new Exception(messages.getGuildSubCommandTagErrorMessage("retag", "illegal-tag", newTag));
                } else if (manager.isGuildTagTaken(newTag)) {
                    throw new Exception(messages.getGuildSubCommandTagErrorMessage("retag", "tag-is-taken", newTag));
                }

                manager.getGuildByPlayer(sender).setTag(newTag);
                getPlayer(sender).sendMessage(messages.getGuildSubCommandGuildSuccessMessage("retag", "sender-message", newTag, name));
                for (UUID journeyman : guild.getJourneymen()) {
                    Player player = getPlayer(journeyman);
                    if (player != null) {
                        player.sendMessage(messages.getGuildSubCommandGuildSenderSuccessMessage("retag", "affiliated-message", newTag, name, getPlayer(sender).getName()));
                    }
                }
                for (UUID apprentice : guild.getApprentices()) {
                    Player player = getPlayer(apprentice);
                    if (player != null) {
                        player.sendMessage(messages.getGuildSubCommandGuildSenderSuccessMessage("retag", "affiliated-message", newTag, name, getPlayer(sender).getName()));
                    }
                }

                return true;
            } catch (Exception exception) {
                getPlayer(sender).sendMessage(exception.getMessage());
                return false;
            }
        }
    }
}
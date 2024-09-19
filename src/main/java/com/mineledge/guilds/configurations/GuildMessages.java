package com.mineledge.guilds.configurations;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class GuildMessages {
    private final YamlConfiguration config;

    public GuildMessages(File file) {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public String getGuildCommandErrorMessage(String message) {
        return translateColorCodes(config.getString("errors.commands.guild." + message));
    }

    public String getGuildSubCommandSuccessMessage(String subcommand, String message) {
        return translateColorCodes(config.getString("successes.commands.guild.subcommands." + subcommand + "." + message));
    }

    public String getGuildSubCommandGuildSuccessMessage(String subcommand, String message, String tag, String name) {
        return translateGuildPlaceholders(getGuildSubCommandSuccessMessage(subcommand, message), tag, name);
    }

    public String getGuildSubCommandGuildSenderSuccessMessage(String subcommand, String message, String tag, String name, String sender) {
        return translateGuildSenderPlaceholders(getGuildSubCommandSuccessMessage(subcommand, message), tag, name, sender);
    }

    public String getGuildSubCommandGuildTargetSuccessMessage(String subcommand, String message, String tag, String name, String target) {
        return translateGuildTargetPlaceholders(getGuildSubCommandSuccessMessage(subcommand, message), tag, name, target);
    }

    public String getGuildSubCommandAllSuccessMessage(String subcommand, String message, String tag, String name, String sender, String target) {
        return translatePlaceholders(getGuildSubCommandSuccessMessage(subcommand, message), tag, name, sender, target);
    }

    public String getGuildSubCommandErrorMessage(String subcommand, String message) {
        return translateColorCodes(config.getString("errors.commands.guild.subcommands." + subcommand + "." + message));
    }

    public String getGuildSubCommandTargetErrorMessage(String subcommand, String message, String target) {
        return translateTargetPlaceholder(getGuildSubCommandErrorMessage(subcommand, message), target);
    }

    public String getGuildSubCommandTagErrorMessage(String subcommand, String message, String tag) {
        return translateTagPlaceholder(getGuildSubCommandErrorMessage(subcommand, message), tag);
    }

    public String getGuildSubCommandNameErrorMessage(String subcommand, String message, String name) {
        return translateNamePlaceholder(getGuildSubCommandErrorMessage(subcommand, message), name);
    }

    public String getGuildSubCommandGuildErrorMessage(String subcommand, String message, String tag, String name) {
        return translateGuildPlaceholders(getGuildSubCommandErrorMessage(subcommand, message), tag, name);
    }

    public String getGuildSubCommandGuildTargetErrorMessage(String subcommand, String message, String tag, String name, String target) {
        return translateGuildTargetPlaceholders(getGuildSubCommandErrorMessage(subcommand, message), tag, name, target);
    }

    private static String translateColorCodes(String message) {
        return message.replace("&", "§");
    }

    private static String translateTargetPlaceholder(String message, String target) {
        return message.replace("{target}", target);
    }

    private static String translateTagPlaceholder(String message, String tag) {
        return message.replace("{tag}", tag);
    }

    private static String translateNamePlaceholder(String message, String name) {
        return message.replace("{tag}", name);
    }

    private static String translateGuildPlaceholders(String message, String tag, String name) {
        return message.replace("{tag}", tag).replace("{name}", name);
    }

    private static String translateGuildSenderPlaceholders(String message, String tag, String name, String sender) {
        return message.replace("{tag}", tag).replace("{name}", name).replace("{sender}", sender);
    }

    private static String translateGuildTargetPlaceholders(String message, String tag, String name, String target) {
        return message.replace("{tag}", tag).replace("{name}", name).replace("{target}", target);
    }

    private static String translatePlaceholders(String message, String tag, String name, String sender, String target) {
        return message.replace("{tag}", tag).replace("{name}", name).replace("{sender}", sender).replace("{target}", target);
    }
}

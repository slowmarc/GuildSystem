package com.mineledge.guilds.configurations;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class GuildMessages {
    private final YamlConfiguration config;

    public GuildMessages(File file) {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public String getSuccessMessage(String subcommand, String message) {
        return translateColorCodes(config.getString("successes.commands.guild.subcommands." + subcommand + "." + message));
    }

    public String getGuildSuccessMessage(String subcommand, String message, String tag, String name) {
        return translateGuildPlaceholders(getSuccessMessage(subcommand, message), tag, name);
    }

    public String getSuccessMessage(String subcommand, String message, String tag, String name, String sender, String target) {
        return translatePlaceholders(getSuccessMessage(subcommand, message), tag, name, sender, target);
    }

    public String getErrorMessage(String subcommand, String message) {
        return translateColorCodes(config.getString("errors.commands.guild.subcommands." + subcommand + "." + message));
    }

    public String getTagErrorMessage(String subcommand, String message, String tag) {
        return translateTagPlaceholder(getErrorMessage(subcommand, message), tag);
    }

    public String getNameErrorMessage(String subcommand, String message, String name) {
        return translateNamePlaceholder(getErrorMessage(subcommand, message), name);
    }

    public String getGuildErrorMessage(String subcommand, String message, String tag, String name) {
        return translateGuildPlaceholders(getErrorMessage(subcommand, message), tag, name);
    }

    private static String translateTagPlaceholder(String message, String tag) {
        return message.replace("{tag}", tag);
    }

    private static String translateColorCodes(String message) {
        return message.replace("&", "§")
    }

    private static String translateNamePlaceholder(String message, String name) {
        return message.replace("{tag}", name);
    }

    private static String translateGuildPlaceholders(String message, String tag, String name) {
        return message.replace("{tag}", tag).replace("{name}", name);
    }

    private static String translatePlaceholders(String message, String tag, String name, String sender, String target) {
        return message.replace("{tag}", tag).replace("{name}", name).replace("{sender}", sender).replace("{target}", target);
    }
}

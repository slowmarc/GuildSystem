package com.mineledge.guilds.managers;

import com.mineledge.guilds.models.Guild;
import com.mineledge.guilds.repositories.GuildRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GuildManager {
    private final Set<Guild> guilds;
    private final GuildRepository repository;

    public GuildManager(GuildRepository repository) {
        this.repository = repository;
        guilds = repository.load();
    }

    public Set<Guild> getGuilds() {
        return guilds;
    }

    public GuildRepository getRepository() {
        return repository;
    }

    public Guild getGuildByTag(String tag) {
        for (Guild guild : guilds) {
            if (guild.getTag().equalsIgnoreCase(tag)) {
                return guild;
            }
        }

        return null;
    }

    public Guild getGuildByName(String name) {
        for (Guild guild : guilds) {
            if (guild.getName().equalsIgnoreCase(name)) {
                return guild;
            }
        }

        return null;
    }

    public Guild getGuildByPlayer(UUID target) {
        for (Guild guild : guilds) {
            if (guild.isAffiliated(target)) {
                return guild;
            }
        }

        return null;
    }

    public Set<Guild> getInvitingGuildsByPlayer(UUID target) {
        Set<Guild> invitingGuilds = new HashSet<>();
        for (Guild guild : guilds) {
            if (guild.isRecipient(target)) {
                invitingGuilds.add(guild);
            }
        }

        return invitingGuilds;
    }

    public void addGuild(String tag, String name, UUID master) {
        UUID identifier;
        do {
            identifier = UUID.randomUUID();
        } while (isGuildIdentifierTaken(identifier));

        guilds.add(new Guild(identifier, tag, name, master));
    }

    public void removeGuild(Guild guild) {
        guilds.remove(guild);
    }

    public boolean isGuildIdentifierTaken(UUID identifier) {
        for (Guild guild : guilds) {
            if (guild.getIdentifier().equals(identifier)) {
                return true;
            }
        }

        return false;
    }

    public boolean isGuildTagTaken(String tag) {
        for (Guild guild : guilds) {
            if (guild.getTag().equalsIgnoreCase(tag)) {
                return true;
            }
        }

        return false;
    }

    public boolean isGuildNameTaken(String name) {
        for (Guild guild : guilds) {
            if (guild.getTag().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    public boolean isPlayerMaster(UUID target) {
        return getGuildByPlayer(target).isMaster(target);
    }

    public boolean isPlayerJourneyman(UUID target) {
        return getGuildByPlayer(target).isJourneyman(target);
    }

    public boolean isPlayerApprentice(UUID target) {
        return getGuildByPlayer(target).isApprentice(target);
    }

    public boolean isPlayerRecipient(Guild guild, UUID target) {
        return guild.isRecipient(target);
    }

    public boolean isPlayerAffiliated(UUID target) {
        for (Guild guild : guilds) {
            if (guild.isAffiliated(target)) {
                return true;
            }
        }

        return false;
    }
}

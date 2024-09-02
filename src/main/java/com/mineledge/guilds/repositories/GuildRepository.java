package com.mineledge.guilds.repositories;

import com.mineledge.guilds.models.Guild;

import java.util.Set;

public interface GuildRepository {
    Set<Guild> load();

    void save(Set<Guild> guilds);
}

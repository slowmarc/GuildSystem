package com.mineledge.guilds.models;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Guild {
    private final UUID identification;
    private String tag;
    private String name;
    private final Date creationDate;

    private UUID master;
    private final Set<UUID> journeymen;
    private final Set<UUID> apprentices;
    private final Set<UUID> recipients;

    public Guild(UUID identification, String tag, String name, UUID master) {
        this.identification = identification;
        this.tag = tag;
        this.name = name;
        creationDate = new Date();

        this.master = master;
        journeymen = new HashSet<>();
        apprentices = new HashSet<>();
        recipients = new HashSet<>();
    }

    public Guild(UUID identification, String tag, String name, Date creationDate, UUID master, Set<UUID> journeymen, Set<UUID> apprentices, Set<UUID> recipients) {
        this.identification = identification;
        this.tag = tag;
        this.name = name;
        this.creationDate = creationDate;

        this.master = master;
        this.journeymen = journeymen;
        this.apprentices = apprentices;
        this.recipients = recipients;
    }

    public UUID getIdentification() {
        return identification;
    }

    public String getTag() {
        return tag;
    }

    public String getName() {
        return name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public UUID getMaster() {
        return master;
    }

    public Set<UUID> getJourneymen() {
        return journeymen;
    }

    public Set<UUID> getApprentices() {
        return apprentices;
    }

    public Set<UUID> getRecipients() {
        return recipients;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMaster(UUID target) {
        journeymen.remove(target);
        journeymen.add(master);

        this.master = target;
    }

    public void addJourneyman(UUID target) {
        apprentices.remove(target);
        journeymen.add(target);
    }

    public void addApprentice(UUID target) {
        recipients.remove(target);
        apprentices.add(target);
    }

    public void addRecipient(UUID target) {
        recipients.add(target);
    }

    public void removeJourneyman(UUID target) {
        journeymen.remove(target);
        apprentices.add(target);
    }

    public void removeApprentice(UUID target) {
        apprentices.remove(target);
    }

    public void removeRecipient(UUID target) {
        recipients.remove(target);
    }

    public boolean isMaster(UUID target) {
        return master.equals(target);
    }

    public boolean isJourneyman(UUID target) {
        return journeymen.contains(target);
    }

    public boolean isApprentice(UUID target) {
        return apprentices.contains(target);
    }

    public boolean isRecipient(UUID target) {
        return recipients.contains(target);
    }
}
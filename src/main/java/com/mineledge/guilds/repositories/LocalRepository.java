package com.mineledge.guilds.repositories;

import com.mineledge.guilds.models.Guild;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LocalRepository implements GuildRepository {
    private final File file;

    public LocalRepository(File file) {
        this.file = file;

        if (!file.exists()) {
            try {
                file.createNewFile();

                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(new JSONArray().toJSONString());
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Set<Guild> load() {
        Set<Guild> guilds = new HashSet<>();
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(file)) {
            JSONArray jsonArray = (JSONArray) parser.parse(reader);

            for (Object object : jsonArray) {
                JSONObject jsonObject = (JSONObject) object;
                Guild guild = parseGuild(jsonObject);
                if (guild != null) {
                    guilds.add(guild);
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return guilds;
    }

    @Override
    public void save(Set<Guild> guilds) {
        JSONArray jsonArray = new JSONArray();

        for (Guild guild : guilds) {
            jsonArray.add(parseJsonObject(guild));
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(jsonArray.toJSONString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Guild parseGuild(JSONObject jsonObject) {
        UUID identifier = UUID.fromString((String) jsonObject.get("identifier"));
        String tag = (String) jsonObject.get("tag");
        String name = (String) jsonObject.get("name");
        String creationDate = (String) jsonObject.get("date");

        UUID master = UUID.fromString((String) jsonObject.get("master"));
        Set<UUID> journeymen = parseUUIDSet((JSONArray) jsonObject.get("journeymen"));
        Set<UUID> apprentices = parseUUIDSet((JSONArray) jsonObject.get("apprentices"));
        Set<UUID> recipients = parseUUIDSet((JSONArray) jsonObject.get("recipients"));

        return new Guild(identifier, tag, name, creationDate, master, journeymen, apprentices, recipients);
    }

    private Set<UUID> parseUUIDSet(JSONArray jsonArray) {
        Set<UUID> uuidSet = new HashSet<>();
        for (Object object : jsonArray) {
            uuidSet.add(UUID.fromString((String) object));
        }

        return uuidSet;
    }

    private JSONObject parseJsonObject(Guild guild) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("identifier", guild.getIdentifier().toString());
        jsonObject.put("tag", guild.getTag());
        jsonObject.put("name", guild.getName());
        jsonObject.put("creationDate", guild.getCreationDate());

        jsonObject.put("master", guild.getMaster().toString());
        jsonObject.put("journeymen", parseJsonArray(guild.getJourneymen()));
        jsonObject.put("apprentices", parseJsonArray(guild.getApprentices()));
        jsonObject.put("recipients", parseJsonArray(guild.getRecipients()));

        return jsonObject;
    }

    private JSONArray parseJsonArray(Set<UUID> uuidSet) {
        JSONArray jsonArray = new JSONArray();
        for (UUID uuid : uuidSet) {
            jsonArray.add(uuid.toString());
        }
        return jsonArray;
    }
}
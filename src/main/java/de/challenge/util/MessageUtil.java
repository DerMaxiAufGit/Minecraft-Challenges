package de.challenge.util;

import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Map;

public class MessageUtil {

    private final FileConfiguration messages;
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    public MessageUtil(ChallengePlugin plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        this.messages = YamlConfiguration.loadConfiguration(file);
    }

    public String getRaw(String key, Player player) {
        String locale = getLocale(player);
        String msg = messages.getString(locale + "." + key);
        if (msg == null) {
            msg = messages.getString("en." + key);
        }
        if (msg == null) {
            return key;
        }
        return msg;
    }

    public String getRaw(String key, Player player, Map<String, String> placeholders) {
        String msg = getRaw(key, player);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return msg;
    }

    public Component get(String key, Player player) {
        return LEGACY.deserialize(getRaw(key, player));
    }

    public Component get(String key, Player player, Map<String, String> placeholders) {
        return LEGACY.deserialize(getRaw(key, player, placeholders));
    }

    private String getLocale(Player player) {
        String locale = player.locale().getLanguage();
        if ("de".equalsIgnoreCase(locale)) return "de";
        return "en";
    }
}

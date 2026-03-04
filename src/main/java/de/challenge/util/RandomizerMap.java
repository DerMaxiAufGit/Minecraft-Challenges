package de.challenge.util;

import de.challenge.ChallengePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Generic persistent mapping used by randomizer challenges.
 * Maps keys (Material names, EntityType names) to values (Material names).
 */
public class RandomizerMap<K, V> {

    private final ChallengePlugin plugin;
    private final String fileName;
    private final Map<K, V> mapping = new LinkedHashMap<>();
    private final KeySerializer<K> keySerializer;
    private final ValueSerializer<V> valueSerializer;

    public interface KeySerializer<K> {
        String serialize(K key);
        K deserialize(String raw);
    }

    public interface ValueSerializer<V> {
        String serialize(V value);
        V deserialize(String raw);
    }

    public RandomizerMap(ChallengePlugin plugin, String fileName,
                         KeySerializer<K> keySerializer, ValueSerializer<V> valueSerializer) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

    public void generate(List<K> keys, List<V> values) {
        mapping.clear();
        List<V> shuffled = new ArrayList<>(values);
        Collections.shuffle(shuffled);
        for (int i = 0; i < keys.size(); i++) {
            mapping.put(keys.get(i), shuffled.get(i % shuffled.size()));
        }
        save();
    }

    public V get(K key) {
        return mapping.get(key);
    }

    public V getOrDefault(K key, V defaultValue) {
        return mapping.getOrDefault(key, defaultValue);
    }

    public boolean isEmpty() {
        return mapping.isEmpty();
    }

    public void save() {
        File file = new File(plugin.getDataFolder(), fileName);
        FileConfiguration config = new YamlConfiguration();
        for (Map.Entry<K, V> entry : mapping.entrySet()) {
            config.set(keySerializer.serialize(entry.getKey()), valueSerializer.serialize(entry.getValue()));
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save randomizer map " + fileName + ": " + e.getMessage());
        }
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        mapping.clear();
        for (String key : config.getKeys(false)) {
            try {
                K k = keySerializer.deserialize(key);
                V v = valueSerializer.deserialize(config.getString(key));
                if (k != null && v != null) {
                    mapping.put(k, v);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid entry in " + fileName + ": " + key);
            }
        }
    }

    public void delete() {
        mapping.clear();
        File file = new File(plugin.getDataFolder(), fileName);
        if (file.exists()) {
            file.delete();
        }
    }
}

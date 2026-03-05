package de.challenge;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ChallengeSettingsManager {

    private final ChallengePlugin plugin;
    private final File settingsFile;
    private FileConfiguration settings;

    public ChallengeSettingsManager(ChallengePlugin plugin) {
        this.plugin = plugin;
        this.settingsFile = new File(plugin.getDataFolder(), "challenge-settings.yml");
    }

    public void load() {
        if (settingsFile.exists()) {
            settings = YamlConfiguration.loadConfiguration(settingsFile);
        } else {
            settings = new YamlConfiguration();
        }
    }

    public void save() {
        try {
            settings.save(settingsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save challenge settings: " + e.getMessage());
        }
    }

    public int getInt(String path, int defaultValue) {
        if (settings.contains(path)) return settings.getInt(path);
        return plugin.getConfig().getInt(path, defaultValue);
    }

    public long getLong(String path, long defaultValue) {
        if (settings.contains(path)) return settings.getLong(path);
        return plugin.getConfig().getLong(path, defaultValue);
    }

    public double getDouble(String path, double defaultValue) {
        if (settings.contains(path)) return settings.getDouble(path);
        return plugin.getConfig().getDouble(path, defaultValue);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        if (settings.contains(path)) return settings.getBoolean(path);
        return plugin.getConfig().getBoolean(path, defaultValue);
    }

    public String getString(String path, String defaultValue) {
        if (settings.contains(path)) return settings.getString(path);
        return plugin.getConfig().getString(path, defaultValue);
    }

    public void set(String path, Object value) {
        settings.set(path, value);
        save();
    }

    public Object get(String path) {
        if (settings.contains(path)) return settings.get(path);
        return plugin.getConfig().get(path);
    }

    public boolean hasCustomValue(String path) {
        return settings.contains(path);
    }

    public void resetValue(String path) {
        settings.set(path, null);
        save();
    }
}

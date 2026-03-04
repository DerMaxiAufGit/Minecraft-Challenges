package de.challenge.challenges.projects;

import de.challenge.Challenge;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class Project extends Challenge {

    protected int currentIndex = 0;
    protected boolean complete = false;

    public Project(ChallengePlugin plugin) {
        super(plugin);
    }

    public abstract List<String> getObjectives();

    public int getCurrentIndex() { return currentIndex; }

    public boolean isComplete() { return complete; }

    public String getCurrentObjective() {
        List<String> objectives = getObjectives();
        if (currentIndex >= objectives.size()) return null;
        return objectives.get(currentIndex);
    }

    protected void advanceObjective(Player player) {
        currentIndex++;
        List<String> objectives = getObjectives();

        if (currentIndex >= objectives.size()) {
            complete = true;
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(Component.text(getDisplayName() + " Complete!", NamedTextColor.GREEN));
            }
        } else {
            player.sendMessage(Component.text(
                    "Progress: " + currentIndex + "/" + objectives.size()
                            + " | Next: " + objectives.get(currentIndex),
                    NamedTextColor.YELLOW));
        }
        saveProgress();
    }

    protected void saveProgress() {
        File file = new File(plugin.getDataFolder(), "project_" + getId() + ".yml");
        FileConfiguration config = new YamlConfiguration();
        config.set("index", currentIndex);
        config.set("complete", complete);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save project " + getId() + ": " + e.getMessage());
        }
    }

    protected void loadProgress() {
        File file = new File(plugin.getDataFolder(), "project_" + getId() + ".yml");
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        currentIndex = config.getInt("index", 0);
        complete = config.getBoolean("complete", false);
    }

    @Override
    protected void onEnable() {
        loadProgress();
    }

    @Override
    protected void onDisable() {
        saveProgress();
    }
}

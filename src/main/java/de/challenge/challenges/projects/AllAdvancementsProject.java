package de.challenge.challenges.projects;

import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AllAdvancementsProject extends Project {

    private List<String> objectives;
    private final Set<String> completed = new HashSet<>();

    public AllAdvancementsProject(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "all_advancements"; }

    @Override
    public String getDisplayName() { return "All Advancements"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.GOLD_BLOCK); }

    @Override
    public String getDescription() { return "Complete every advancement"; }

    @Override
    public List<String> getObjectives() {
        if (objectives == null) {
            objectives = new ArrayList<>();
            Iterator<Advancement> iter = Bukkit.advancementIterator();
            while (iter.hasNext()) {
                Advancement adv = iter.next();
                // Exclude recipe advancements
                if (!adv.getKey().getKey().startsWith("recipes/")) {
                    objectives.add(adv.getKey().toString());
                }
            }
        }
        return objectives;
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        if (!active || complete) return;
        if (event.getAdvancement().getKey().getKey().startsWith("recipes/")) return;

        String key = event.getAdvancement().getKey().toString();
        if (completed.add(key)) {
            int total = getObjectives().size();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(Component.text(
                        "Advancements: " + completed.size() + "/" + total, NamedTextColor.YELLOW));
            }
            if (completed.size() >= total) {
                complete = true;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(Component.text("All Advancements Complete!", NamedTextColor.GREEN));
                }
            }
            saveProgress();
        }
    }

    @Override
    protected void saveProgress() {
        super.saveProgress();
        // Also save completed set via config
        org.bukkit.configuration.file.FileConfiguration config =
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                        new java.io.File(plugin.getDataFolder(), "project_" + getId() + ".yml"));
        config.set("completed", new ArrayList<>(completed));
        try {
            config.save(new java.io.File(plugin.getDataFolder(), "project_" + getId() + ".yml"));
        } catch (java.io.IOException e) {
            plugin.getLogger().warning("Failed to save advancement progress: " + e.getMessage());
        }
    }

    @Override
    protected void loadProgress() {
        super.loadProgress();
        java.io.File file = new java.io.File(plugin.getDataFolder(), "project_" + getId() + ".yml");
        if (!file.exists()) return;
        org.bukkit.configuration.file.FileConfiguration config =
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
        List<String> list = config.getStringList("completed");
        completed.addAll(list);
    }
}

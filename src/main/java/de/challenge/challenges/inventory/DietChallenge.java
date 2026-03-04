package de.challenge.challenges.inventory;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DietChallenge extends Challenge {

    private final Map<UUID, Set<Material>> eatenFoods = new HashMap<>();

    public DietChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "diet"; }

    @Override
    public String getDisplayName() { return "Diet"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.APPLE); }

    @Override
    public String getDescription() { return "Each food type can only be eaten once"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.INVENTORY; }

    @Override
    protected void onEnable() {
        loadEatenFoods();
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!active) return;
        Player player = event.getPlayer();
        Material food = event.getItem().getType();

        Set<Material> eaten = eatenFoods.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        if (eaten.contains(food)) {
            event.setCancelled(true);
            String foodName = food.name().toLowerCase().replace('_', ' ');
            player.sendMessage(Component.text("You already ate " + foodName + "!", NamedTextColor.RED));
        } else {
            eaten.add(food);
        }
    }

    @Override
    protected void onDisable() {
        if (preservingState) {
            saveEatenFoods();
        } else {
            File file = new File(plugin.getDataFolder(), "diet_state.yml");
            if (file.exists()) {
                file.delete();
            }
        }
        eatenFoods.clear();
    }

    private void saveEatenFoods() {
        File file = new File(plugin.getDataFolder(), "diet_state.yml");
        FileConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, Set<Material>> entry : eatenFoods.entrySet()) {
            List<String> foods = entry.getValue().stream()
                    .map(Material::name)
                    .collect(Collectors.toList());
            config.set(entry.getKey().toString(), foods);
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save diet state: " + e.getMessage());
        }
    }

    private void loadEatenFoods() {
        File file = new File(plugin.getDataFolder(), "diet_state.yml");
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                Set<Material> foods = new HashSet<>();
                for (String name : config.getStringList(key)) {
                    try {
                        foods.add(Material.valueOf(name));
                    } catch (IllegalArgumentException ignored) {}
                }
                eatenFoods.put(uuid, foods);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in diet_state.yml: " + key);
            }
        }
    }
}

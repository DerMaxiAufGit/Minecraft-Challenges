package de.challenge.challenges.movement;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OnlyUpwardChallenge extends Challenge {

    private final Map<UUID, Double> maxY = new HashMap<>();

    public OnlyUpwardChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "only_upward"; }
    @Override public String getDisplayName() { return "Only Upward"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.ARROW); }
    @Override public String getDescription() { return "You can never go below your highest Y level"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.MOVEMENT; }

    @Override
    protected void onEnable() {
        loadState();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!active) return;
        if (event.getTo() == null) return;
        Player player = event.getPlayer();
        if (player.isInsideVehicle() || player.isInWater() || player.isGliding()) return;

        UUID uuid = player.getUniqueId();
        double currentY = event.getTo().getY();
        double highest = maxY.getOrDefault(uuid, currentY);

        if (currentY > highest) {
            maxY.put(uuid, currentY);
        } else if (currentY < highest - 0.1) {
            player.setHealth(0);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!active) return;
        maxY.remove(event.getPlayer().getUniqueId());
    }

    @Override
    protected void onDisable() {
        if (preservingState) {
            saveState();
        } else {
            File file = new File(plugin.getDataFolder(), "only_upward_state.yml");
            if (file.exists()) file.delete();
        }
        maxY.clear();
    }

    private void saveState() {
        File file = new File(plugin.getDataFolder(), "only_upward_state.yml");
        FileConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, Double> entry : maxY.entrySet()) {
            config.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save only upward state: " + e.getMessage());
        }
    }

    private void loadState() {
        File file = new File(plugin.getDataFolder(), "only_upward_state.yml");
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                maxY.put(uuid, config.getDouble(key));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in only_upward_state.yml: " + key);
            }
        }
    }
}

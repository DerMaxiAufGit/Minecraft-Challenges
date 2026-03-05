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

public class OnlyDownwardChallenge extends Challenge {

    private final Map<UUID, Double> minY = new HashMap<>();

    public OnlyDownwardChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "only_downward"; }
    @Override public String getDisplayName() { return "Only Downward"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.ANVIL); }
    @Override public String getDescription() { return "You can never go above your lowest Y level"; }
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
        double lowest = minY.getOrDefault(uuid, currentY);

        if (currentY < lowest) {
            minY.put(uuid, currentY);
        } else if (currentY > lowest + 0.1) {
            player.setHealth(0);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!active) return;
        minY.remove(event.getPlayer().getUniqueId());
    }

    @Override
    protected void onDisable() {
        if (preservingState) {
            saveState();
        } else {
            File file = new File(plugin.getDataFolder(), "only_downward_state.yml");
            if (file.exists()) file.delete();
        }
        minY.clear();
    }

    private void saveState() {
        File file = new File(plugin.getDataFolder(), "only_downward_state.yml");
        FileConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, Double> entry : minY.entrySet()) {
            config.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save only downward state: " + e.getMessage());
        }
    }

    private void loadState() {
        File file = new File(plugin.getDataFolder(), "only_downward_state.yml");
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                minY.put(uuid, config.getDouble(key));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in only_downward_state.yml: " + key);
            }
        }
    }
}

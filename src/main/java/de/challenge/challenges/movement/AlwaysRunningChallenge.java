package de.challenge.challenges.movement;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AlwaysRunningChallenge extends Challenge {

    private BukkitTask task;
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final Map<UUID, Integer> idleSeconds = new HashMap<>();

    public AlwaysRunningChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "always_running"; }

    @Override
    public String getDisplayName() { return "Always Running"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.SUGAR); }

    @Override
    public String getDescription() { return "You must keep moving or take damage"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.MOVEMENT; }

    @Override
    protected void onEnable() {
        int maxIdle = plugin.getSettingsManager().getInt("always-running.max-idle-seconds", 3);
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead() || player.isSleeping()) continue;
                if (player.getOpenInventory().getTopInventory().getSize() > 0
                        && player.getOpenInventory().getType() != org.bukkit.event.inventory.InventoryType.CRAFTING) {
                    continue;
                }

                UUID uuid = player.getUniqueId();
                Location current = player.getLocation();
                Location last = lastLocations.get(uuid);

                if (last != null && last.getWorld().equals(current.getWorld())
                        && last.distanceSquared(current) < 0.1) {
                    int idle = idleSeconds.getOrDefault(uuid, 0) + 1;
                    idleSeconds.put(uuid, idle);
                    if (idle >= maxIdle) {
                        player.damage(1.0);
                        player.sendMessage(Component.text("Keep moving!", NamedTextColor.RED));
                    }
                } else {
                    idleSeconds.put(uuid, 0);
                }
                lastLocations.put(uuid, current.clone());
            }
        }, 0L, 20L);
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("always-running.max-idle-seconds", "Max Idle (s)", 3, 1, 30, 1)
        );
    }

    @Override
    protected void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        lastLocations.clear();
        idleSeconds.clear();
    }
}

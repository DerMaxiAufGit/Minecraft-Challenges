package de.challenge.challenges.damage;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoEqualHeartsChallenge extends Challenge {

    private BukkitTask task;

    public NoEqualHeartsChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "no_equal_hearts"; }
    @Override public String getDisplayName() { return "No Equal Hearts"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.REDSTONE); }
    @Override public String getDescription() { return "Players with the same health die"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.DAMAGE; }

    @Override
    protected void onEnable() {
        int interval = plugin.getSettingsManager().getInt("no-equal-hearts.check-interval-ticks", 20);
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Map<Double, List<Player>> healthMap = new HashMap<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead()) continue;
                double health = Math.round(player.getHealth() * 2) / 2.0;
                healthMap.computeIfAbsent(health, k -> new ArrayList<>()).add(player);
            }
            for (List<Player> group : healthMap.values()) {
                if (group.size() > 1) {
                    for (Player player : group) {
                        player.setHealth(0);
                    }
                }
            }
        }, 40L, interval);
    }

    @Override
    protected void onDisable() {
        if (task != null) { task.cancel(); task = null; }
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("no-equal-hearts.check-interval-ticks", "Check Interval (ticks)", 20, 10, 100, 5)
        );
    }
}

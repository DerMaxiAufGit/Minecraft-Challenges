package de.challenge.challenges.movement;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class SocialDistancingChallenge extends Challenge {

    private BukkitTask task;

    public SocialDistancingChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "social_distancing"; }
    @Override public String getDisplayName() { return "Social Distancing"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.IRON_BARS); }
    @Override public String getDescription() { return "Take damage when too close to entities"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.MOVEMENT; }

    @Override
    protected void onEnable() {
        double minDist = plugin.getSettingsManager().getDouble("social-distancing.min-distance", 3.0);
        double damage = plugin.getSettingsManager().getDouble("social-distancing.damage", 1.0);
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead()) continue;
                for (Entity nearby : player.getNearbyEntities(minDist, minDist, minDist)) {
                    if (nearby instanceof LivingEntity && !(nearby instanceof Player)) {
                        player.damage(damage);
                        break;
                    }
                }
            }
        }, 0L, 20L);
    }

    @Override
    protected void onDisable() {
        if (task != null) { task.cancel(); task = null; }
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofDouble("social-distancing.min-distance", "Min Distance", 3.0, 1.0, 20.0, 0.5),
                ConfigurableSetting.ofDouble("social-distancing.damage", "Damage", 1.0, 0.5, 10.0, 0.5)
        );
    }
}

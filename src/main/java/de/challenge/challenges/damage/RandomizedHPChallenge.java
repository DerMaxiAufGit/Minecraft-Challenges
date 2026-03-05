package de.challenge.challenges.damage;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomizedHPChallenge extends Challenge {

    private BukkitTask task;

    public RandomizedHPChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "randomized_hp"; }
    @Override public String getDisplayName() { return "Randomized HP"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.GLISTERING_MELON_SLICE); }
    @Override public String getDescription() { return "Your health is randomized periodically"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.DAMAGE; }

    @Override
    protected void onEnable() {
        int intervalSeconds = plugin.getSettingsManager().getInt("randomized-hp.interval-seconds", 60);
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead()) continue;
                AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (attr == null) continue;
                double maxHealth = attr.getBaseValue();
                double newHealth = ThreadLocalRandom.current().nextDouble(1.0, maxHealth + 1);
                player.setHealth(Math.min(newHealth, maxHealth));
            }
        }, intervalSeconds * 20L, intervalSeconds * 20L);
    }

    @Override
    protected void onDisable() {
        if (task != null) { task.cancel(); task = null; }
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("randomized-hp.interval-seconds", "Interval (s)", 60, 10, 300, 10)
        );
    }
}

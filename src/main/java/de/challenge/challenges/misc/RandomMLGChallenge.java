package de.challenge.challenges.misc;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomMLGChallenge extends Challenge {

    private BukkitTask task;

    public RandomMLGChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "random_mlg"; }

    @Override
    public String getDisplayName() { return "Random MLG"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.WATER_BUCKET); }

    @Override
    public String getDescription() { return "Get teleported up randomly - land safely!"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.ENVIRONMENTAL; }

    @Override
    protected void onEnable() {
        scheduleNext();
    }

    private void scheduleNext() {
        int minInterval = plugin.getSettingsManager().getInt("random-mlg.min-interval-seconds", 600);
        int maxInterval = plugin.getSettingsManager().getInt("random-mlg.max-interval-seconds", 900);
        int warningSeconds = plugin.getSettingsManager().getInt("random-mlg.warning-seconds", 10);
        int height = plugin.getSettingsManager().getInt("random-mlg.height", 40);

        int interval = ThreadLocalRandom.current().nextInt(minInterval, maxInterval + 1);
        int warningDelay = Math.max(0, interval - warningSeconds);

        // Schedule warning
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!active) return;
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.showTitle(Title.title(
                        Component.text("MLG incoming!", NamedTextColor.YELLOW, TextDecoration.BOLD),
                        Component.text(warningSeconds + "s warning!", NamedTextColor.RED),
                        Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(3), Duration.ofMillis(500))
                ));
            }
        }, warningDelay * 20L);

        // Schedule teleport
        task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!active) return;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead()) continue;
                player.teleport(player.getLocation().add(0, height, 0));
                // Give water bucket if they don't have one
                if (!player.getInventory().contains(Material.WATER_BUCKET)) {
                    player.getInventory().addItem(new ItemStack(Material.WATER_BUCKET));
                }
            }
            scheduleNext();
        }, interval * 20L);
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("random-mlg.min-interval-seconds", "Min Interval (s)", 600, 60, 1800, 60),
                ConfigurableSetting.ofInt("random-mlg.max-interval-seconds", "Max Interval (s)", 900, 120, 3600, 60),
                ConfigurableSetting.ofInt("random-mlg.height", "Drop Height", 40, 10, 100, 5),
                ConfigurableSetting.ofInt("random-mlg.warning-seconds", "Warning Time (s)", 10, 3, 30, 1)
        );
    }

    @Override
    protected void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}

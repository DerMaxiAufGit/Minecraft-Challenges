package de.challenge.challenges.environmental;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class ShrinkingBorderChallenge extends Challenge {

    private BukkitTask task;
    private double originalSize;

    public ShrinkingBorderChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "shrinking_border"; }

    @Override
    public String getDisplayName() { return "Shrinking Border"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.STRUCTURE_VOID); }

    @Override
    public String getDescription() { return "The world border shrinks over time"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.ENVIRONMENTAL; }

    @Override
    protected void onEnable() {
        double startSize = plugin.getSettingsManager().getDouble("shrinking-border.start-size", 1000);
        double minSize = plugin.getSettingsManager().getDouble("shrinking-border.min-size", 50);
        double shrinkPerMinute = plugin.getSettingsManager().getDouble("shrinking-border.shrink-per-minute", 10);

        World overworld = Bukkit.getWorlds().get(0);
        WorldBorder border = overworld.getWorldBorder();
        originalSize = border.getSize();
        border.setSize(startSize);

        // Shrink every 60 seconds
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            double currentSize = border.getSize();
            double newSize = Math.max(minSize, currentSize - shrinkPerMinute);
            border.setSize(newSize, 60L); // smooth transition over 60 seconds
        }, 1200L, 1200L); // every 60 seconds
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofDouble("shrinking-border.start-size", "Start Size", 1000, 100, 10000, 100),
                ConfigurableSetting.ofDouble("shrinking-border.min-size", "Min Size", 50, 10, 500, 10),
                ConfigurableSetting.ofDouble("shrinking-border.shrink-per-minute", "Shrink Per Minute", 10, 1, 100, 1)
        );
    }

    @Override
    protected void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        // Restore original border size
        World overworld = Bukkit.getWorlds().get(0);
        WorldBorder border = overworld.getWorldBorder();
        border.setSize(originalSize);
    }
}

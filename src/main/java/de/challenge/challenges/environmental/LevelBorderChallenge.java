package de.challenge.challenges.environmental;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LevelBorderChallenge extends Challenge {

    private double originalSize;
    private double multiplier;
    private double minSize;

    public LevelBorderChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "level_border"; }

    @Override
    public String getDisplayName() { return "Level = Border"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.EXPERIENCE_BOTTLE); }

    @Override
    public String getDescription() { return "World border size equals your XP level x multiplier"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.ENVIRONMENTAL; }

    @Override
    protected void onEnable() {
        multiplier = plugin.getSettingsManager().getDouble("level-border.multiplier", 10.0);
        minSize = plugin.getSettingsManager().getDouble("level-border.min-size", 20.0);

        World overworld = Bukkit.getWorlds().get(0);
        originalSize = overworld.getWorldBorder().getSize();
        updateBorder();
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent event) {
        if (!active) return;
        updateBorder();
    }

    private void updateBorder() {
        // Use the highest level among all online players
        int maxLevel = 0;
        for (var player : Bukkit.getOnlinePlayers()) {
            maxLevel = Math.max(maxLevel, player.getLevel());
        }

        double newSize = Math.max(minSize, maxLevel * multiplier);
        World overworld = Bukkit.getWorlds().get(0);
        WorldBorder border = overworld.getWorldBorder();
        border.setSize(newSize, 2L); // smooth 2-second transition
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofDouble("level-border.multiplier", "Level Multiplier", 10.0, 1.0, 100.0, 1.0),
                ConfigurableSetting.ofDouble("level-border.min-size", "Min Size", 20.0, 5.0, 200.0, 5.0)
        );
    }

    @Override
    protected void onDisable() {
        World overworld = Bukkit.getWorlds().get(0);
        overworld.getWorldBorder().setSize(originalSize);
    }
}

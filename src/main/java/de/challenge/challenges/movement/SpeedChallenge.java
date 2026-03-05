package de.challenge.challenges.movement;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class SpeedChallenge extends Challenge {

    private BukkitTask task;

    public SpeedChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "speed"; }
    @Override public String getDisplayName() { return "Speed"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.SUGAR); }
    @Override public String getDescription() { return "Everything is faster"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.MOVEMENT; }

    @Override
    protected void onEnable() {
        int multiplier = plugin.getSettingsManager().getInt("speed.multiplier", 2);
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, multiplier - 1, true, false));
            }
        }, 0L, 40L);
    }

    @Override
    protected void onDisable() {
        if (task != null) { task.cancel(); task = null; }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.removePotionEffect(PotionEffectType.SPEED);
        }
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("speed.multiplier", "Speed Level", 2, 1, 10, 1)
        );
    }
}

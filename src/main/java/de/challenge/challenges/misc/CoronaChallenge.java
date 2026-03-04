package de.challenge.challenges.misc;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class CoronaChallenge extends Challenge {

    private BukkitTask task;

    public CoronaChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "corona"; }

    @Override
    public String getDisplayName() { return "Corona"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.FERMENTED_SPIDER_EYE); }

    @Override
    public String getDescription() { return "Stay away from mobs or get poisoned"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.MOBS; }

    @Override
    protected void onEnable() {
        int interval = plugin.getSettingsManager().getInt("corona.check-interval-ticks", 40);
        double distance = plugin.getSettingsManager().getDouble("corona.distance-blocks", 2.0);

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead()) continue;
                boolean tooClose = false;
                for (Entity entity : player.getNearbyEntities(distance, distance, distance)) {
                    if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                        tooClose = true;
                        break;
                    }
                }
                if (tooClose) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
                    player.sendActionBar(Component.text("\u26A0 Keep your distance!", NamedTextColor.RED));
                }
            }
        }, 0L, interval);
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("corona.check-interval-ticks", "Check Interval (ticks)", 40, 10, 200, 10),
                ConfigurableSetting.ofDouble("corona.distance-blocks", "Distance (blocks)", 2.0, 1.0, 10.0, 0.5)
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

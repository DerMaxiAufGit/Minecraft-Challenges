package de.challenge.challenges.damage;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class KillEffectChallenge extends Challenge {

    private static final List<PotionEffectType> SAFE_EFFECTS = List.of(
            PotionEffectType.SPEED, PotionEffectType.SLOWNESS, PotionEffectType.HASTE,
            PotionEffectType.MINING_FATIGUE, PotionEffectType.STRENGTH, PotionEffectType.JUMP_BOOST,
            PotionEffectType.NAUSEA, PotionEffectType.REGENERATION, PotionEffectType.RESISTANCE,
            PotionEffectType.FIRE_RESISTANCE, PotionEffectType.WATER_BREATHING, PotionEffectType.INVISIBILITY,
            PotionEffectType.BLINDNESS, PotionEffectType.NIGHT_VISION, PotionEffectType.HUNGER,
            PotionEffectType.WEAKNESS, PotionEffectType.POISON, PotionEffectType.GLOWING,
            PotionEffectType.LEVITATION, PotionEffectType.SLOW_FALLING
    );

    public KillEffectChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "kill_effect"; }
    @Override public String getDisplayName() { return "Kill = Effect"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.DRAGON_BREATH); }
    @Override public String getDescription() { return "Killing a mob gives you a random potion effect"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.DAMAGE; }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (!active) return;
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        boolean permanent = plugin.getSettingsManager().getBoolean("kill-effect.permanent", false);
        int durationSeconds = plugin.getSettingsManager().getInt("kill-effect.duration-seconds", 30);
        int duration = permanent ? Integer.MAX_VALUE : durationSeconds * 20;

        PotionEffectType type = SAFE_EFFECTS.get(ThreadLocalRandom.current().nextInt(SAFE_EFFECTS.size()));
        killer.addPotionEffect(new PotionEffect(type, duration, 0));
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofBoolean("kill-effect.permanent", "Permanent", false),
                ConfigurableSetting.ofInt("kill-effect.duration-seconds", "Duration (s)", 30, 5, 300, 5)
        );
    }
}

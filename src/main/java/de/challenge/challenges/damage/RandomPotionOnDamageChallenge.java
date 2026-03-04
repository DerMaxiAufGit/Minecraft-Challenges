package de.challenge.challenges.damage;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomPotionOnDamageChallenge extends Challenge {

    private static final List<PotionEffectType> SAFE_EFFECTS = List.of(
            PotionEffectType.SPEED,
            PotionEffectType.SLOWNESS,
            PotionEffectType.HASTE,
            PotionEffectType.MINING_FATIGUE,
            PotionEffectType.STRENGTH,
            PotionEffectType.JUMP_BOOST,
            PotionEffectType.NAUSEA,
            PotionEffectType.REGENERATION,
            PotionEffectType.RESISTANCE,
            PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.WATER_BREATHING,
            PotionEffectType.INVISIBILITY,
            PotionEffectType.BLINDNESS,
            PotionEffectType.NIGHT_VISION,
            PotionEffectType.HUNGER,
            PotionEffectType.WEAKNESS,
            PotionEffectType.POISON,
            PotionEffectType.GLOWING,
            PotionEffectType.LEVITATION,
            PotionEffectType.SLOW_FALLING
    );

    public RandomPotionOnDamageChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "random_potion_on_damage"; }

    @Override
    public String getDisplayName() { return "Random Potion on Damage"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.POTION); }

    @Override
    public String getDescription() { return "Taking damage gives a random potion effect"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.DAMAGE; }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!active) return;
        if (!(event.getEntity() instanceof Player player)) return;

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int minDuration = plugin.getSettingsManager().getInt("random-potion-on-damage.min-duration-seconds", 5) * 20;
        int maxDuration = plugin.getSettingsManager().getInt("random-potion-on-damage.max-duration-seconds", 30) * 20;
        int maxAmp = plugin.getSettingsManager().getInt("random-potion-on-damage.max-amplifier", 2);

        PotionEffectType type = SAFE_EFFECTS.get(random.nextInt(SAFE_EFFECTS.size()));
        int duration = random.nextInt(minDuration, maxDuration + 1);
        int amplifier = random.nextInt(maxAmp + 1);

        player.addPotionEffect(new PotionEffect(type, duration, amplifier));
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("random-potion-on-damage.min-duration-seconds", "Min Duration (s)", 5, 1, 60, 1),
                ConfigurableSetting.ofInt("random-potion-on-damage.max-duration-seconds", "Max Duration (s)", 30, 5, 120, 5),
                ConfigurableSetting.ofInt("random-potion-on-damage.max-amplifier", "Max Amplifier", 2, 0, 5, 1)
        );
    }
}

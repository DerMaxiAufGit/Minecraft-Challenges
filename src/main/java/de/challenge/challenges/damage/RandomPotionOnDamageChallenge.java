package de.challenge.challenges.damage;

import de.challenge.Challenge;
import de.challenge.ChallengePlugin;
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

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!active) return;
        if (!(event.getEntity() instanceof Player player)) return;

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int minDuration = plugin.getConfig().getInt("random-potion-on-damage.min-duration-seconds", 5) * 20;
        int maxDuration = plugin.getConfig().getInt("random-potion-on-damage.max-duration-seconds", 30) * 20;
        int maxAmp = plugin.getConfig().getInt("random-potion-on-damage.max-amplifier", 2);

        PotionEffectType type = SAFE_EFFECTS.get(random.nextInt(SAFE_EFFECTS.size()));
        int duration = random.nextInt(minDuration, maxDuration + 1);
        int amplifier = random.nextInt(maxAmp + 1);

        player.addPotionEffect(new PotionEffect(type, duration, amplifier));
    }
}

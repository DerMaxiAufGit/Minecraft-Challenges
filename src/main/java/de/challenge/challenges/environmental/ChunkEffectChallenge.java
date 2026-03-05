package de.challenge.challenges.environmental;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ChunkEffectChallenge extends Challenge {

    private static final List<PotionEffectType> EFFECTS = List.of(
            PotionEffectType.SPEED, PotionEffectType.SLOWNESS, PotionEffectType.HASTE,
            PotionEffectType.MINING_FATIGUE, PotionEffectType.STRENGTH, PotionEffectType.JUMP_BOOST,
            PotionEffectType.NAUSEA, PotionEffectType.REGENERATION, PotionEffectType.RESISTANCE,
            PotionEffectType.FIRE_RESISTANCE, PotionEffectType.WATER_BREATHING, PotionEffectType.INVISIBILITY,
            PotionEffectType.NIGHT_VISION, PotionEffectType.HUNGER, PotionEffectType.WEAKNESS,
            PotionEffectType.POISON, PotionEffectType.GLOWING, PotionEffectType.SLOW_FALLING
    );

    private final Map<Long, PotionEffectType> chunkEffects = new HashMap<>();

    public ChunkEffectChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "chunk_effect"; }
    @Override public String getDisplayName() { return "Chunk = Effect"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.BREWING_STAND); }
    @Override public String getDescription() { return "Each chunk gives a random potion effect"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.ENVIRONMENTAL; }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!active) return;
        if (event.getTo() == null) return;
        int fromCX = event.getFrom().getBlockX() >> 4;
        int fromCZ = event.getFrom().getBlockZ() >> 4;
        int toCX = event.getTo().getBlockX() >> 4;
        int toCZ = event.getTo().getBlockZ() >> 4;

        if (fromCX == toCX && fromCZ == toCZ) return;

        Player player = event.getPlayer();
        long key = ((long) toCX << 32) | (toCZ & 0xFFFFFFFFL);
        PotionEffectType effect = chunkEffects.computeIfAbsent(key,
                k -> EFFECTS.get(ThreadLocalRandom.current().nextInt(EFFECTS.size())));

        int duration = plugin.getSettingsManager().getInt("chunk-effect.duration-seconds", 30) * 20;
        int amplifier = plugin.getSettingsManager().getInt("chunk-effect.amplifier", 0);
        player.addPotionEffect(new PotionEffect(effect, duration, amplifier));
    }

    @Override
    protected void onDisable() {
        if (!preservingState) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (PotionEffectType effect : EFFECTS) {
                    player.removePotionEffect(effect);
                }
            }
        }
        chunkEffects.clear();
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("chunk-effect.duration-seconds", "Duration (s)", 30, 5, 120, 5),
                ConfigurableSetting.ofInt("chunk-effect.amplifier", "Amplifier", 0, 0, 5, 1)
        );
    }
}

package de.challenge.challenges.randomizer;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import de.challenge.util.RandomizerMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BiomeEffectChallenge extends Challenge {

    private static final List<PotionEffectType> EFFECTS = List.of(
            PotionEffectType.SPEED, PotionEffectType.SLOWNESS,
            PotionEffectType.HASTE, PotionEffectType.MINING_FATIGUE,
            PotionEffectType.STRENGTH, PotionEffectType.JUMP_BOOST,
            PotionEffectType.REGENERATION, PotionEffectType.RESISTANCE,
            PotionEffectType.FIRE_RESISTANCE, PotionEffectType.WATER_BREATHING,
            PotionEffectType.NIGHT_VISION, PotionEffectType.HUNGER,
            PotionEffectType.WEAKNESS, PotionEffectType.ABSORPTION,
            PotionEffectType.SATURATION, PotionEffectType.LUCK,
            PotionEffectType.SLOW_FALLING, PotionEffectType.CONDUIT_POWER,
            PotionEffectType.DOLPHINS_GRACE
    );

    private RandomizerMap<String, String> biomeEffectMap;
    private final Map<UUID, String> lastBiome = new HashMap<>();
    private BukkitTask checkTask;

    public BiomeEffectChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "biome_effect"; }

    @Override
    public String getDisplayName() { return "Biome Effects"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.GRASS_BLOCK); }

    @Override
    public String getDescription() { return "Each biome gives a unique potion effect"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.RANDOMIZER; }

    @Override
    protected void onEnable() {
        RandomizerMap.KeySerializer<String> keySerializer = new RandomizerMap.KeySerializer<>() {
            @Override public String serialize(String key) { return key; }
            @Override public String deserialize(String raw) { return raw; }
        };
        RandomizerMap.ValueSerializer<String> valueSerializer = new RandomizerMap.ValueSerializer<>() {
            @Override public String serialize(String value) { return value; }
            @Override public String deserialize(String raw) { return raw; }
        };

        biomeEffectMap = new RandomizerMap<>(plugin, "randomizer_biome_effects.yml", keySerializer, valueSerializer);
        biomeEffectMap.load();

        if (biomeEffectMap.isEmpty()) {
            List<String> biomeNames = new ArrayList<>();
            for (Biome biome : Registry.BIOME) {
                biomeNames.add(biome.getKey().toString());
            }
            List<String> effectNames = new ArrayList<>();
            for (int i = 0; i < biomeNames.size(); i++) {
                effectNames.add(EFFECTS.get(i % EFFECTS.size()).getKey().toString());
            }
            biomeEffectMap.generate(biomeNames, effectNames);
        }

        int checkInterval = plugin.getSettingsManager().getInt("biome-effect.check-interval-ticks", 40);
        checkTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkPlayers, 20L, checkInterval);
    }

    private void checkPlayers() {
        int duration = plugin.getSettingsManager().getInt("biome-effect.effect-duration-seconds", 10) * 20;
        int amplifier = plugin.getSettingsManager().getInt("biome-effect.amplifier", 0);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isDead()) continue;
            String currentBiome = player.getLocation().getBlock().getBiome().getKey().toString();
            String lastPlayerBiome = lastBiome.get(player.getUniqueId());

            if (!currentBiome.equals(lastPlayerBiome)) {
                lastBiome.put(player.getUniqueId(), currentBiome);

                String effectName = biomeEffectMap.get(currentBiome);
                if (effectName != null) {
                    PotionEffectType effectType = Registry.POTION_EFFECT_TYPE.get(
                            org.bukkit.NamespacedKey.fromString(effectName));
                    if (effectType != null) {
                        player.addPotionEffect(new PotionEffect(effectType, duration, amplifier, true, true));
                    }
                }
            }
        }
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("biome-effect.check-interval-ticks", "Check Interval (ticks)", 40, 10, 200, 10),
                ConfigurableSetting.ofInt("biome-effect.effect-duration-seconds", "Effect Duration (s)", 10, 3, 60, 5),
                ConfigurableSetting.ofInt("biome-effect.amplifier", "Amplifier", 0, 0, 5, 1)
        );
    }

    @Override
    protected void onDisable() {
        if (checkTask != null) {
            checkTask.cancel();
            checkTask = null;
        }
        lastBiome.clear();
        if (biomeEffectMap != null) {
            if (!preservingState) {
                biomeEffectMap.delete();
            }
            biomeEffectMap = null;
        }
    }
}

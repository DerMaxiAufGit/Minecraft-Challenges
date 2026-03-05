package de.challenge.challenges.randomizer;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomHotbarChallenge extends Challenge {

    private static final Material[] ITEMS;

    static {
        Material[] all = Material.values();
        List<Material> items = new java.util.ArrayList<>();
        for (Material m : all) {
            if (m.isItem() && !m.isAir() && !m.name().startsWith("LEGACY_")) {
                items.add(m);
            }
        }
        ITEMS = items.toArray(new Material[0]);
    }

    private BukkitTask task;

    public RandomHotbarChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "random_hotbar"; }
    @Override public String getDisplayName() { return "Random Hotbar"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.CHEST_MINECART); }
    @Override public String getDescription() { return "Your hotbar is replaced with random items periodically"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.RANDOMIZER; }

    @Override
    protected void onEnable() {
        int intervalSeconds = plugin.getSettingsManager().getInt("random-hotbar.interval-seconds", 60);
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (int i = 0; i < 9; i++) {
                    Material mat = ITEMS[ThreadLocalRandom.current().nextInt(ITEMS.length)];
                    player.getInventory().setItem(i, new ItemStack(mat));
                }
            }
        }, intervalSeconds * 20L, intervalSeconds * 20L);
    }

    @Override
    protected void onDisable() {
        if (task != null) { task.cancel(); task = null; }
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("random-hotbar.interval-seconds", "Interval (s)", 60, 10, 300, 10)
        );
    }
}

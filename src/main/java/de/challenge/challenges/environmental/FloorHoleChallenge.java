package de.challenge.challenges.environmental;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FloorHoleChallenge extends Challenge {

    private BukkitTask task;

    public FloorHoleChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "floor_hole"; }
    @Override public String getDisplayName() { return "Floor Holes"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.DARK_OAK_TRAPDOOR); }
    @Override public String getDescription() { return "Random holes appear in the floor near you"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.ENVIRONMENTAL; }

    @Override
    protected void onEnable() {
        int interval = plugin.getSettingsManager().getInt("floor-hole.interval-ticks", 40);
        int radius = plugin.getSettingsManager().getInt("floor-hole.radius", 5);
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead()) continue;
                int px = player.getLocation().getBlockX();
                int pz = player.getLocation().getBlockZ();
                int rx = px + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
                int rz = pz + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
                int y = player.getWorld().getHighestBlockYAt(rx, rz);
                Block block = player.getWorld().getBlockAt(rx, y, rz);
                if (!block.getType().isAir()) {
                    block.setType(Material.AIR);
                }
            }
        }, 20L, interval);
    }

    @Override
    protected void onDisable() {
        if (task != null) { task.cancel(); task = null; }
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("floor-hole.interval-ticks", "Interval (ticks)", 40, 10, 200, 10),
                ConfigurableSetting.ofInt("floor-hole.radius", "Radius", 5, 1, 20, 1)
        );
    }
}

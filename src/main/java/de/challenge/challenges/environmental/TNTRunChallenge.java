package de.challenge.challenges.environmental;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TNTRunChallenge extends Challenge {

    private BukkitTask task;
    private final Set<Location> scheduledBlocks = new HashSet<>();
    private final List<BukkitTask> pendingRemovals = new ArrayList<>();

    public TNTRunChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "tnt_run"; }
    @Override public String getDisplayName() { return "TNT Run"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.TNT); }
    @Override public String getDescription() { return "Blocks disappear beneath you after a delay"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.ENVIRONMENTAL; }

    @Override
    protected void onEnable() {
        int delay = plugin.getSettingsManager().getInt("tnt-run.removal-delay-ticks", 15);
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead() || !player.isOnGround()) continue;
                Block below = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
                if (below.getType().isAir()) continue;
                Location loc = below.getLocation();
                if (scheduledBlocks.contains(loc)) continue;
                scheduledBlocks.add(loc);
                Material originalType = below.getType();
                BukkitTask pending = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (!active) return;
                    if (below.getType() == originalType) {
                        below.setType(Material.AIR);
                    }
                    scheduledBlocks.remove(loc);
                }, delay);
                pendingRemovals.add(pending);
            }
        }, 0L, 5L);
    }

    @Override
    protected void onDisable() {
        if (task != null) { task.cancel(); task = null; }
        for (BukkitTask pending : pendingRemovals) {
            pending.cancel();
        }
        pendingRemovals.clear();
        scheduledBlocks.clear();
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("tnt-run.removal-delay-ticks", "Removal Delay (ticks)", 15, 5, 60, 5)
        );
    }
}

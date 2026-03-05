package de.challenge.challenges.projects;

import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FullNetheritBeaconProject extends Project {

    private BukkitTask timerTask;
    private int remainingMinutes;

    public FullNetheritBeaconProject(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "full_netherite_beacon"; }
    @Override public String getDisplayName() { return "Full Netherite Beacon"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.BEACON); }
    @Override public String getDescription() { return "Build a full netherite beacon pyramid"; }

    @Override
    public List<String> getObjectives() {
        // Single objective: build the beacon
        return List.of("FULL_NETHERITE_BEACON");
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        remainingMinutes = plugin.getSettingsManager().getInt("full-netherite-beacon.time-limit-minutes", 0);
        if (remainingMinutes > 0) {
            timerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                remainingMinutes--;
                if (remainingMinutes <= 0 && !complete) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.setHealth(0);
                    }
                }
            }, 1200L, 1200L);
        }
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        if (timerTask != null) { timerTask.cancel(); timerTask = null; }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!active || complete) return;
        if (event.getBlock().getType() != Material.BEACON) return;
        if (isFullNetheritePyramid(event.getBlock())) {
            advanceObjective(event.getPlayer());
        }
    }

    private boolean isFullNetheritePyramid(Block beacon) {
        // Check 4 layers: 3x3, 5x5, 7x7, 9x9 of netherite blocks below the beacon
        int bx = beacon.getX();
        int bz = beacon.getZ();
        int by = beacon.getY();

        for (int layer = 1; layer <= 4; layer++) {
            int size = layer * 2 + 1;
            int startX = bx - layer;
            int startZ = bz - layer;
            int y = by - layer;

            for (int x = startX; x < startX + size; x++) {
                for (int z = startZ; z < startZ + size; z++) {
                    if (beacon.getWorld().getBlockAt(x, y, z).getType() != Material.NETHERITE_BLOCK) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("full-netherite-beacon.time-limit-minutes", "Time Limit (min, 0=off)", 0, 0, 600, 30)
        );
    }
}

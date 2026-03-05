package de.challenge.challenges.modifiers;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class TimberModifier extends Challenge {

    private static final Set<Material> LOGS = Set.of(
            Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG,
            Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
            Material.MANGROVE_LOG, Material.CHERRY_LOG,
            Material.CRIMSON_STEM, Material.WARPED_STEM
    );

    private final Set<Block> breaking = new HashSet<>();

    public TimberModifier(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "timber"; }
    @Override public String getDisplayName() { return "Timber"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.OAK_LOG); }
    @Override public String getDescription() { return "Breaking a log fells the entire tree"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.MODIFIERS; }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!active) return;
        Block broken = event.getBlock();
        if (!LOGS.contains(broken.getType())) return;
        if (breaking.contains(broken)) return; // re-entrancy guard

        int maxBlocks = plugin.getSettingsManager().getInt("timber.max-blocks", 64);
        Material logType = broken.getType();

        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new ArrayDeque<>();
        queue.add(broken);
        visited.add(broken);

        while (!queue.isEmpty() && visited.size() < maxBlocks) {
            Block current = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        Block neighbor = current.getRelative(dx, dy, dz);
                        if (neighbor.getType() == logType && visited.add(neighbor)) {
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        breaking.addAll(visited);
        try {
            for (Block block : visited) {
                if (!block.equals(broken)) {
                    block.breakNaturally();
                }
            }
        } finally {
            breaking.removeAll(visited);
        }
    }

    @Override
    protected void onDisable() {
        breaking.clear();
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("timber.max-blocks", "Max Blocks", 64, 16, 256, 16)
        );
    }
}

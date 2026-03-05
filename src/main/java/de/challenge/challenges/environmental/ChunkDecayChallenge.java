package de.challenge.challenges.environmental;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class ChunkDecayChallenge extends Challenge {

    private BukkitTask task;

    public ChunkDecayChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "chunk_decay"; }
    @Override public String getDisplayName() { return "Chunk Decay"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.DEAD_BUSH); }
    @Override public String getDescription() { return "Chunks slowly lose their top layer of blocks"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.ENVIRONMENTAL; }

    @Override
    protected void onEnable() {
        int intervalSeconds = plugin.getSettingsManager().getInt("chunk-decay.interval-seconds", 30);
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead()) continue;
                Chunk chunk = player.getLocation().getChunk();
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        int y = chunk.getWorld().getHighestBlockYAt(
                                (chunk.getX() << 4) + x, (chunk.getZ() << 4) + z);
                        if (y > chunk.getWorld().getMinHeight()) {
                            chunk.getBlock(x, y, z).setType(Material.AIR);
                        }
                    }
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
                ConfigurableSetting.ofInt("chunk-decay.interval-seconds", "Interval (s)", 30, 10, 300, 10)
        );
    }
}

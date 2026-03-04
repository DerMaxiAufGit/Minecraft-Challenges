package de.challenge.challenges.misc;

import de.challenge.Challenge;
import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class DoubleSpawnChallenge extends Challenge {

    private static final String DUPLICATE_TAG = "challenge_duplicate";

    public DoubleSpawnChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "double_spawn"; }

    @Override
    public String getDisplayName() { return "Double Spawn"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.ZOMBIE_HEAD); }

    @Override
    public String getDescription() { return "Naturally spawning mobs are doubled"; }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (!active) return;
        if (event.getEntity().hasMetadata(DUPLICATE_TAG)) return;

        SpawnReason reason = event.getSpawnReason();
        if (reason != SpawnReason.NATURAL && reason != SpawnReason.SPAWNER
                && reason != SpawnReason.VILLAGE_DEFENSE && reason != SpawnReason.PATROL) {
            return;
        }

        LivingEntity original = event.getEntity();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            LivingEntity clone = (LivingEntity) original.getWorld()
                    .spawnEntity(original.getLocation(), original.getType());
            clone.setMetadata(DUPLICATE_TAG, new FixedMetadataValue(plugin, true));
        });
    }
}

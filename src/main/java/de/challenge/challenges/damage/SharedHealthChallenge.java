package de.challenge.challenges.damage;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class SharedHealthChallenge extends Challenge {

    private static final String SHARED_DAMAGE_TAG = "shared_health_damage";

    public SharedHealthChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "shared_health"; }

    @Override
    public String getDisplayName() { return "Shared Health"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.RED_DYE); }

    @Override
    public String getDescription() { return "Damage to one player is shared with all"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.DAMAGE; }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!active) return;
        if (!(event.getEntity() instanceof Player damagedPlayer)) return;
        if (damagedPlayer.hasMetadata(SHARED_DAMAGE_TAG)) return;

        double damage = event.getFinalDamage();

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other.equals(damagedPlayer)) continue;
                if (other.isDead()) continue;
                other.setMetadata(SHARED_DAMAGE_TAG, new FixedMetadataValue(plugin, true));
                other.damage(damage);
                other.removeMetadata(SHARED_DAMAGE_TAG, plugin);
            }
        });
    }
}

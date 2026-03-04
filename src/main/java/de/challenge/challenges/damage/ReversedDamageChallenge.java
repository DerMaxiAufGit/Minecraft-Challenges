package de.challenge.challenges.damage;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class ReversedDamageChallenge extends Challenge {

    public ReversedDamageChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "reversed_damage"; }

    @Override
    public String getDisplayName() { return "Reversed Damage"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.DIAMOND_SWORD); }

    @Override
    public String getDescription() { return "Damage you deal may reflect back to you"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.DAMAGE; }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!active) return;
        if (!(event.getDamager() instanceof Player player)) return;
        double chance = plugin.getConfig().getDouble("reversed-damage.reflect-chance", 0.5);
        if (Math.random() < chance) {
            // Schedule to avoid recursive damage
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isDead()) {
                    player.damage(event.getDamage());
                }
            });
        }
    }
}

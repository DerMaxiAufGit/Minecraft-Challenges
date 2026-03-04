package de.challenge.challenges.damage;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;

public class NeverFullHeartsChallenge extends Challenge {

    public NeverFullHeartsChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "never_full_hearts"; }

    @Override
    public String getDisplayName() { return "Never Full Hearts"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.GOLDEN_APPLE); }

    @Override
    public String getDescription() { return "Reaching full HP kills you"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.DAMAGE; }

    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent event) {
        if (!active) return;
        if (!(event.getEntity() instanceof Player player)) return;

        double newHealth = player.getHealth() + event.getAmount();
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        if (newHealth >= maxHealth) {
            // Schedule kill for next tick to avoid issues during heal event
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isDead()) {
                    player.damage(player.getHealth() + 1);
                }
            });
        }
    }
}

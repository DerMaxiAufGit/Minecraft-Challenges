package de.challenge.challenges.modifiers;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class NoHitDelayModifier extends Challenge {

    public NoHitDelayModifier(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "no_hit_delay"; }
    @Override public String getDisplayName() { return "No Hit Delay"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.BLAZE_ROD); }
    @Override public String getDescription() { return "Remove invincibility frames after damage"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.MODIFIERS; }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!active) return;
        if (event.getEntity() instanceof LivingEntity entity) {
            entity.setNoDamageTicks(0);
        }
    }
}

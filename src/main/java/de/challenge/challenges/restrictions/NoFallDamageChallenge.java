package de.challenge.challenges.restrictions;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class NoFallDamageChallenge extends Challenge {

    public NoFallDamageChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "no_fall_damage"; }
    @Override public String getDisplayName() { return "No Fall Damage"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.FEATHER); }
    @Override public String getDescription() { return "Any fall damage kills you instantly"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.RESTRICTIONS; }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!active) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        event.setCancelled(true);
        player.setHealth(0);
    }
}

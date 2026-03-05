package de.challenge.challenges.modifiers;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class OldPvPModifier extends Challenge {

    public OldPvPModifier(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "old_pvp"; }
    @Override public String getDisplayName() { return "Old PvP"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.STONE_SWORD); }
    @Override public String getDescription() { return "Remove attack cooldown (1.8 PvP)"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.MODIFIERS; }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!active) return;
        if (event.getDamager() instanceof Player player) {
            player.resetCooldown();
        }
    }
}

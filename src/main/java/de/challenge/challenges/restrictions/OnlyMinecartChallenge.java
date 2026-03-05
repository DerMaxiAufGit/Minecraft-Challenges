package de.challenge.challenges.restrictions;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public class OnlyMinecartChallenge extends Challenge {

    public OnlyMinecartChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "only_minecart"; }
    @Override public String getDisplayName() { return "Only Minecart"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.MINECART); }
    @Override public String getDescription() { return "You can only move while in a minecart"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.RESTRICTIONS; }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!active) return;
        Player player = event.getPlayer();
        if (player.isInsideVehicle()) return;

        double dx = event.getTo().getX() - event.getFrom().getX();
        double dz = event.getTo().getZ() - event.getFrom().getZ();
        if (dx * dx + dz * dz > 0.0001) {
            event.setCancelled(true);
        }
    }
}

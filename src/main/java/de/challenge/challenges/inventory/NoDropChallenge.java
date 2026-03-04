package de.challenge.challenges.inventory;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class NoDropChallenge extends Challenge {

    public NoDropChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "no_drop"; }

    @Override
    public String getDisplayName() { return "No Drop"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.DROPPER); }

    @Override
    public String getDescription() { return "Cannot drop items"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.INVENTORY; }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!active) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("You cannot drop items!", NamedTextColor.RED));
    }
}

package de.challenge.challenges.movement;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

public class NoSneakChallenge extends Challenge {

    public NoSneakChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "no_sneak"; }

    @Override
    public String getDisplayName() { return "No Sneak"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.LEATHER_BOOTS); }

    @Override
    public String getDescription() { return "Sneaking is disabled"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.MOVEMENT; }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!active) return;
        if (event.isSneaking()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Sneaking is disabled!", NamedTextColor.RED));
        }
    }
}

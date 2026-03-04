package de.challenge.challenges.movement;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

public class NoJumpChallenge extends Challenge {

    public NoJumpChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "no_jump"; }

    @Override
    public String getDisplayName() { return "No Jump"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.RABBIT_FOOT); }

    @Override
    public String getDescription() { return "Jumping is disabled"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.MOVEMENT; }

    @EventHandler
    public void onJump(PlayerJumpEvent event) {
        if (!active) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("Jumping is disabled!", NamedTextColor.RED));
    }
}

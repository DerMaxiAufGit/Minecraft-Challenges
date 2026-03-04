package de.challenge.challenges.restrictions;

import de.challenge.Challenge;
import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;

public class NoXPChallenge extends Challenge {

    public NoXPChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "no_xp"; }

    @Override
    public String getDisplayName() { return "No XP"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.EXPERIENCE_BOTTLE); }

    @Override
    public String getDescription() { return "XP gain is disabled"; }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent event) {
        if (!active) return;
        if (event.getAmount() > 0) {
            event.setAmount(0);
        }
    }
}

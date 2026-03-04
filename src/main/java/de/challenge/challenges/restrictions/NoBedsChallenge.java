package de.challenge.challenges.restrictions;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.inventory.ItemStack;

public class NoBedsChallenge extends Challenge {

    public NoBedsChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "no_beds"; }

    @Override
    public String getDisplayName() { return "No Beds"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.RED_BED); }

    @Override
    public String getDescription() { return "Cannot sleep in beds"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.RESTRICTIONS; }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (!active) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("Sleeping is disabled!", NamedTextColor.RED));
    }
}

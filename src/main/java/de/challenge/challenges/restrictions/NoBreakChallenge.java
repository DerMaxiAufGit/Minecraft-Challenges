package de.challenge.challenges.restrictions;

import de.challenge.Challenge;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class NoBreakChallenge extends Challenge {

    public NoBreakChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "no_break"; }

    @Override
    public String getDisplayName() { return "No Block Break"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.IRON_PICKAXE); }

    @Override
    public String getDescription() { return "Block breaking is disabled"; }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!active) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("Block breaking is disabled!", NamedTextColor.RED));
    }
}

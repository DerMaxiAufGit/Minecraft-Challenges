package de.challenge.challenges.environmental;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class EverythingReversedChallenge extends Challenge {

    public EverythingReversedChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "everything_reversed"; }
    @Override public String getDisplayName() { return "Everything Reversed"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.OBSERVER); }
    @Override public String getDescription() { return "Breaking places blocks, placing breaks blocks"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.ENVIRONMENTAL; }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!active) return;
        // Reverse: breaking a block places a random block instead
        event.setCancelled(true);
        Block block = event.getBlock();
        Material[] materials = {Material.STONE, Material.DIRT, Material.OAK_PLANKS, Material.COBBLESTONE,
                Material.SAND, Material.GRAVEL, Material.BRICKS, Material.GLASS};
        block.setType(materials[(int) (Math.random() * materials.length)]);
        event.getPlayer().sendActionBar(Component.text("Reversed!", NamedTextColor.RED));
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!active) return;
        // Reverse: placing a block breaks it instead
        event.setCancelled(true);
        Block block = event.getBlock();
        block.setType(Material.AIR);
        event.getPlayer().sendActionBar(Component.text("Reversed!", NamedTextColor.RED));
    }
}

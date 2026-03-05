package de.challenge.challenges.restrictions;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class NoLightChallenge extends Challenge {

    private static final Set<Material> LIGHT_BLOCKS = Set.of(
            Material.TORCH, Material.WALL_TORCH,
            Material.SOUL_TORCH, Material.SOUL_WALL_TORCH,
            Material.LANTERN, Material.SOUL_LANTERN,
            Material.GLOWSTONE, Material.SHROOMLIGHT,
            Material.SEA_LANTERN, Material.JACK_O_LANTERN,
            Material.CAMPFIRE, Material.SOUL_CAMPFIRE,
            Material.REDSTONE_LAMP, Material.END_ROD,
            Material.CANDLE, Material.LAVA_BUCKET
    );

    public NoLightChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "no_light"; }
    @Override public String getDisplayName() { return "No Light"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.TORCH); }
    @Override public String getDescription() { return "You cannot place light sources"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.RESTRICTIONS; }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!active) return;
        if (LIGHT_BLOCKS.contains(event.getBlock().getType())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("You cannot place light sources!", NamedTextColor.RED));
        }
    }
}

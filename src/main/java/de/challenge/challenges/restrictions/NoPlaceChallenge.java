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

public class NoPlaceChallenge extends Challenge {

    private static final Set<Material> EXEMPT = Set.of(
            Material.ENDER_EYE,
            Material.WATER_BUCKET,
            Material.LAVA_BUCKET,
            Material.FLINT_AND_STEEL
    );

    public NoPlaceChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "no_place"; }

    @Override
    public String getDisplayName() { return "No Block Place"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.BARRIER); }

    @Override
    public String getDescription() { return "Block placing is disabled"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.RESTRICTIONS; }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!active) return;
        Material hand = event.getItemInHand().getType();
        if (EXEMPT.contains(hand)) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("Block placing is disabled!", NamedTextColor.RED));
    }
}

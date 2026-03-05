package de.challenge.challenges.restrictions;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class NoFoodChallenge extends Challenge {

    public NoFoodChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "no_food"; }
    @Override public String getDisplayName() { return "No Food"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.COOKED_BEEF); }
    @Override public String getDescription() { return "You cannot eat food"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.RESTRICTIONS; }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!active) return;
        if (event.getItem().getType().isEdible()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Eating is disabled!", NamedTextColor.RED));
        }
    }
}

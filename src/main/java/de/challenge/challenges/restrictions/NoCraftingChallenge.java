package de.challenge.challenges.restrictions;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class NoCraftingChallenge extends Challenge {

    public NoCraftingChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "no_crafting"; }

    @Override
    public String getDisplayName() { return "No Crafting"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.CRAFTING_TABLE); }

    @Override
    public String getDescription() { return "Crafting is disabled"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.RESTRICTIONS; }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!active) return;
        boolean allowEye = plugin.getConfig().getBoolean("no-crafting.allow-eye-of-ender", true);
        if (allowEye && event.getRecipe().getResult().getType() == Material.ENDER_EYE) {
            return;
        }
        event.setCancelled(true);
        event.getWhoClicked().sendMessage(Component.text("Crafting is disabled!", NamedTextColor.RED));
    }
}

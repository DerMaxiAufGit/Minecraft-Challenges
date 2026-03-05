package de.challenge.challenges.modifiers;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BackpackModifier extends Challenge {

    public BackpackModifier(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "backpack"; }
    @Override public String getDisplayName() { return "Backpack"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.CHEST); }
    @Override public String getDescription() { return "Shared backpack accessible via /backpack"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.MODIFIERS; }
}

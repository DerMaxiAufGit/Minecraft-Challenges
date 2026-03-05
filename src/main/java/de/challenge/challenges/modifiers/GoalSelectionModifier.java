package de.challenge.challenges.modifiers;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GoalSelectionModifier extends Challenge {

    public GoalSelectionModifier(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "goal_selection"; }
    @Override public String getDisplayName() { return "Goal Selection"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.DRAGON_EGG); }
    @Override public String getDescription() { return "Change the win condition entity"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.MODIFIERS; }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofString("goal-selection.goal", "Goal Entity", "ENDER_DRAGON")
        );
    }
}

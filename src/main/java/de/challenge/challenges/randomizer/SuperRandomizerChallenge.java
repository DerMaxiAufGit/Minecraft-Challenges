package de.challenge.challenges.randomizer;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SuperRandomizerChallenge extends Challenge {

    public SuperRandomizerChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "super_randomizer"; }
    @Override public String getDisplayName() { return "Super Randomizer"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.NETHER_STAR); }
    @Override public String getDescription() { return "Enables all randomizer challenges at once"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.RANDOMIZER; }

    @Override
    protected void onEnable() {
        for (Challenge c : plugin.getChallengeManager().getRegisteredChallenges().values()) {
            if (c.getCategory() == ChallengeCategory.RANDOMIZER && !c.getId().equals("super_randomizer")) {
                if (!c.isActive()) {
                    plugin.getChallengeManager().enableChallenge(c.getId());
                }
            }
        }
    }

    @Override
    protected void onDisable() {
        if (preservingState) return;
        for (Challenge c : plugin.getChallengeManager().getRegisteredChallenges().values()) {
            if (c.getCategory() == ChallengeCategory.RANDOMIZER && !c.getId().equals("super_randomizer")) {
                if (c.isActive()) {
                    plugin.getChallengeManager().disableChallenge(c.getId());
                }
            }
        }
    }
}

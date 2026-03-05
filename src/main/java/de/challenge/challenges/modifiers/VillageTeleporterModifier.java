package de.challenge.challenges.modifiers;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class VillageTeleporterModifier extends Challenge {

    public VillageTeleporterModifier(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "village_teleporter"; }
    @Override public String getDisplayName() { return "Village Teleporter"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.BELL); }
    @Override public String getDescription() { return "Use /village to teleport to the nearest village"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.MODIFIERS; }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("village-teleporter.search-radius", "Search Radius", 5000, 500, 20000, 500)
        );
    }
}

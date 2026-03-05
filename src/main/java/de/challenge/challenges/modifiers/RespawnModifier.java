package de.challenge.challenges.modifiers;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import de.challenge.DeathOverride;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RespawnModifier extends Challenge implements DeathOverride {

    private int teamLivesRemaining;

    public RespawnModifier(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "respawn"; }
    @Override public String getDisplayName() { return "Respawn"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.TOTEM_OF_UNDYING); }
    @Override public String getDescription() { return "Control death behavior: one-life, team-lives, or unlimited"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.MODIFIERS; }

    @Override
    protected void onEnable() {
        teamLivesRemaining = plugin.getSettingsManager().getInt("respawn.team-lives", 3);
    }

    @Override
    public boolean allowDeath() {
        String mode = plugin.getSettingsManager().getString("respawn.mode", "one-life");
        switch (mode) {
            case "unlimited":
                return true;
            case "team-lives":
                if (teamLivesRemaining > 0) {
                    teamLivesRemaining--;
                    Bukkit.broadcast(Component.text("Team lives remaining: " + teamLivesRemaining, NamedTextColor.YELLOW));
                    return true;
                }
                return false;
            default: // one-life
                return false;
        }
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofString("respawn.mode", "Mode", "one-life"),
                ConfigurableSetting.ofInt("respawn.team-lives", "Team Lives", 3, 1, 50, 1)
        );
    }
}

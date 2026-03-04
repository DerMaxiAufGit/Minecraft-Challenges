package de.challenge.challenges.damage;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.inventory.ItemStack;

public class AchievementDamageChallenge extends Challenge {

    public AchievementDamageChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "achievement_damage"; }

    @Override
    public String getDisplayName() { return "Achievement Damage"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.KNOWLEDGE_BOOK); }

    @Override
    public String getDescription() { return "Take damage when earning advancements"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.DAMAGE; }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        if (!active) return;
        // Filter out recipe advancements
        if (event.getAdvancement().getKey().getKey().startsWith("recipes/")) return;

        double damage = plugin.getConfig().getDouble("achievement-damage.damage-amount", 1.0);
        event.getPlayer().damage(damage);
        event.getPlayer().sendMessage(
                Component.text("Advancement unlocked! -" + (damage / 2) + " hearts", NamedTextColor.RED));
    }
}

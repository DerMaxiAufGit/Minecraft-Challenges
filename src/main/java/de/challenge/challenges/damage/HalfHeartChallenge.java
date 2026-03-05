package de.challenge.challenges.damage;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class HalfHeartChallenge extends Challenge {

    public HalfHeartChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "half_heart"; }
    @Override public String getDisplayName() { return "Half Heart"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.BEETROOT); }
    @Override public String getDescription() { return "All players have only half a heart"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.DAMAGE; }

    @Override
    protected void onEnable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyHalfHeart(player);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!active) return;
        applyHalfHeart(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!active) return;
        // Delay by 1 tick so respawn health is applied first
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (active) applyHalfHeart(event.getPlayer());
        }, 1L);
    }

    private void applyHalfHeart(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(1.0);
            player.setHealth(1.0);
        }
    }

    @Override
    protected void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) {
                attr.setBaseValue(20.0);
                player.setHealth(Math.min(player.getHealth(), 20.0));
            }
        }
    }
}

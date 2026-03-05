package de.challenge.challenges.modifiers;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerGlowModifier extends Challenge {

    public PlayerGlowModifier(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "player_glow"; }
    @Override public String getDisplayName() { return "Player Glow"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.GLOWSTONE_DUST); }
    @Override public String getDescription() { return "All players glow permanently"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.MODIFIERS; }

    @Override
    protected void onEnable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyGlow(player);
        }
    }

    @Override
    protected void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.removePotionEffect(PotionEffectType.GLOWING);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!active) return;
        applyGlow(event.getPlayer());
    }

    private void applyGlow(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false));
    }
}

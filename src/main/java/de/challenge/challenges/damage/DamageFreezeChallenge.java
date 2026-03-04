package de.challenge.challenges.damage;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DamageFreezeChallenge extends Challenge {

    private final Set<UUID> frozenPlayers = new HashSet<>();

    public DamageFreezeChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "damage_freeze"; }

    @Override
    public String getDisplayName() { return "Damage Freeze"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.ICE); }

    @Override
    public String getDescription() { return "Taking damage freezes you in place"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.DAMAGE; }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!active) return;
        if (!(event.getEntity() instanceof Player player)) return;

        int freezeTicks = plugin.getConfig().getInt("damage-freeze.duration-ticks", 60);
        frozenPlayers.add(player.getUniqueId());
        player.sendActionBar(Component.text("You are frozen!", NamedTextColor.AQUA));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            frozenPlayers.remove(player.getUniqueId());
        }, freezeTicks);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!active) return;
        if (!frozenPlayers.contains(event.getPlayer().getUniqueId())) return;

        // Allow head rotation but not movement
        if (event.getFrom().getX() != event.getTo().getX()
                || event.getFrom().getY() != event.getTo().getY()
                || event.getFrom().getZ() != event.getTo().getZ()) {
            event.setCancelled(true);
        }
    }

    @Override
    protected void onDisable() {
        frozenPlayers.clear();
    }
}

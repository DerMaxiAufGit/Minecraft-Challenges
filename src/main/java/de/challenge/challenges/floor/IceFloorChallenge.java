package de.challenge.challenges.floor;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class IceFloorChallenge extends Challenge {

    private final Set<UUID> frozen = new HashSet<>();

    public IceFloorChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "ice_floor"; }

    @Override
    public String getDisplayName() { return "Ice Floor"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.PACKED_ICE); }

    @Override
    public String getDescription() { return "Ice forms under your feet (toggle with sneak)"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.FLOOR; }

    @Override
    protected void onEnable() {
        frozen.clear();
        // Everyone starts with ice active
        Bukkit.getOnlinePlayers().forEach(p -> frozen.add(p.getUniqueId()));
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!active) return;
        UUID uuid = event.getPlayer().getUniqueId();
        if (event.isSneaking()) {
            if (frozen.contains(uuid)) {
                frozen.remove(uuid);
            } else {
                frozen.add(uuid);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!active) return;
        if (!frozen.contains(event.getPlayer().getUniqueId())) return;

        // Only trigger on full block change
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Block below = event.getTo().getBlock().getRelative(BlockFace.DOWN);
        if (below.getType().isAir() || below.isLiquid()) return;
        if (below.getType() == Material.PACKED_ICE) return;

        Material original = below.getType();
        below.setType(Material.PACKED_ICE);

        int meltDelay = plugin.getSettingsManager().getInt("ice-floor.melt-delay-ticks", 100);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (below.getType() == Material.PACKED_ICE) {
                below.setType(original);
            }
        }, meltDelay);
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("ice-floor.melt-delay-ticks", "Melt Delay (ticks)", 100, 20, 600, 20)
        );
    }

    @Override
    protected void onDisable() {
        frozen.clear();
    }
}

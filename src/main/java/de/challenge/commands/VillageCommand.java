package de.challenge.commands;

import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VillageCommand implements CommandExecutor {

    private final ChallengePlugin plugin;

    public VillageCommand(ChallengePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        if (!plugin.getChallengeManager().isActive("village_teleporter")) {
            player.sendMessage(Component.text("The Village Teleporter modifier is not active!", NamedTextColor.RED));
            return true;
        }
        int radius = plugin.getSettingsManager().getInt("village-teleporter.search-radius", 5000);
        Location playerLoc = player.getLocation().clone();
        World world = player.getWorld();

        player.sendMessage(Component.text("Searching for village...", NamedTextColor.YELLOW));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Location village = world.locateNearestStructure(playerLoc, StructureType.VILLAGE, radius, false);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                if (village == null) {
                    player.sendMessage(Component.text("No village found nearby!", NamedTextColor.RED));
                    return;
                }
                int y = world.getHighestBlockYAt(village.getBlockX(), village.getBlockZ()) + 1;
                village.setY(y);
                player.teleport(village);
                player.sendMessage(Component.text("Teleported to the nearest village!", NamedTextColor.GREEN));
            });
        });
        return true;
    }
}

package de.challenge.commands;

import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ResetCommand implements CommandExecutor {

    private final ChallengePlugin plugin;
    private final Map<UUID, Long> confirmations = new HashMap<>();
    private static final long CONFIRM_TIMEOUT_MS = 15000;

    public ResetCommand(ChallengePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("confirm")) {
            confirmations.put(player.getUniqueId(), System.currentTimeMillis());
            player.sendMessage(Component.text("Are you sure you want to reset the world?", NamedTextColor.RED));
            player.sendMessage(Component.text("Type /reset confirm within 15 seconds to confirm.", NamedTextColor.YELLOW));
            return true;
        }

        Long timestamp = confirmations.remove(player.getUniqueId());
        if (timestamp == null || System.currentTimeMillis() - timestamp > CONFIRM_TIMEOUT_MS) {
            player.sendMessage(Component.text("Confirmation expired. Type /reset first.", NamedTextColor.RED));
            return true;
        }

        performReset(player);
        return true;
    }

    private void performReset(Player initiator) {
        plugin.getChallengeManager().resetAll();
        plugin.getTimerManager().reset();
        plugin.getTimerManager().hideFromAll();

        Bukkit.broadcast(Component.text("World is resetting...", NamedTextColor.RED));

        // Kick all players
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.kick(Component.text("World is being reset. Please rejoin!", NamedTextColor.YELLOW));
        }

        // Schedule world deletion/recreation on next tick
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            World world = Bukkit.getWorld("world");
            if (world != null) {
                Bukkit.unloadWorld(world, false);
                deleteDirectory(world.getWorldFolder());
            }

            World nether = Bukkit.getWorld("world_nether");
            if (nether != null) {
                Bukkit.unloadWorld(nether, false);
                deleteDirectory(nether.getWorldFolder());
            }

            World end = Bukkit.getWorld("world_the_end");
            if (end != null) {
                Bukkit.unloadWorld(end, false);
                deleteDirectory(end.getWorldFolder());
            }

            // Recreate worlds
            Bukkit.createWorld(new WorldCreator("world"));
            Bukkit.createWorld(new WorldCreator("world_nether").environment(World.Environment.NETHER));
            Bukkit.createWorld(new WorldCreator("world_the_end").environment(World.Environment.THE_END));

            // Delete randomizer data
            deleteRandomizerFiles();

            // Delete state
            File stateFile = new File(plugin.getDataFolder(), "state.yml");
            if (stateFile.exists()) {
                stateFile.delete();
            }

            plugin.getLogger().info("World reset complete.");
        }, 20L);
    }

    private void deleteRandomizerFiles() {
        String[] files = {"randomizer_blocks.yml", "randomizer_crafting.yml", "randomizer_mobs.yml"};
        for (String name : files) {
            File f = new File(plugin.getDataFolder(), name);
            if (f.exists()) {
                f.delete();
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteDirectory(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
        dir.delete();
    }
}

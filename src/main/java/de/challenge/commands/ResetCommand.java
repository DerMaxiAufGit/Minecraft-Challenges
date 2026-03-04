package de.challenge.commands;

import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

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

        Bukkit.broadcast(Component.text("World is resetting... Server will restart.", NamedTextColor.RED));

        // Delete randomizer data and state before shutdown
        deleteRandomizerFiles();
        File stateFile = new File(plugin.getDataFolder(), "state.yml");
        if (stateFile.exists()) {
            stateFile.delete();
        }

        // Write a marker file so onLoad() deletes world folders BEFORE they are loaded on next start
        File marker = new File(plugin.getDataFolder(), "reset_pending");
        plugin.getDataFolder().mkdirs();
        try {
            marker.createNewFile();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create reset marker: " + e.getMessage());
        }

        // Disable auto-save to prevent world data from being written during shutdown
        for (String worldName : List.of("world", "world_nether", "world_the_end")) {
            World w = Bukkit.getWorld(worldName);
            if (w != null) {
                w.setAutoSave(false);
            }
        }

        // Signal to onDisable() that this is a reset (skip state saving)
        plugin.setWorldFoldersToDelete(List.of(new File("reset_marker_only")));

        // Kick all players then restart the server
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.kick(Component.text("World is being reset. Please rejoin!", NamedTextColor.YELLOW));
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.getServer().shutdown(), 10L);
    }

    private void deleteRandomizerFiles() {
        String[] files = {
                "randomizer_blocks.yml", "randomizer_crafting.yml",
                "randomizer_mobs.yml", "randomizer_biome_effects.yml",
                "diet_state.yml"
        };
        for (String name : files) {
            File f = new File(plugin.getDataFolder(), name);
            if (f.exists()) {
                f.delete();
            }
        }
        // Delete project progress files
        File[] projectFiles = plugin.getDataFolder().listFiles(
                (dir, name) -> name.startsWith("project_") && name.endsWith(".yml"));
        if (projectFiles != null) {
            for (File f : projectFiles) {
                f.delete();
            }
        }
    }

}

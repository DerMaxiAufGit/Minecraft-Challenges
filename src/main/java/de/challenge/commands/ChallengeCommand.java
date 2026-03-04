package de.challenge.commands;

import de.challenge.ChallengePlugin;
import de.challenge.gui.CategoryMenuGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChallengeCommand implements CommandExecutor {

    private final ChallengePlugin plugin;

    public ChallengeCommand(ChallengePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        new CategoryMenuGUI(plugin, player).open();
        return true;
    }
}

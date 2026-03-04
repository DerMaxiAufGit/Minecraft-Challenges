package de.challenge.commands;

import de.challenge.ChallengePlugin;
import de.challenge.TimerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TimerCommand implements CommandExecutor, TabCompleter {

    private final ChallengePlugin plugin;
    private static final List<String> SUBCOMMANDS = Arrays.asList("start", "pause", "resume", "reset", "set");

    public TimerCommand(ChallengePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /timer <start|pause|resume|reset|set> [HH:MM:SS]", NamedTextColor.YELLOW));
            return true;
        }

        TimerManager timer = plugin.getTimerManager();

        switch (args[0].toLowerCase()) {
            case "start" -> {
                timer.start();
                plugin.getChallengeManager().startAll();
                sender.sendMessage(Component.text("Timer started.", NamedTextColor.GREEN));
            }
            case "pause" -> {
                timer.pause();
                sender.sendMessage(Component.text("Timer paused.", NamedTextColor.GRAY));
            }
            case "resume" -> {
                timer.resume();
                plugin.getChallengeManager().startAll();
                sender.sendMessage(Component.text("Timer resumed.", NamedTextColor.GREEN));
            }
            case "reset" -> {
                timer.reset();
                plugin.getChallengeManager().stopAll();
                sender.sendMessage(Component.text("Timer reset.", NamedTextColor.RED));
            }
            case "set" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /timer set <HH:MM:SS>", NamedTextColor.YELLOW));
                    return true;
                }
                long seconds = TimerManager.parseTime(args[1]);
                timer.setTime(seconds);
                sender.sendMessage(Component.text("Timer set to " + TimerManager.formatTime(seconds) + ".", NamedTextColor.GREEN));
            }
            default -> sender.sendMessage(Component.text("Unknown subcommand. Use: start, pause, resume, reset, set", NamedTextColor.RED));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return List.of("00:00:00");
        }
        return List.of();
    }
}

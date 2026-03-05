package de.challenge.commands;

import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class BackpackCommand implements CommandExecutor {

    private final ChallengePlugin plugin;
    private Inventory backpack;

    public BackpackCommand(ChallengePlugin plugin) {
        this.plugin = plugin;
        this.backpack = Bukkit.createInventory(null, 54, Component.text("Shared Backpack"));
        loadBackpack();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        if (!plugin.getChallengeManager().isActive("backpack")) {
            player.sendMessage(Component.text("The Backpack modifier is not active!", NamedTextColor.RED));
            return true;
        }
        player.openInventory(backpack);
        return true;
    }

    public void saveBackpack() {
        File file = new File(plugin.getDataFolder(), "backpack.yml");
        FileConfiguration config = new YamlConfiguration();
        for (int i = 0; i < backpack.getSize(); i++) {
            ItemStack item = backpack.getItem(i);
            if (item != null) {
                config.set("slot." + i, item);
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save backpack: " + e.getMessage());
        }
    }

    private void loadBackpack() {
        File file = new File(plugin.getDataFolder(), "backpack.yml");
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.getConfigurationSection("slot") == null) return;
        for (String key : config.getConfigurationSection("slot").getKeys(false)) {
            int slot;
            try {
                slot = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid slot key in backpack.yml: " + key);
                continue;
            }
            ItemStack item = config.getItemStack("slot." + key);
            if (item != null && slot >= 0 && slot < backpack.getSize()) {
                backpack.setItem(slot, item);
            }
        }
    }

    public Inventory getBackpack() {
        return backpack;
    }

    public void clearBackpack() {
        backpack.clear();
        File file = new File(plugin.getDataFolder(), "backpack.yml");
        if (file.exists()) file.delete();
    }
}

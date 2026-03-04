package de.challenge.gui;

import de.challenge.Challenge;
import de.challenge.ChallengePlugin;
import de.challenge.ChallengeSettingsManager;
import de.challenge.ConfigurableSetting;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ChallengeSettingsGUI implements Listener {

    private static final int ROWS = 6;
    private static final int SIZE = ROWS * 9;
    private static final int SETTINGS_SLOTS = 45;
    private static final int CONTROL_ROW_START = 45;

    private final ChallengePlugin plugin;
    private final Player player;
    private final Challenge challenge;
    private final List<ConfigurableSetting> settings;
    private Inventory inventory;

    public ChallengeSettingsGUI(ChallengePlugin plugin, Player player, Challenge challenge) {
        this.plugin = plugin;
        this.player = player;
        this.challenge = challenge;
        this.settings = challenge.getConfigurableSettings();
    }

    public void open() {
        String title = challenge.getDisplayName() + " Settings";
        inventory = Bukkit.createInventory(null, SIZE, Component.text(title));

        refreshItems();
        fillControlRow();

        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void refreshItems() {
        ChallengeSettingsManager sm = plugin.getSettingsManager();
        for (int i = 0; i < settings.size() && i < SETTINGS_SLOTS; i++) {
            ConfigurableSetting setting = settings.get(i);
            inventory.setItem(i, createSettingItem(setting, sm));
        }
    }

    private ItemStack createSettingItem(ConfigurableSetting setting, ChallengeSettingsManager sm) {
        ItemStack item;
        List<Component> lore = new ArrayList<>();

        switch (setting.getType()) {
            case BOOLEAN -> {
                boolean value = sm.getBoolean(setting.getConfigPath(), (boolean) setting.getDefaultValue());
                item = new ItemStack(value ? Material.LIME_DYE : Material.GRAY_DYE);
                lore.add(Component.text("Current: " + (value ? "ON" : "OFF"),
                        value ? NamedTextColor.GREEN : NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.empty());
                lore.add(Component.text("Click to toggle", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
            }
            case INT -> {
                int value = sm.getInt(setting.getConfigPath(), (int) setting.getDefaultValue());
                int step = (int) setting.getStep();
                item = new ItemStack(Material.REPEATER);
                lore.add(Component.text("Current: " + value, NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Range: " + setting.getMinValue() + " - " + setting.getMaxValue(),
                        NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.empty());
                lore.add(Component.text("Left-click: +" + step, NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Right-click: -" + step, NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Shift-click: x5 step", NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false));
            }
            case LONG -> {
                long value = sm.getLong(setting.getConfigPath(), (long) setting.getDefaultValue());
                long step = (long) setting.getStep();
                item = new ItemStack(Material.REPEATER);
                lore.add(Component.text("Current: " + value, NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Range: " + setting.getMinValue() + " - " + setting.getMaxValue(),
                        NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.empty());
                lore.add(Component.text("Left-click: +" + step, NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Right-click: -" + step, NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Shift-click: x5 step", NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false));
            }
            case DOUBLE -> {
                double value = sm.getDouble(setting.getConfigPath(), (double) setting.getDefaultValue());
                double step = (double) setting.getStep();
                item = new ItemStack(Material.COMPARATOR);
                lore.add(Component.text("Current: " + String.format("%.1f", value), NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Range: " + setting.getMinValue() + " - " + setting.getMaxValue(),
                        NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.empty());
                lore.add(Component.text("Left-click: +" + String.format("%.1f", step), NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Right-click: -" + String.format("%.1f", step), NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Shift-click: x5 step", NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false));
            }
            default -> {
                String value = sm.getString(setting.getConfigPath(), (String) setting.getDefaultValue());
                item = new ItemStack(Material.NAME_TAG);
                lore.add(Component.text("Current: " + value, NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }

        if (sm.hasCustomValue(setting.getConfigPath())) {
            lore.add(Component.empty());
            lore.add(Component.text("Middle-click to reset to default", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }

        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(setting.getDisplayName(), NamedTextColor.GOLD, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void fillControlRow() {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.displayName(Component.text(" "));
        filler.setItemMeta(fillerMeta);
        for (int i = CONTROL_ROW_START; i < SIZE; i++) {
            inventory.setItem(i, filler);
        }

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("Back", NamedTextColor.YELLOW));
        back.setItemMeta(backMeta);
        inventory.setItem(CONTROL_ROW_START, back);

        // Reset all button
        ItemStack reset = new ItemStack(Material.BARRIER);
        ItemMeta resetMeta = reset.getItemMeta();
        resetMeta.displayName(Component.text("Reset All to Defaults", NamedTextColor.RED, TextDecoration.BOLD));
        reset.setItemMeta(resetMeta);
        inventory.setItem(CONTROL_ROW_START + 4, reset);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player clicker)) return;
        if (!clicker.equals(player)) return;
        if (!event.getView().getTopInventory().equals(inventory)) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= SIZE) return;

        ChallengeSettingsManager sm = plugin.getSettingsManager();

        // Setting slot
        if (slot < SETTINGS_SLOTS && slot < settings.size()) {
            ConfigurableSetting setting = settings.get(slot);
            ClickType click = event.getClick();

            // Middle click = reset
            if (click == ClickType.MIDDLE) {
                sm.resetValue(setting.getConfigPath());
                refreshItems();
                return;
            }

            switch (setting.getType()) {
                case BOOLEAN -> {
                    boolean current = sm.getBoolean(setting.getConfigPath(), (boolean) setting.getDefaultValue());
                    sm.set(setting.getConfigPath(), !current);
                }
                case INT -> {
                    int current = sm.getInt(setting.getConfigPath(), (int) setting.getDefaultValue());
                    int step = (int) setting.getStep();
                    if (click.isShiftClick()) step *= 5;
                    int newVal;
                    if (click.isLeftClick()) {
                        newVal = Math.min((int) setting.getMaxValue(), current + step);
                    } else if (click.isRightClick()) {
                        newVal = Math.max((int) setting.getMinValue(), current - step);
                    } else {
                        return;
                    }
                    sm.set(setting.getConfigPath(), newVal);
                }
                case LONG -> {
                    long current = sm.getLong(setting.getConfigPath(), (long) setting.getDefaultValue());
                    long step = (long) setting.getStep();
                    if (click.isShiftClick()) step *= 5;
                    long newVal;
                    if (click.isLeftClick()) {
                        newVal = Math.min((long) setting.getMaxValue(), current + step);
                    } else if (click.isRightClick()) {
                        newVal = Math.max((long) setting.getMinValue(), current - step);
                    } else {
                        return;
                    }
                    sm.set(setting.getConfigPath(), newVal);
                }
                case DOUBLE -> {
                    double current = sm.getDouble(setting.getConfigPath(), (double) setting.getDefaultValue());
                    double step = (double) setting.getStep();
                    if (click.isShiftClick()) step *= 5;
                    double newVal;
                    if (click.isLeftClick()) {
                        newVal = Math.min((double) setting.getMaxValue(), current + step);
                    } else if (click.isRightClick()) {
                        newVal = Math.max((double) setting.getMinValue(), current - step);
                    } else {
                        return;
                    }
                    // Round to avoid floating point drift
                    newVal = Math.round(newVal * 100.0) / 100.0;
                    sm.set(setting.getConfigPath(), newVal);
                }
                default -> {
                    return;
                }
            }
            refreshItems();
            return;
        }

        // Back button
        if (slot == CONTROL_ROW_START) {
            clicker.closeInventory();
            new CategoryChallengeListGUI(plugin, clicker, challenge.getCategory(), 0).open();
            return;
        }

        // Reset all to defaults
        if (slot == CONTROL_ROW_START + 4) {
            for (ConfigurableSetting setting : settings) {
                sm.resetValue(setting.getConfigPath());
            }
            refreshItems();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer().equals(player) && event.getView().getTopInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);
        }
    }
}

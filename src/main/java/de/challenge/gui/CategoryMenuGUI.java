package de.challenge.gui;

import de.challenge.ChallengeCategory;
import de.challenge.ChallengeManager;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CategoryMenuGUI implements Listener {

    private static final int SIZE = 54; // 6 rows
    private static final int CONTROL_ROW_START = 45;

    // Category icons in 3 rows (4+4+3)
    private static final int[] CATEGORY_SLOTS = {
            10, 11, 12, 13,      // row 2
            19, 20, 21, 22,      // row 3
            28, 29, 30           // row 4
    };

    private final ChallengePlugin plugin;
    private final Player player;
    private Inventory inventory;

    public CategoryMenuGUI(ChallengePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        inventory = Bukkit.createInventory(null, SIZE,
                Component.text("Challenge Categories"));

        ChallengeManager manager = plugin.getChallengeManager();
        ChallengeCategory[] categories = ChallengeCategory.values();

        // Fill background
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.displayName(Component.text(" "));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, filler);
        }

        // Place category icons
        for (int i = 0; i < categories.length && i < CATEGORY_SLOTS.length; i++) {
            ChallengeCategory category = categories[i];
            long activeCount = manager.getActiveChallengeCountInCategory(category);
            long totalCount = manager.getChallengesByCategory(category).size();

            ItemStack icon = new ItemStack(category.getIcon());
            ItemMeta meta = icon.getItemMeta();
            meta.displayName(Component.text(category.getDisplayName(), NamedTextColor.GOLD, TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(category.getDescription(), NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            NamedTextColor countColor = activeCount > 0 ? NamedTextColor.GREEN : NamedTextColor.GRAY;
            lore.add(Component.text("Active: " + activeCount + "/" + totalCount, countColor)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Click to browse", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            icon.setItemMeta(meta);

            inventory.setItem(CATEGORY_SLOTS[i], icon);
        }

        // Start/Stop button
        fillControlRow();

        HandlerList.unregisterAll(this); // safe no-op if not registered yet
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void fillControlRow() {
        boolean running = plugin.getChallengeManager().isChallengeRunning();
        ItemStack toggle = new ItemStack(running ? Material.RED_DYE : Material.GREEN_DYE);
        ItemMeta toggleMeta = toggle.getItemMeta();
        if (running) {
            toggleMeta.displayName(Component.text("Stop Challenge", NamedTextColor.RED, TextDecoration.BOLD));
        } else {
            toggleMeta.displayName(Component.text("Start Challenge", NamedTextColor.GREEN, TextDecoration.BOLD));
        }
        toggle.setItemMeta(toggleMeta);
        inventory.setItem(CONTROL_ROW_START + 4, toggle);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player clicker)) return;
        if (!clicker.equals(player)) return;
        if (!event.getView().getTopInventory().equals(inventory)) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= SIZE) return;

        // Check category slots
        ChallengeCategory[] categories = ChallengeCategory.values();
        for (int i = 0; i < categories.length && i < CATEGORY_SLOTS.length; i++) {
            if (slot == CATEGORY_SLOTS[i]) {
                clicker.closeInventory();
                new CategoryChallengeListGUI(plugin, clicker, categories[i], 0).open();
                return;
            }
        }

        // Start/Stop button
        if (slot == CONTROL_ROW_START + 4) {
            ChallengeManager manager = plugin.getChallengeManager();
            if (manager.isChallengeRunning()) {
                manager.stopAll();
                plugin.getTimerManager().pause();
            } else {
                manager.startAll();
                plugin.getTimerManager().start();
            }
            clicker.closeInventory();
            new CategoryMenuGUI(plugin, clicker).open();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer().equals(player) && event.getView().getTopInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);
        }
    }
}

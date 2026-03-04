package de.challenge.gui;

import de.challenge.Challenge;
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

public class CategoryChallengeListGUI implements Listener {

    private static final int ROWS = 6;
    private static final int SIZE = ROWS * 9;
    private static final int CHALLENGE_SLOTS = 45;
    private static final int CONTROL_ROW_START = 45;

    private final ChallengePlugin plugin;
    private final Player player;
    private final ChallengeCategory category;
    private final int page;
    private Inventory inventory;
    private List<Challenge> challenges;

    public CategoryChallengeListGUI(ChallengePlugin plugin, Player player,
                                     ChallengeCategory category, int page) {
        this.plugin = plugin;
        this.player = player;
        this.category = category;
        this.page = page;
    }

    public void open() {
        ChallengeManager manager = plugin.getChallengeManager();
        challenges = manager.getChallengesByCategory(category);
        int totalPages = Math.max(1, (int) Math.ceil((double) challenges.size() / CHALLENGE_SLOTS));
        int currentPage = Math.min(page, totalPages - 1);

        String title = category.getDisplayName();
        if (totalPages > 1) {
            title += " (Page " + (currentPage + 1) + "/" + totalPages + ")";
        }
        inventory = Bukkit.createInventory(null, SIZE, Component.text(title));

        int startIndex = currentPage * CHALLENGE_SLOTS;
        int endIndex = Math.min(startIndex + CHALLENGE_SLOTS, challenges.size());

        for (int i = startIndex; i < endIndex; i++) {
            Challenge challenge = challenges.get(i);
            boolean active = manager.isActive(challenge.getId());
            inventory.setItem(i - startIndex, createIcon(challenge, active));
        }

        fillControlRow(currentPage, totalPages);

        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    private void fillControlRow(int currentPage, int totalPages) {
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
        backMeta.displayName(Component.text("Back to Categories", NamedTextColor.YELLOW));
        back.setItemMeta(backMeta);
        inventory.setItem(CONTROL_ROW_START, back);

        // Previous page
        if (currentPage > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.displayName(Component.text("Previous Page", NamedTextColor.YELLOW));
            prev.setItemMeta(prevMeta);
            inventory.setItem(CONTROL_ROW_START + 3, prev);
        }

        // Next page
        if (currentPage < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.displayName(Component.text("Next Page", NamedTextColor.YELLOW));
            next.setItemMeta(nextMeta);
            inventory.setItem(CONTROL_ROW_START + 5, next);
        }

        // Start/Stop button
        boolean running = plugin.getChallengeManager().isChallengeRunning();
        ItemStack toggle = new ItemStack(running ? Material.RED_DYE : Material.GREEN_DYE);
        ItemMeta toggleMeta = toggle.getItemMeta();
        if (running) {
            toggleMeta.displayName(Component.text("Stop Challenge", NamedTextColor.RED, TextDecoration.BOLD));
        } else {
            toggleMeta.displayName(Component.text("Start Challenge", NamedTextColor.GREEN, TextDecoration.BOLD));
        }
        toggle.setItemMeta(toggleMeta);
        inventory.setItem(CONTROL_ROW_START + 8, toggle);
    }

    private ItemStack createIcon(Challenge challenge, boolean active) {
        ItemStack icon = challenge.getIcon().clone();
        ItemMeta meta = icon.getItemMeta();

        NamedTextColor color = active ? NamedTextColor.GREEN : NamedTextColor.RED;
        String status = active ? " [ON]" : " [OFF]";
        meta.displayName(Component.text(challenge.getDisplayName() + status, color, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(challenge.getDescription(), NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Left-click to toggle", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        if (!challenge.getConfigurableSettings().isEmpty()) {
            lore.add(Component.text("Right-click for settings", NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);

        icon.setItemMeta(meta);
        return icon;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player clicker)) return;
        if (!clicker.equals(player)) return;
        if (!event.getView().getTopInventory().equals(inventory)) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= SIZE) return;

        ChallengeManager manager = plugin.getChallengeManager();
        int totalPages = Math.max(1, (int) Math.ceil((double) challenges.size() / CHALLENGE_SLOTS));

        // Challenge slot click
        if (slot < CHALLENGE_SLOTS) {
            int index = page * CHALLENGE_SLOTS + slot;
            if (index < challenges.size()) {
                Challenge challenge = challenges.get(index);
                if (event.isRightClick() && !challenge.getConfigurableSettings().isEmpty()) {
                    clicker.closeInventory();
                    new ChallengeSettingsGUI(plugin, clicker, challenge).open();
                } else {
                    manager.toggleChallenge(challenge.getId());
                    boolean active = manager.isActive(challenge.getId());
                    inventory.setItem(slot, createIcon(challenge, active));
                }
            }
            return;
        }

        // Back button
        if (slot == CONTROL_ROW_START) {
            clicker.closeInventory();
            new CategoryMenuGUI(plugin, clicker).open();
            return;
        }

        // Previous page
        if (slot == CONTROL_ROW_START + 3 && page > 0) {
            clicker.closeInventory();
            new CategoryChallengeListGUI(plugin, clicker, category, page - 1).open();
            return;
        }

        // Next page
        if (slot == CONTROL_ROW_START + 5 && page < totalPages - 1) {
            clicker.closeInventory();
            new CategoryChallengeListGUI(plugin, clicker, category, page + 1).open();
            return;
        }

        // Start/Stop button
        if (slot == CONTROL_ROW_START + 8) {
            if (manager.isChallengeRunning()) {
                manager.stopAll();
                plugin.getTimerManager().pause();
            } else {
                manager.startAll();
                plugin.getTimerManager().start();
            }
            clicker.closeInventory();
            new CategoryChallengeListGUI(plugin, clicker, category, page).open();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer().equals(player) && event.getView().getTopInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);
        }
    }
}

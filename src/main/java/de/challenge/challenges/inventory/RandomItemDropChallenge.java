package de.challenge.challenges.inventory;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomItemDropChallenge extends Challenge {

    private BukkitTask task;

    public RandomItemDropChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "random_item_drop"; }

    @Override
    public String getDisplayName() { return "Random Item Drop"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.HOPPER); }

    @Override
    public String getDescription() { return "Items randomly fall out of your inventory"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.INVENTORY; }

    @Override
    protected void onEnable() {
        int intervalTicks = plugin.getSettingsManager().getInt("random-item-drop.interval-ticks", 600);
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::dropRandomItem, intervalTicks, intervalTicks);
    }

    private void dropRandomItem() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isDead()) continue;

            ItemStack[] contents = player.getInventory().getContents();
            // Find non-empty slots
            java.util.List<Integer> filledSlots = new java.util.ArrayList<>();
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null && contents[i].getType() != Material.AIR) {
                    filledSlots.add(i);
                }
            }
            if (filledSlots.isEmpty()) continue;

            int slotIndex = filledSlots.get(ThreadLocalRandom.current().nextInt(filledSlots.size()));
            ItemStack item = contents[slotIndex];
            player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
            player.getInventory().setItem(slotIndex, null);
            player.sendMessage(Component.text("An item fell out of your inventory!", NamedTextColor.RED));
        }
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("random-item-drop.interval-ticks", "Interval (ticks)", 600, 100, 6000, 100)
        );
    }

    @Override
    protected void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}

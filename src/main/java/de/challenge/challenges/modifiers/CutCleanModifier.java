package de.challenge.challenges.modifiers;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class CutCleanModifier extends Challenge {

    private static final Map<Material, Material> ORE_SMELT = Map.of(
            Material.IRON_ORE, Material.IRON_INGOT,
            Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT,
            Material.GOLD_ORE, Material.GOLD_INGOT,
            Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT,
            Material.COPPER_ORE, Material.COPPER_INGOT,
            Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT,
            Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP
    );

    private static final Map<Material, Material> FOOD_COOK = Map.of(
            Material.BEEF, Material.COOKED_BEEF,
            Material.PORKCHOP, Material.COOKED_PORKCHOP,
            Material.CHICKEN, Material.COOKED_CHICKEN,
            Material.MUTTON, Material.COOKED_MUTTON,
            Material.RABBIT, Material.COOKED_RABBIT,
            Material.COD, Material.COOKED_COD,
            Material.SALMON, Material.COOKED_SALMON
    );

    public CutCleanModifier(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "cut_clean"; }
    @Override public String getDisplayName() { return "Cut Clean"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.IRON_INGOT); }
    @Override public String getDescription() { return "Ores drop smelted items, mobs drop cooked meat"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.MODIFIERS; }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!active) return;
        Material smelted = ORE_SMELT.get(event.getBlock().getType());
        if (smelted != null) {
            event.setDropItems(false);
            event.getBlock().getWorld().dropItemNaturally(
                    event.getBlock().getLocation(), new ItemStack(smelted));
        }
        if (event.getBlock().getType() == Material.GRAVEL) {
            event.setDropItems(false);
            event.getBlock().getWorld().dropItemNaturally(
                    event.getBlock().getLocation(), new ItemStack(Material.FLINT));
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!active) return;
        event.getDrops().replaceAll(item -> {
            Material cooked = FOOD_COOK.get(item.getType());
            if (cooked != null) {
                return new ItemStack(cooked, item.getAmount());
            }
            return item;
        });
    }
}

package de.challenge.challenges.randomizer;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.util.RandomizerMap;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlockRandomizerChallenge extends Challenge {

    private static final Set<Material> EXCLUDED = Set.of(
            Material.AIR, Material.CAVE_AIR, Material.VOID_AIR,
            Material.BEDROCK, Material.END_PORTAL_FRAME,
            Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK,
            Material.BARRIER, Material.STRUCTURE_BLOCK, Material.STRUCTURE_VOID
    );

    private RandomizerMap<Material, Material> blockMap;

    public BlockRandomizerChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "block_randomizer"; }

    @Override
    public String getDisplayName() { return "Block Randomizer"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.GRASS_BLOCK); }

    @Override
    public String getDescription() { return "Blocks drop random items"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.RANDOMIZER; }

    @Override
    protected void onEnable() {
        RandomizerMap.KeySerializer<Material> serializer = new RandomizerMap.KeySerializer<>() {
            @Override public String serialize(Material key) { return key.name(); }
            @Override public Material deserialize(String raw) { return Material.valueOf(raw); }
        };
        RandomizerMap.ValueSerializer<Material> valueSerializer = new RandomizerMap.ValueSerializer<>() {
            @Override public String serialize(Material value) { return value.name(); }
            @Override public Material deserialize(String raw) { return Material.valueOf(raw); }
        };

        blockMap = new RandomizerMap<>(plugin, "randomizer_blocks.yml", serializer, valueSerializer);
        blockMap.load();

        if (blockMap.isEmpty()) {
            List<Material> mineableBlocks = new ArrayList<>();
            List<Material> allItems = new ArrayList<>();
            for (Material mat : Material.values()) {
                if (mat.isBlock() && !EXCLUDED.contains(mat) && !mat.name().startsWith("LEGACY_")) {
                    mineableBlocks.add(mat);
                }
                if (mat.isItem() && !mat.isAir() && !mat.name().startsWith("LEGACY_")) {
                    allItems.add(mat);
                }
            }
            blockMap.generate(mineableBlocks, allItems);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!active) return;
        event.setDropItems(false);
        Material mapped = blockMap.getOrDefault(event.getBlock().getType(), Material.AIR);
        if (mapped != Material.AIR) {
            event.getBlock().getWorld().dropItemNaturally(
                    event.getBlock().getLocation(), new ItemStack(mapped));
        }
    }

    @Override
    protected void onDisable() {
        if (blockMap != null) {
            if (!preservingState) {
                blockMap.delete();
            }
            blockMap = null;
        }
    }
}

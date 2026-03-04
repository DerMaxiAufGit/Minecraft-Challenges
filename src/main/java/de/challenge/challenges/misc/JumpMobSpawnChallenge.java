package de.challenge.challenges.misc;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class JumpMobSpawnChallenge extends Challenge {

    private static final List<EntityType> HOSTILE_MOBS = List.of(
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER,
            EntityType.CREEPER, EntityType.ENDERMAN, EntityType.WITCH,
            EntityType.SLIME, EntityType.CAVE_SPIDER, EntityType.SILVERFISH,
            EntityType.DROWNED, EntityType.HUSK, EntityType.STRAY,
            EntityType.PHANTOM, EntityType.PILLAGER, EntityType.VINDICATOR
    );

    public JumpMobSpawnChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "jump_mob_spawn"; }

    @Override
    public String getDisplayName() { return "Jump = Mob Spawn"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.RABBIT_FOOT); }

    @Override
    public String getDescription() { return "Jumping spawns a random hostile mob"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.MOBS; }

    // Using Paper's PlayerJumpEvent
    @org.bukkit.event.EventHandler
    public void onJump(com.destroystokyo.paper.event.player.PlayerJumpEvent event) {
        if (!active) return;
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        EntityType mobType = HOSTILE_MOBS.get(ThreadLocalRandom.current().nextInt(HOSTILE_MOBS.size()));
        player.getWorld().spawnEntity(loc, mobType);
    }
}

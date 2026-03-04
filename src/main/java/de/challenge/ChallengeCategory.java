package de.challenge;

import org.bukkit.Material;

public enum ChallengeCategory {

    RESTRICTIONS("restrictions", "Restrictions", Material.BARRIER,
            "Challenges that restrict what you can do"),
    MOVEMENT("movement", "Movement", Material.LEATHER_BOOTS,
            "Challenges that affect how you move"),
    DAMAGE("damage", "Damage", Material.DIAMOND_SWORD,
            "Challenges that change how damage works"),
    FLOOR("floor", "Floor", Material.MAGMA_BLOCK,
            "Challenges that change the ground beneath you"),
    INVENTORY("inventory", "Inventory", Material.CHEST,
            "Challenges that affect your inventory"),
    MOBS("mobs", "Mobs", Material.ZOMBIE_HEAD,
            "Challenges involving mobs"),
    RANDOMIZER("randomizer", "Randomizer", Material.COMMAND_BLOCK,
            "Challenges that randomize game mechanics"),
    FORCE("force", "Force", Material.CLOCK,
            "Timed objectives you must complete"),
    ENVIRONMENTAL("environmental", "Environmental", Material.LIGHTNING_ROD,
            "Challenges that change the world around you"),
    PROJECTS("projects", "Projects", Material.NETHER_STAR,
            "Long-term collection projects");

    private final String id;
    private final String displayName;
    private final Material icon;
    private final String description;

    ChallengeCategory(String id, String displayName, Material icon, String description) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getIcon() { return icon; }
    public String getDescription() { return description; }
}

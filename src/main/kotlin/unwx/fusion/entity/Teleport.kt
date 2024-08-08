package unwx.fusion.entity

data class Teleport(
    /**
     * The radius (in blocks) within which entities and blocks are affected during teleportation.
     */
    val forceRadius: Int,

    /**
     * Indicates whether fire is extinguished within the force radius during teleportation.
     */
    val extinguishesFire: Boolean,

    /**
     * The force strength applied to entities within the force radius during teleportation.
     */
    val forceStrength: Float,

    /**
     * The minimum cooldown time (in ticks) between teleportations for a player.
     */
    val teleportCooldown: Int
)

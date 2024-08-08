package unwx.fusion.entity

data class Level(
    /**
     * The unique identifier of this fusion level.
     */
    val id: Int,

    /**
     * The next level in the progression, or `null` if this is the highest level.
     */
    val nextLevel: Level?,

    /**
     * The maximum distance (in blocks) allowed between players for them to be considered connected.
     */
    val connectionRange: Int,

    /**
     * Whether incoming damage is distributed among connected players.
     */
    val isDamageDistributed: Boolean,

    /**
     * Whether incoming experience points (XP) are distributed among connected players.
     */
    val isXpDistributed: Boolean,

    /**
     * Whether incoming potion effects are distributed among connected players.
     */
    val arePotionEffectsDistributed: Boolean,

    /**
     * Whether health is passively transferred between connected players.
     */
    val isHealthPassivelyDistributed: Boolean,

    /**
     * Whether food levels are passively transferred between connected players.
     */
    val isFoodPassivelyDistributed: Boolean,

    /**
     * Whether players can teleport to each other.
     */
    val isTeleportEnabled: Boolean,

    /**
     * The percentage of incoming damage to distribute among connected players (0.0 to 1.0).
     */
    val damageDistributionPercentage: Float,

    /**
     * The percentage of incoming XP to distribute among connected players (0.0 to 1.0).
     */
    val xpDistributionPercentage: Float,

    /**
     * The amount of health transferred per second between connected players.
     */
    val healthTransferPerSecond: Float,

    /**
     * The amount of food level transferred per second between connected players.
     */
    val foodTransferPerSecond: Float,

    /**
     * The time (in ticks) required for connected players to reach the next level, or `null` if this is the highest level.
     */
    val timeToNextLevel: Int?,

    /**
     * Details about teleport functionality if enabled, or `null` if disabled.
     */
    val teleport: Teleport?
)

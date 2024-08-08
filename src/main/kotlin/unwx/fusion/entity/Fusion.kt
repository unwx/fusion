package unwx.fusion.entity

import org.bukkit.entity.Player

interface Fusion {
    /**
     * The unique name of this fusion.
     */
    val name: String

    /**
     * The current level of this fusion.
     *
     * @see Level
     */
    var level: Level

    /**
     * The remaining time (in ticks) until this fusion levels up.
     */
    var timeToNextLevelLeft: Int


    /**
     * Iterates over all active players who are:
     *  - Online
     *  - Alive
     *  - In survival or adventure mode
     *
     * @param action The function to apply to each active player.
     */
    fun forEachActivePlayer(action: (ActivePlayer) -> Unit)

    /**
     * Retrieves the `ActivePlayer` object for the given `player`, if they are active in the fusion.
     *
     * @return The `ActivePlayer` object, or null if the player is not active.
     */
    fun getActivePlayer(player: Player): ActivePlayer?

    /**
     * Checks if the given `player` is currently active in the fusion.
     *
     * @return `true` if the player is active, `false` otherwise.
     */
    fun isActivePlayer(player: Player): Boolean

    /**
     * Adds the given `player` as an active player to the fusion.
     */
    fun addActivePlayer(player: ActivePlayer)

    /**
     * Removes the given `player` from the list of active players in the fusion.
     */
    fun removeActivePlayer(player: Player)


    /**
     * Retrieves the partner of the given `player`, if they are connected to another player in the fusion.
     *
     * @return The partner's `ActivePlayer` object, or null if the player is not connected.
     */
    fun getPartner(player: Player): ActivePlayer?

    /**
     * Checks if the given `player` is connected to another player within the fusion.
     * Two players are considered connected if:
     *  - They are both active players
     *  - They are in the same world
     *  - They are the closest to each other among all other potential pairs
     *
     * @return `true` if the player is connected, `false` otherwise.
     */
    fun isConnected(player: Player): Boolean

    /**
     * Iterates over all current player connections within the fusion.
     *
     * @param action The function to apply to each connection.
     */
    fun forEachConnection(action: (Connection) -> Unit)

    /**
     * Adds a new connection between two players if they meet the connection criteria.
     */
    fun addConnection(connection: Connection)

    /**
     * Removes any existing connection involving the given `player`.
     */
    fun removeConnection(player: Player)
}

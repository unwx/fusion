package unwx.fusion.listener

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import unwx.fusion.FusionDatabase
import unwx.fusion.entity.*
import unwx.fusion.listener.event.PlayerIsActiveEvent
import unwx.fusion.listener.event.PlayerIsNotActiveEvent
import unwx.fusion.util.callEvent

class ActivePlayerUpdater(private val database: FusionDatabase) : Listener {
    /*
     * We use LOW priority for events that we might cancel/modify.
     * We use HIGH priority when we only need to react to events that haven't been canceled.
     * The priority system here might seem counterintuitive.
     *
     * However, if an event is canceled *after* our plugin has processed it,
     * it could lead to inconsistencies in our plugin's state.
     *
     * While MONITOR priority could ensure that the event isn't canceled after our processing, it's intended for:
     *  - "Purely monitoring the outcome of an event."
     *  - "Making no modifications to the event."
     *
     * Since we're not just monitoring but also changing the game state (players, etc.),
     * we likely cannot use MONITOR priority.
     *
     * We leave the possibility for other plugins to use LOWEST & HIGHEST priorities if they find it necessary.
     */


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        handleConditionChange(event.player, online = false)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        handleConditionChange(event.player, online = true)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        handleConditionChange(event.entity, dead = true)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerRespawn(event: PlayerPostRespawnEvent) {
        handleConditionChange(event.player, dead = false)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerGameModeChange(event: PlayerGameModeChangeEvent) {
        handleConditionChange(event.player, gameMode = event.newGameMode)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        val player = event.player
        val fusion = database.findBy(player) ?: return
        val entity = fusion.getEntity(player) ?: return

        val world = player.world
        entity.removeType(WorldMarker::class.java)
        entity.add(getWorldMarker(world))
    }


    @Suppress("NAME_SHADOWING")
    private fun handleConditionChange(
        player: Player,
        online: Boolean = player.isConnected,
        dead: Boolean = player.isDead,
        gameMode: GameMode = player.gameMode
    ) {
        val fusion = database.findBy(player) ?: return
        val entity = fusion.getEntity(player)

        if (online && !dead && isAllowedGameMode(gameMode)) {
            if (entity == null) {
                val entity = fusion.addEntity(player, getWorldMarker(player.world))
                callEvent(PlayerIsActiveEvent(player, entity, fusion))
            }
        } else {
            if (entity != null) {
                callEvent(PlayerIsNotActiveEvent(player, entity, fusion))
                fusion.removeEntity(player)
            }
        }
    }

    private fun getWorldMarker(world: World): WorldMarker = when (world.environment) {
        World.Environment.NORMAL -> NormalWorld()
        World.Environment.NETHER -> NetherWorld()
        World.Environment.THE_END -> EndWorld()
        else -> OtherWorld()
    }

    private fun isAllowedGameMode(mode: GameMode) = when (mode) {
        GameMode.SURVIVAL, GameMode.ADVENTURE -> true
        else -> false
    }
}

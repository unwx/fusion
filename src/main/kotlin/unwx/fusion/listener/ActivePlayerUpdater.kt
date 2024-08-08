@file:Suppress("NOTHING_TO_INLINE")

package unwx.fusion.listener

import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import unwx.fusion.FusionDatabase
import unwx.fusion.entity.OtherWorld
import unwx.fusion.entity.Overworld
import unwx.fusion.entity.WorldDelegate
import unwx.fusion.listener.event.PlayerIsActiveEvent
import unwx.fusion.listener.event.PlayerIsNotActiveEvent
import unwx.fusion.listener.event.PlayerWorldChangeEvent
import unwx.fusion.util.callEvent
import java.util.*
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet as HashSet

class ActivePlayerUpdater(private val database: FusionDatabase) : Listener {
    private val activePlayers = HashSet<UUID>()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        handleConditionChange(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        handleConditionChange(event.player)
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        handleConditionChange(event.entity, dead = true)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        handleConditionChange(event.player, dead = false)
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerGameModeChange(event: PlayerGameModeChangeEvent) {
        handleConditionChange(event.player, gameMode = event.newGameMode)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        val player =event.player
        val fusion = database.findBy(player) ?: return

        fusion.getEntity(player)?.let {
            it.removeType(WorldDelegate::class.java)
            val world = player.world

            when (world.environment) {
                World.Environment.NORMAL -> it.add(Overworld(world))
                else -> it.add(OtherWorld(world))
            }

            callEvent(PlayerWorldChangeEvent(player, event.from, world, fusion))
        }
    }


    private fun handleConditionChange(
        player: Player,
        dead: Boolean = player.isDead,
        gameMode: GameMode = player.gameMode
    ) {
        val fusion = database.findBy(player) ?: return
        val isPlayerActive = activePlayers.contains(player.uniqueId)

        if (player.isConnected && !dead && isAllowedGameMode(gameMode)) {
            if (!isPlayerActive) {
                fusion.addEntity(player)
                callEvent(PlayerIsActiveEvent(player, fusion))
            }
        } else {
            if (isPlayerActive) {
                fusion.removeEntity(player)
                callEvent(PlayerIsNotActiveEvent(player, fusion))
            }
        }
    }

    private inline fun isAllowedGameMode(mode: GameMode) = when (mode) {
        GameMode.SURVIVAL, GameMode.ADVENTURE -> true
        else -> false
    }
}

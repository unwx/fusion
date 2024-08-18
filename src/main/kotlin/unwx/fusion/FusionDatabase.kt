package unwx.fusion

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import unwx.fusion.entity.Fusion
import unwx.fusion.entity.FusionImpl
import unwx.fusion.listener.event.TeamJoinEvent
import unwx.fusion.listener.event.TeamLeaveEvent
import unwx.fusion.util.callEvent
import unwx.fusion.util.getTeam
import java.util.*
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap as HashMap

class FusionDatabase(private val levelManager: LevelManager) : Listener {
    companion object {
        private val syncTeamsWithFusionsCommandRegex =
            "^/(team|party).* (join |leave |add |create |remove |delete |disband ).*$".toRegex()
    }

    private val playerToFusion = HashMap<UUID, Fusion>()
    private val teamToFusion = HashMap<String, Fusion>()

    fun findBy(player: Player): Fusion? = playerToFusion[player.uniqueId]

    fun forEach(action: (Fusion) -> Unit) = teamToFusion.values.forEach(action)

    fun sync() {
        val teams = Bukkit.getScoreboardManager().mainScoreboard.teams.map { it.name }
        removeNonexistentFusions(teams)
        addNewFusions(teams)
        syncPlayersWithFusions()
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerWriteCommand(event: PlayerCommandPreprocessEvent) {
        /*
         * There are no Team events in Purpur API,
         * so we have to somehow adapt to the situation.
         *
         * In any case, I think it's better than making
         * own Team system apart from the built-in one.
         */
        if (event.message.matches(syncTeamsWithFusionsCommandRegex)) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin(), ::sync, 1)
        }
    }

    private fun removeNonexistentFusions(teams: Iterable<String>) {
        val iterator = teamToFusion.keys.iterator()
        while (iterator.hasNext()) {
            if (!teams.contains(iterator.next())) iterator.remove()
        }
    }

    private fun addNewFusions(teams: Iterable<String>) {
        for (team in teams) {
            if (!teamToFusion.containsKey(team)) {
                teamToFusion[team] = FusionImpl(levelManager.getFirst())
            }
        }
    }

    private fun syncPlayersWithFusions() {
        for (player in Bukkit.getOnlinePlayers()) {
            val playerId = player.uniqueId
            val newTeam = player.getTeam()?.name

            val oldFusion = playerToFusion[playerId]
            val newFusion = newTeam?.let { teamToFusion[it] }
            if (oldFusion == newFusion) continue

            oldFusion?.let {
                callEvent(TeamLeaveEvent(player, it))
                playerToFusion.remove(playerId)
            }
            newFusion?.let {
                playerToFusion[playerId] = it
                callEvent(TeamJoinEvent(player, it))
            }
        }
    }
}

package unwx.fusion.listener

import org.bukkit.Bukkit.getCurrentTick
import org.bukkit.Bukkit.getScheduler
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitTask
import unwx.fusion.entity.Fusion
import unwx.fusion.listener.event.ConnectedEvent
import unwx.fusion.listener.event.DisconnectedEvent
import unwx.fusion.plugin
import unwx.fusion.util.Logger.warn
import unwx.fusion.util.sound.RandomSound
import unwx.fusion.util.sound.RandomSounds
import unwx.fusion.util.sound.Sounds.HIGHEST
import unwx.fusion.util.sound.Sounds.NORMAL
import unwx.fusion.util.sound.Sounds.playAt
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap as HashMap

class LevelUpdater : Listener {
    private val fusionToTask = HashMap<Fusion, Task>()
    private val fusionLevelUpSounds = RandomSounds(
        2,
        3,
        RandomSound(
            NORMAL,
            HIGHEST,
            Sound.BLOCK_NOTE_BLOCK_BELL,
            Sound.BLOCK_NOTE_BLOCK_CHIME
        )
    )

    @EventHandler
    fun onConnect(event: ConnectedEvent) {
        val fusion = event.fusion
        if (event.connectionsCount > 1 || fusion.level.next == null) return

        if (fusionToTask.containsKey(fusion)) {
            warn { "Connections count for fusion '$fusion' is '${event.connectionsCount}', but levelUp() task is already scheduled" }
            fusionToTask.remove(fusion)
        }

        val timeToLevelUp = fusion.timeToNextLevelLeft.toLong()
        if (timeToLevelUp > 0) scheduleLevelUp(fusion, timeToLevelUp)
        else levelUp(fusion)
    }

    @EventHandler
    fun onDisconnect(event: DisconnectedEvent) {
        if (event.connectionsCount > 0) return
        val fusion = event.fusion

        val task = fusionToTask.remove(fusion) ?: return
        val timeBeingConnected = (getCurrentTick() - task.startedAt).coerceAtLeast(0)

        fusion.timeToNextLevelLeft = (fusion.timeToNextLevelLeft - timeBeingConnected).coerceAtLeast(0)
        task.bukkit.cancel()
    }

    private fun levelUp(fusion: Fusion) {
        fusion.level = fusion.level.next!!

        if (fusion.level.next != null) {
            fusion.timeToNextLevelLeft = fusion.level.timeToNextLevel!!
            scheduleLevelUp(fusion, fusion.timeToNextLevelLeft.toLong())
        }

        fusion.getComponent(Player::class).iterator().forEach { fusionLevelUpSounds.sounds().playAt(it) }
    }

    private fun scheduleLevelUp(
        fusion: Fusion,
        delay: Long
    ) {
        fusionToTask[fusion] = Task(
            getScheduler().runTaskLater(plugin(), Runnable { levelUp(fusion) }, delay),
            getCurrentTick()
        )
    }

    private class Task(
        val bukkit: BukkitTask,
        val startedAt: Int
    )
}

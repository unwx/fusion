package unwx.fusion.listener

import dev.dominion.ecs.api.Entity
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import unwx.fusion.entity.Fusion
import unwx.fusion.entity.OtherWorld
import unwx.fusion.entity.Partner
import unwx.fusion.entity.WorldMarker
import unwx.fusion.listener.event.PlayerChangedWorldEvent
import unwx.fusion.listener.event.PlayerIsActiveEvent
import unwx.fusion.listener.event.PlayerIsNotActiveEvent
import unwx.fusion.listener.event.PlayerMoveEvent
import unwx.fusion.util.distanceSquared
import unwx.fusion.util.getPartner
import unwx.fusion.util.getPlayer
import unwx.fusion.util.particle.LinearPainter
import unwx.fusion.util.sound.RandomSound
import unwx.fusion.util.sound.Sounds.HIGH
import unwx.fusion.util.sound.Sounds.HIGHEST
import unwx.fusion.util.sound.Sounds.LOWER
import unwx.fusion.util.sound.Sounds.LOWEST
import unwx.fusion.util.sound.Sounds.playAt
import java.util.*

class ConnectionUpdater : Listener {
    // TODO on a large server sounds can be annoying, solution? Reduce volume?
    private val fusionConnectSound = RandomSound(
        HIGH,
        HIGHEST,
        Sound.BLOCK_NOTE_BLOCK_BELL,
        Sound.BLOCK_NOTE_BLOCK_CHIME
    )
    private val fusionDisconnectSound = RandomSound(
        LOWEST,
        LOWER,
        Sound.BLOCK_NOTE_BLOCK_BELL,
        Sound.BLOCK_NOTE_BLOCK_CHIME
    )
    private val fusionConnectPainter = LinearPainter(
        Particle.HEART,
        15,
        2.5,
    )


    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        searchBestPartnerFor(event.entity, event.fusion)
    }

    @EventHandler
    fun onPlayerIsActive(event: PlayerIsActiveEvent) {
        searchBestPartnerFor(event.entity, event.fusion)
    }

    @EventHandler
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        // The world is already changed in the player's fields
        searchBestPartnerFor(event.entity, event.fusion)
    }

    @EventHandler
    fun onPlayerIsNotActive(event: PlayerIsNotActiveEvent) {
        removeConnection(event.entity, event.fusion)
    }

    private fun searchBestPartnerFor(
        entity1: Entity,
        fusion: Fusion,
        exceptionId: UUID? = null
    ) {
        val player1 = entity1.getPlayer()!!
        var bestPartner: Entity? = null
        var smallestDistance: Double? = null

        val iterator = run {
            val marker = entity1.get(WorldMarker::class.java)!!
            val getComponents = { fusion.getComponents(marker::class, true) }

            if (marker is OtherWorld) {
                getComponents()
                    .stream()
                    .filter { it.comp1.world.uid == player1.world.uid }
                    .iterator()
            } else {
                getComponents().iterator()
            }
        }

        for (components in iterator) {
            if (components.comp1.uniqueId == exceptionId) continue

            // TODO should I implement something like Octree? Or use parallel processing?
            val distance = player1.distanceSquared(components.comp1)
            if (distance > fusion.level.connectionRange || (smallestDistance != null && distance >= smallestDistance)) continue

            val player2PartnerDistance = components.entity.getPartner()?.let { components.comp1.distanceSquared(it) }
            if (player2PartnerDistance != null && distance >= player2PartnerDistance) continue

            smallestDistance = distance
            bestPartner = components.entity
        }

        if (bestPartner != null) addConnection(entity1, bestPartner, fusion)
        else removeConnection(entity1, fusion)
    }

    private fun addConnection(
        entity1: Entity,
        entity2: Entity,
        fusion: Fusion,
    ) {
        val player1 = entity1.getPlayer()!!
        val player2 = entity2.getPlayer()!!

        run {
            val partner1 = entity1.getPartner()
            if (partner1 != null && partner1.uniqueId == player2.uniqueId) return
        }

        removeConnection(entity1, fusion)
        removeConnection(entity2, fusion)

        entity1.add(Partner(player2))
        entity2.add(Partner(player1))

        fusionConnectSound.sound().playAt(player1.location, player2.location)
        fusionConnectPainter.drawLine(player1.location, player2.location)
    }

    private fun removeConnection(
        entity1: Entity,
        fusion: Fusion
    ) {
        val player2 = entity1.getPartner() ?: return
        val entity2 = fusion.getEntity(player2.uniqueId)!!
        val player1 = entity1.getPlayer()!!

        entity1.removeType(Partner::class.java)
        entity2.removeType(Partner::class.java)

        fusionDisconnectSound.sound().playAt(player1.location, player2.location)
        searchBestPartnerFor(entity2, fusion, player1.uniqueId)
    }
}

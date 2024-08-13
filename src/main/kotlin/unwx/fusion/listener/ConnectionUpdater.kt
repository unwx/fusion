package unwx.fusion.listener

import dev.dominion.ecs.api.Entity
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerMoveEvent
import unwx.fusion.FusionDatabase
import unwx.fusion.entity.*
import unwx.fusion.listener.event.ConnectedEvent
import unwx.fusion.listener.event.DisconnectedEvent
import unwx.fusion.listener.event.PlayerIsActiveEvent
import unwx.fusion.listener.event.PlayerIsNotActiveEvent
import unwx.fusion.util.*
import unwx.fusion.util.particle.LinearPainter
import unwx.fusion.util.sound.RandomSound
import unwx.fusion.util.sound.Sounds.HIGH
import unwx.fusion.util.sound.Sounds.HIGHEST
import unwx.fusion.util.sound.Sounds.LOWER
import unwx.fusion.util.sound.Sounds.LOWEST
import unwx.fusion.util.sound.Sounds.playAt
import java.util.*
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap as IntHashMap

class ConnectionUpdater(private val database: FusionDatabase) : Listener {
    private val connectionsCountMap = IntHashMap<Fusion>()

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


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerMove(event: PlayerMoveEvent) {
        val fusion = database.findBy(event.player) ?: return
        val entity = fusion.getEntity(event.player) ?: return
        searchBestPartnerFor(entity, fusion, location1 = event.to)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        val fusion = database.findBy(event.player) ?: return
        val entity = fusion.getEntity(event.player) ?: return

        // The world is already changed in the player's fields
        searchBestPartnerFor(entity, fusion)
    }

    @EventHandler
    fun onPlayerIsActive(event: PlayerIsActiveEvent) {
        searchBestPartnerFor(event.entity, event.fusion)
    }

    @EventHandler
    fun onPlayerIsNotActive(event: PlayerIsNotActiveEvent) {
        removeConnection(event.entity, event.fusion)
    }


    @Suppress("NAME_SHADOWING")
    private fun searchBestPartnerFor(
        entity1: Entity,
        fusion: Fusion,
        location1: Location? = null,
        exceptionId: UUID? = null
    ) {
        val player1 = entity1.getPlayer()!!
        val location1 = location1 ?: player1.location
        var bestPartner: Entity? = null
        var smallestDistance: Double? = null

        val iterator = run {
            val marker = entity1.get(WorldMarker::class.java)!!
            val getComponents = { fusion.getComponents(Player::class, marker::class, true) }

            if (marker is OtherWorld) {
                getComponents()
                    .stream()
                    .filter { it.comp1.world.uid == location1.world.uid }
                    .iterator()
            } else {
                getComponents().iterator()
            }
        }

        for (components in iterator) {
            if (components.comp1.uniqueId in arrayOf(exceptionId, player1.uniqueId)) continue

            // TODO should I implement something like Octree? Or use parallel processing?
            val distance = location1.distanceSquared(components.comp1.location)
            if (distance > fusion.level.connectionRange || (smallestDistance != null && distance >= smallestDistance)) continue

            val player2PartnerDistance = components.entity.getPartner()?.let {
                if (it.uniqueId == player1.uniqueId) components.comp1.location.distanceSquared(location1)
                else components.comp1.distanceSquared(it)
            }
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

        val passiveBuffer = PassiveDistributionBuffer()
        val connection1 = Connection(player1, player2, passiveBuffer)
        val connection2 = Connection(player2, player1, passiveBuffer)
        entity1.add(connection1)
        entity2.add(connection2)

        fusionConnectSound.sound().playAt(player1.location, player2.location)
        fusionConnectPainter.drawLine(player1.location, player2.location)

        val connectionsCount = connectionsCountMap.addTo(fusion, 1) + 1
        callEvent(ConnectedEvent(connection1, connectionsCount, fusion))
    }

    private fun removeConnection(
        entity1: Entity,
        fusion: Fusion
    ) {
        val connection = entity1.getConnection() ?: return
        val player2 = connection.player2
        val entity2 = fusion.getEntity(player2.uniqueId)!!
        val player1 = entity1.getPlayer()!!

        entity1.removeType(Connection::class.java)
        entity2.removeType(Connection::class.java)

        fusionDisconnectSound.sound().playAt(player1, player2)
        searchBestPartnerFor(entity2, fusion, exceptionId = player1.uniqueId)

        val connectionsCount = connectionsCountMap.addTo(fusion, -1) -1
        if (connectionsCount <= 0) connectionsCountMap.removeInt(fusion)
        callEvent(DisconnectedEvent(connection, connectionsCount, fusion))
    }
}

package unwx.fusion.listener

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import org.bukkit.Particle.GLOW
import org.bukkit.Particle.TOTEM
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.potion.PotionEffect
import unwx.fusion.FusionDatabase
import unwx.fusion.entity.Connection
import unwx.fusion.entity.Fusion
import unwx.fusion.entity.PassiveDistributionBuffer
import unwx.fusion.listener.event.ConnectedEvent
import unwx.fusion.listener.event.DisconnectedEvent
import unwx.fusion.util.PotionEffects.isDistributed
import unwx.fusion.util.PotionEffects.toDistributed
import unwx.fusion.util.TPS
import unwx.fusion.util.particle.LinearPainter
import java.util.*
import kotlin.math.abs

class PassiveDistributor(private val database: FusionDatabase) : EventListener {
    private val distributeHealthPainter = LinearPainter(
        GLOW,
        11,
        2.8,
        0.2
    )
    private val distributeFoodPainter = LinearPainter(
        TOTEM,
        9,
        3.0,
        0.2
    )

    @EventHandler
    fun onTick(event: ServerTickStartEvent) {
        database.forEach(::distributeFusion)
    }

    @EventHandler
    fun onConnect(event: ConnectedEvent) {
        if (!event.fusion.level.arePotionEffectsDistributed) return
        distributePotionEffects(event.connection)
    }

    @EventHandler
    fun onDisconnect(event: DisconnectedEvent) {
        event.connection.forEach(::removeDistributedPotionEffects)
    }

    private fun distributeFusion(fusion: Fusion) {
        val iterator = fusion.getComponent(Connection::class).iterator()
        iterator.forEach { connection ->
            if (connection.player1.uniqueId >= connection.player2.uniqueId) return@forEach
            val level = fusion.level

            if (level.isHealthPassivelyDistributed) {
                var distributed = distribute(
                    connection,
                    level.healthTransferPerSecond,
                    { it.health },
                    { player, value -> player.health = value },
                    { it.health },
                    { buffer, value -> buffer.health = value }
                )
                distributed = distribute(
                    connection,
                    level.healthTransferPerSecond,
                    { it.absorptionAmount },
                    { player, value -> player.absorptionAmount = value },
                    { it.health },
                    { buffer, value -> buffer.health = value },
                    false
                ) || distributed

                if (distributed) distributeHealthPainter.drawLine(connection.player1, connection.player2)
            }
            if (level.isFoodPassivelyDistributed) {
                val distributed = distribute(
                    connection,
                    level.foodTransferPerSecond,
                    { it.foodLevel.toDouble() },
                    { player, value -> player.foodLevel = value.toInt() },
                    { it.food },
                    { buffer, value -> buffer.food = value }
                )
                if (distributed) distributeFoodPainter.drawLine(connection.player1, connection.player2)
            }
        }
    }

    private fun distributePotionEffects(connection: Connection) {
        val player1 = connection.player1
        val player2 = connection.player2

        player1.addPotionEffects(player2.activePotionEffects.toDistributed())
        player2.addPotionEffects(player1.activePotionEffects.toDistributed())
    }

    private fun removeDistributedPotionEffects(player: Player) {
        val effects = player.activePotionEffects
        if (!player.clearActivePotionEffects()) return

        effects.forEach {
            var current: PotionEffect? = it
            while (current != null) {
                if (!current.isDistributed()) player.addPotionEffect(current)
                current = current.hiddenPotionEffect
            }
        }
    }

    private fun distribute(
        connection: Connection,
        ratePerSecond: Float,
        getValue: (Player) -> Double,
        setValue: (Player, Double) -> Unit,
        getBuffer: (PassiveDistributionBuffer) -> Float,
        setBuffer: (PassiveDistributionBuffer, Float) -> Unit,
        incrementBuffer: Boolean = true
    ): Boolean {
        val value1 = getValue(connection.player1)
        val value2 = getValue(connection.player2)
        val diff = abs(value1 - value2).toInt().let { it - it % 2 }
        val buffer = getBuffer(connection.passiveBuffer)

        if (diff < 2) {
            if (incrementBuffer && buffer < ratePerSecond) setBuffer(
                connection.passiveBuffer,
                tickBuffer(buffer, ratePerSecond).coerceAtMost(ratePerSecond)
            )
            return false
        }

        val distribution = (buffer.toInt().let { it - it % 2 }.coerceAtMost(diff))
        if (distribution < 2) {
            if (incrementBuffer) setBuffer(connection.passiveBuffer, tickBuffer(buffer, ratePerSecond))
            return false
        }

        setBuffer(connection.passiveBuffer, buffer - distribution)
        val half = distribution / 2

        if (value1 > value2) {
            setValue(connection.player1, value1 - half)
            setValue(connection.player2, value2 + half)
        } else {
            setValue(connection.player1, value1 + half)
            setValue(connection.player2, value2 - half)
        }

        return true
    }

    private fun tickBuffer(
        current: Float,
        ratePerSecond: Float,
    ) = current + (ratePerSecond / (1000 / TPS))
}

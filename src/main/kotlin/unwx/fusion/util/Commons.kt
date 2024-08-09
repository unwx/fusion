@file:Suppress("NOTHING_TO_INLINE")

package unwx.fusion.util

import dev.dominion.ecs.api.Entity
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import unwx.fusion.entity.Partner
import java.time.Duration

const val TPS = 20

inline fun callEvent(event: Event) = Bukkit.getPluginManager().callEvent(event)

inline fun Double.square() = this * this

inline fun Duration.toTicks(): Int = (this.toMillis() / (1000 / TPS)).toInt()

inline fun Entity.getPlayer(): Player? = this.get(Player::class.java)

inline fun Entity.getPartner(): Player? = this.get(Partner::class.java)?.player

inline fun Player.distanceSquared(player: Player): Double = this.location.distanceSquared(player.location)

fun scale(
    value: Double,
    oldMin: Double,
    oldMax: Double,
    newMin: Double,
    newMax: Double
): Double {
    val oldRange = oldMax - oldMin
    val newRange = newMax - newMin
    var normalizedValue = value
    normalizedValue = oldMin.coerceAtLeast(normalizedValue)
    normalizedValue = oldMax.coerceAtMost(normalizedValue)

    return ((normalizedValue - oldMin) * newRange / oldRange) + newMin
}

inline fun scale(
    value: Float,
    oldMin: Float,
    oldMax: Float,
    newMin: Float,
    newMax: Float
): Float = scale(value.toDouble(), oldMin.toDouble(), oldMax.toDouble(), newMin.toDouble(), newMax.toDouble()).toFloat()

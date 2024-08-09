@file:Suppress("NOTHING_TO_INLINE")

package unwx.fusion.util

import org.bukkit.Bukkit
import org.bukkit.event.Event
import java.time.Duration

const val TPS = 20

inline fun callEvent(event: Event) = Bukkit.getPluginManager().callEvent(event)

inline fun Double.square() = this * this

inline fun Duration.toTicks(): Int = (this.toMillis() / (1000 / TPS)).toInt()

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

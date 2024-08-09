package unwx.fusion.util.particle

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.World
import kotlin.math.pow

class GeometricPainter(
    private val particle: Particle,
    private val commonRatio: Double,
    private val extra: Double = 0.0
) : ParticlePainter() {
    override fun internalDrawLine(from: Location, to: Location) {
        val world = from.world!!
        val direction = to.subtract(from).toVector().normalize()

        val distance = from.distance(to)
        var numIndex = 2
        var i = 0.0

        val getPosition = { from.clone().add(direction.clone().multiply(i)) }
        val progress = {
            numIndex++
            commonRatio.pow(numIndex.toDouble()) - commonRatio
        }

        val distanceHalf = distance / 2.0
        while (i < distanceHalf) {
            drawAt(getPosition(), world)
            i += progress()
        }

        numIndex = 2
        i = distance
        while (i > distanceHalf) {
            drawAt(getPosition(), world)
            i -= progress()
        }
    }

    private fun drawAt(
        position: Location,
        world: World
    ) {
        world.spawnParticle(particle, position, 1, 0.0, 0.0, 0.0, extra, null, false)
    }
}

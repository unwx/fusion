package unwx.fusion.util.particle

import org.bukkit.Location
import org.bukkit.Particle

class LinearPainter(
    private val particle: Particle,
    private val maxParticles: Int,
    private val minParticlesInterval: Double,
    private val extra: Double = 0.0
) : ParticlePainter() {
    @Suppress("UnnecessaryVariable")
    override fun internalDrawLine(from: Location, to: Location) {
        val distance = from.distance(to)
        val interval = (distance / maxParticles).coerceAtLeast(minParticlesInterval)
        val direction = to
            .subtract(from)
            .toVector()
            .normalize()
            .multiply(interval)

        val position = from
        var i = 0.0

        while (i < distance) {
            from.world!!.spawnParticle(particle, position, 1, 0.0, 0.0, 0.0, extra, null, false)
            position.add(direction)
            i += interval
        }
    }
}
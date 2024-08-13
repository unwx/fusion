package unwx.fusion.util.particle

import org.bukkit.Particle.END_ROD
import org.bukkit.Particle.SONIC_BOOM

object Particles {
    private val COMPASS_PING = END_ROD
    private val TELEPORT_TRACE = SONIC_BOOM

    val compassPingPainter = GeometricPainter(
        COMPASS_PING,
        1.6
    )

    val teleportTracePainter = GeometricPainter(
        TELEPORT_TRACE,
        2.1
    )
}

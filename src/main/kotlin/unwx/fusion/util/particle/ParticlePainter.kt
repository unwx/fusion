package unwx.fusion.util.particle

import org.bukkit.Location
import unwx.fusion.util.Logger.warn

abstract class ParticlePainter {
    @Suppress("NAME_SHADOWING")
    fun drawLine(from: Location, to: Location) {
        val from = from.clone()
        val to = to.clone()

        if (from.world != to.world) {
            warn {"Cannot draw line between ${from.world} and ${to.world}"}
            return
        }
        if (from.world == null) {
            warn {"Cannot spawn particles between '$from' and '$to': location has no world"}
            return
        }

        internalDrawLine(from, to)
    }

    protected abstract fun internalDrawLine(from: Location, to: Location)
}

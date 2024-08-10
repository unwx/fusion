package unwx.fusion.listener.event

import org.bukkit.event.HandlerList
import unwx.fusion.entity.Connection
import unwx.fusion.entity.Fusion

class ConnectedEvent(
    val connection: Connection,
    fusion: Fusion
) : FusionEvent(fusion) {
    companion object {
        private val staticHandlers: HandlerList = HandlerList()
    }

    override fun getHandlers() = staticHandlers
}

package unwx.fusion.listener.event

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import unwx.fusion.entity.Fusion

class PlayerIsActiveEvent(
    val player: Player,
    fusion: Fusion
) : FusionEvent(fusion) {
    companion object {
        private val staticHandlers: HandlerList = HandlerList()
    }

    override fun getHandlers() = staticHandlers
}

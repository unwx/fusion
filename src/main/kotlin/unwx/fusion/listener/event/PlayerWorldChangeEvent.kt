package unwx.fusion.listener.event

import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import unwx.fusion.entity.Fusion

class PlayerWorldChangeEvent(
    val player: Player,
    val from: World,
    val to: World,
    fusion: Fusion
) : FusionEvent(fusion) {
    companion object {
        private val staticHandlers: HandlerList = HandlerList()
    }

    override fun getHandlers() = staticHandlers
}

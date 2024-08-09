package unwx.fusion.listener.event

import dev.dominion.ecs.api.Entity
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import unwx.fusion.entity.Fusion

class PlayerChangedWorldEvent(
    val from: World,
    player: Player,
    entity: Entity,
    fusion: Fusion
) : ActivePlayerEvent(player, entity, fusion) {
    companion object {
        private val staticHandlers: HandlerList = HandlerList()
    }

    override fun getHandlers() = staticHandlers
}

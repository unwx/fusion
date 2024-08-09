package unwx.fusion.listener.event

import dev.dominion.ecs.api.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import unwx.fusion.entity.Fusion

abstract class FusionEvent(val fusion: Fusion) : Event()

abstract class ActivePlayerEvent(
    val player: Player,
    val entity: Entity,
    fusion: Fusion
) : FusionEvent(fusion)

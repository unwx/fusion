package unwx.fusion.listener.event

import org.bukkit.event.Event
import unwx.fusion.entity.Fusion

abstract class FusionEvent(val fusion: Fusion) : Event()

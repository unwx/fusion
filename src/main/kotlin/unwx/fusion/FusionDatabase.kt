package unwx.fusion

import org.bukkit.entity.Player
import org.bukkit.event.Listener
import unwx.fusion.entity.Fusion
import java.util.*
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap as HashMap

class FusionDatabase : Listener {
    private val playerToFusion = HashMap<UUID, Fusion>()

    fun findBy(player: Player): Fusion? = playerToFusion[player.uniqueId]

    fun forEach(action: (Fusion) -> Unit) = playerToFusion.values.forEach(action)
}

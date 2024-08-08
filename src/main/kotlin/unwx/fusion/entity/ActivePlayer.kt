package unwx.fusion.entity

import org.bukkit.entity.Player

class ActivePlayer(private val delegate: Player) : Player by delegate {
    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other is Player -> delegate.uniqueId == other.uniqueId
            else -> false
        }
    }

    override fun hashCode(): Int = delegate.uniqueId.hashCode()
}

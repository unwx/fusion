package unwx.fusion.entity

import org.bukkit.entity.Player

class WithCompassInHand
class Connection(
    val player1: Player,
    val player2: Player,
    val passiveBuffer: PassiveDistributionBuffer
) {
    inline fun forEach(action: (Player) -> Unit) {
        action(player1)
        action(player2)
    }
}

class PassiveDistributionBuffer(
    var health: Float = 0.0f,
    var food: Float = 0.0f,
)

interface WorldMarker
class NormalWorld : WorldMarker
class NetherWorld : WorldMarker
class EndWorld : WorldMarker
class OtherWorld : WorldMarker

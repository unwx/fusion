package unwx.fusion.entity

import org.bukkit.World
import org.bukkit.entity.Player

class WithCompassInHand
class Partner(val player: Player)

abstract class WorldDelegate(private val delegate: World) : World by delegate
class Overworld(world: World) : WorldDelegate(world)
class OtherWorld(world: World) : WorldDelegate(world)

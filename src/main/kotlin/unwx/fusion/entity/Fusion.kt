package unwx.fusion.entity

import dev.dominion.ecs.api.Dominion
import dev.dominion.ecs.api.Entity
import dev.dominion.ecs.api.Results
import dev.dominion.ecs.api.Results.With2
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap as HashMap

interface Fusion {
    /**
     * The unique name of this fusion.
     */
    val name: String

    /**
     * The current level of this fusion.
     *
     * @see Level
     */
    var level: Level

    /**
     * The remaining time (in ticks) until this fusion levels up.
     */
    var timeToNextLevelLeft: Int


    fun <T2 : Any> getComponents(comp1: KClass<T2>, withEntity: Boolean): Results<With2<Player, T2>>

    fun getEntity(player: Player): Entity? = getEntity(player.uniqueId)

    fun getEntity(id: UUID): Entity?

    fun addEntity(player: Player, vararg components: Any): Entity

    fun removeEntity(player: Player) = removeEntity(player.uniqueId)

    fun removeEntity(id: UUID)

    fun getOrAddEntity(player: Player) = getEntity(player) ?: addEntity(player)
}

class FusionImpl(
    override val name: String,
    override var level: Level,
    override var timeToNextLevelLeft: Int
) : Fusion {
    private val ecs = Dominion.create("fusion_${name}")
    private val playerToEntity = HashMap<UUID, Entity>()

    override fun <T2 : Any> getComponents(comp1: KClass<T2>, withEntity: Boolean): Results<With2<Player, T2>> {
        return if (withEntity) ecs.findEntitiesWith(Player::class.java, comp1.java)
        else ecs.findCompositionsWith(Player::class.java, comp1.java)
    }

    override fun getEntity(id: UUID): Entity? = playerToEntity[id]

    override fun removeEntity(id: UUID) {
        val entity = playerToEntity.remove(id) ?: return
        ecs.deleteEntity(entity)
    }

    override fun addEntity(
        player: Player,
        vararg components: Any
    ): Entity = ecs.createEntity(*components).also {
        it.add(player)
        playerToEntity[player.uniqueId] = it
    }
}
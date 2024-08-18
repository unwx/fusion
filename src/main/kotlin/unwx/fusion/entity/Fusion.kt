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
     * The current level of this fusion.
     *
     * @see Level
     */
    var level: Level

    /**
     * The remaining time (in ticks) until this fusion levels up.
     */
    var timeToNextLevelLeft: Int

    fun <T1 : Any> getComponent(comp1: KClass<T1>): Results<T1>

    fun <T1 : Any, T2: Any> getComponents(comp1: KClass<T1>, comp2: KClass<T2>, withEntity: Boolean): Results<With2<T1, T2>>

    fun getEntity(player: Player): Entity? = getEntity(player.uniqueId)

    fun getEntity(id: UUID): Entity?

    fun addEntity(player: Player, vararg components: Any): Entity

    fun removeEntity(player: Player) = removeEntity(player.uniqueId)

    fun removeEntity(id: UUID)
}

class FusionImpl(
    override var level: Level,
    override var timeToNextLevelLeft: Int
) : Fusion {
    companion object {
        private var counter = 0
    }

    constructor(level: Level): this(level, level.timeToNextLevel ?: -1)

    private val ecs = Dominion.create("fusion_${counter++}")
    private val playerToEntity = HashMap<UUID, Entity>()


    override fun <T1 : Any> getComponent(comp1: KClass<T1>): Results<T1> = ecs.findCompositionsWith(comp1.java)

    override fun <T1 : Any, T2 : Any> getComponents(
        comp1: KClass<T1>,
        comp2: KClass<T2>,
        withEntity: Boolean
    ): Results<With2<T1, T2>> {
        return if (withEntity) ecs.findEntitiesWith(comp1.java, comp2.java)
        else ecs.findCompositionsWith(comp1.java, comp2.java)
    }


    override fun getEntity(id: UUID): Entity? = playerToEntity[id]

    override fun removeEntity(id: UUID) {
        val entity = playerToEntity.remove(id) ?: return
        ecs.deleteEntity(entity)
        /*
         * "Removes the entity by freeing the id and canceling the reference to all components, if any."
         * Therefore, there is no point in returning the entity to the caller.
         */
    }

    override fun addEntity(
        player: Player,
        vararg components: Any
    ): Entity = ecs.createEntity(*components).also {
        it.add(player)
        playerToEntity[player.uniqueId] = it
    }
}

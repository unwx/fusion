package unwx.fusion.listener

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import org.bukkit.Particle.*
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerExpChangeEvent
import unwx.fusion.FusionDatabase
import unwx.fusion.entity.Connection
import unwx.fusion.entity.Fusion
import unwx.fusion.listener.event.ConnectedEvent
import unwx.fusion.listener.event.DisconnectedEvent
import unwx.fusion.util.PotionEffects.isDistributed
import unwx.fusion.util.PotionEffects.toDistributed
import unwx.fusion.util.PotionEffects.unwrapHidden
import unwx.fusion.util.getConnection
import unwx.fusion.util.particle.LinearPainter
import unwx.fusion.util.sound.RandomSound
import unwx.fusion.util.sound.RandomSounds
import unwx.fusion.util.sound.Sounds.HIGHER
import unwx.fusion.util.sound.Sounds.HIGHEST
import unwx.fusion.util.sound.Sounds.LOWER
import unwx.fusion.util.sound.Sounds.LOWEST
import unwx.fusion.util.sound.Sounds.playAt
import java.util.*
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet as HashSet
import org.bukkit.entity.Entity as BukkitEntity

class ReactiveDistributor(private val database: FusionDatabase) : Listener {
    private val damagedPlayers = HashSet<UUID>()
    private val receivedXpByDistribution = HashSet<UUID>()

    val distributeDamagePainter = LinearPainter(
        LAVA,
        12,
        2.7,
        0.4
    )
    val distributeDeathPainter = LinearPainter(
        SCULK_SOUL,
        35,
        2.0,
    )
    val distributePotionEffectPainter = LinearPainter(
        SPELL_WITCH,
        12,
        2.6,
        0.2
    )
    val distributeDamageSound = RandomSound(
        LOWEST,
        HIGHEST,
        Sound.BLOCK_NOTE_BLOCK_XYLOPHONE,
        Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE,
        Sound.BLOCK_NOTE_BLOCK_COW_BELL,
        Sound.BLOCK_NOTE_BLOCK_PLING,
        Sound.BLOCK_NOTE_BLOCK_GUITAR
    )
    val distributeDeathSound = RandomSounds(
        8,
        11,
        RandomSound(
            LOWEST,
            LOWER,
            Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,
            Sound.BLOCK_NOTE_BLOCK_BASS
        ),
        RandomSound(
            HIGHER,
            HIGHEST,
            Sound.BLOCK_NOTE_BLOCK_BELL,
            Sound.BLOCK_NOTE_BLOCK_FLUTE
        )
    )


    @EventHandler
    fun onTick(event: ServerTickStartEvent) {
        damagedPlayers.clear()
        receivedXpByDistribution.clear()
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerDamage(event: EntityDamageEvent) {
        val c = readContext(event) ?: return
        val level = c.fusion.level

        if (!level.isDamageDistributed) return

        val player1 = c.connection.player1
        val player2 = c.connection.player2

        event.isCancelled = true
        c.connection.forEach { if (damagedPlayers.contains(it.uniqueId)) return }

        val distribution = event.finalDamage * level.damageDistributionPercentage
        if (distribution <= 0) return

        player1.damage(0.01, event.damageSource)
        player2.damage(0.01, player1)
        c.connection.forEach {
            it.health = (it.health - distribution).coerceAtLeast(0.0)
            damagedPlayers.add(it.uniqueId)
        }

        distributeDamagePainter.drawLine(player1, player2)
        distributeDamageSound.sound().playAt(player1, player2)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val c = readContext(event) ?: return
        val player1 = c.connection.player1
        val player2 = c.connection.player2

        player2.health = 1.0
        player2.absorptionAmount = 0.0
        player2.damage(1_000_000_000.0, player1)

        distributeDeathPainter.drawLine(player1, player2)
        distributeDeathSound.sounds().playAt(player1, player2)
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerExpChange(event: PlayerExpChangeEvent) {
        val c = readContext(event) ?: return
        val level = c.fusion.level
        val player1 = c.connection.player1
        val player2 = c.connection.player2

        if (!level.isXpDistributed || receivedXpByDistribution.contains(player1.uniqueId)) return

        val distribution = (event.amount * level.xpDistributionPercentage).toInt()
        event.amount = distribution

        player2.giveExp(distribution, true)
        receivedXpByDistribution.add(player2.uniqueId)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityPotionEffect(event: EntityPotionEffectEvent) {
        if (event.cause == EntityPotionEffectEvent.Cause.PLUGIN) return

        val c = readContext(event) ?: return
        if (!c.fusion.level.arePotionEffectsDistributed) return
        distributePotionEffects(c.connection)
    }

    @EventHandler
    fun onConnect(event: ConnectedEvent) {
        if (!event.fusion.level.arePotionEffectsDistributed) return
        distributePotionEffects(event.connection)
    }

    @EventHandler
    fun onDisconnect(event: DisconnectedEvent) {
        event.connection.forEach(::removeDistributedPotionEffects)
    }


    private fun distributePotionEffects(connection: Connection) {
        val player1 = connection.player1
        val player2 = connection.player2

        val effects1 = player1.activePotionEffects.unwrapHidden()
        val effects2 = player2.activePotionEffects.unwrapHidden()

        player1.clearActivePotionEffects()
        player2.clearActivePotionEffects()

        effects1.forEach {
            player1.addPotionEffect(it)
            player2.addPotionEffect(it.toDistributed())
        }
        effects2.forEach {
            player2.addPotionEffect(it)
            player1.addPotionEffect(it.toDistributed())
        }
        distributePotionEffectPainter.drawLine(player1, player2)
    }

    private fun removeDistributedPotionEffects(player: Player) {
        val effects = player.activePotionEffects.unwrapHidden().filter { !it.isDistributed() }
        if (!player.clearActivePotionEffects()) return
        player.addPotionEffects(effects)
    }


    private fun readContext(event: EntityEvent) = readContext(event.entity)

    private fun readContext(event: PlayerEvent) = readContext(event.player)

    private fun readContext(bukkitEntity: BukkitEntity): Context? {
        val player = bukkitEntity as? Player ?: return null
        val fusion = database.findBy(player) ?: return null
        val entity = fusion.getEntity(player) ?: return null
        val connection = entity.getConnection() ?: return null
        return Context(fusion, connection)
    }

    private class Context(
        val fusion: Fusion,
        val connection: Connection
    )
}

package unwx.fusion.util.sound

import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import unwx.fusion.entity.Teleport
import unwx.fusion.util.Logger.warn
import unwx.fusion.util.scale
import unwx.fusion.util.square
import unwx.fusion.util.toTicks
import java.time.Duration

object Sounds {
    const val LOWEST = 0.5f
    const val LOWER = 0.6f
    const val LOW = 0.75f
    const val NORMAL = 1.0f
    const val HIGH = 1.5f
    const val HIGHER = 1.75f
    const val HIGHEST = 2f

    private const val TELEPORT_MIN_PITCH = 0.55f
    private const val TELEPORT_MAX_PITCH = 1.25f
    private const val TELEPORT_MIN_VOLUME = 1.0f
    private const val TELEPORT_MAX_VOLUME = 2.0f

    private const val PING_RECEIVE_VOLUME = 0.2f
    private const val PING_SEND_VOLUME = 1.0f
    private const val PING_MIN_PITCH = LOWEST
    private const val PING_MAX_PITCH = HIGHEST

    private val PING = Sound.BLOCK_NOTE_BLOCK_GUITAR
    private val TELEPORT_BOOM = Sound.ENTITY_WARDEN_SONIC_BOOM
    private val TELEPORT_PREPARING = Sound.ENTITY_WARDEN_SONIC_CHARGE
    private val TELEPORT_PREPARING_DURATION = Duration.ofMillis(1700)

    val compassFailure = RandomSound(
        LOWEST,
        LOWER,
        Sound.BLOCK_NOTE_BLOCK_BASEDRUM
    )

    val damageDistributed = RandomSound(
        LOWEST,
        HIGHEST,
        Sound.BLOCK_NOTE_BLOCK_XYLOPHONE,
        Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE,
        Sound.BLOCK_NOTE_BLOCK_COW_BELL,
        Sound.BLOCK_NOTE_BLOCK_PLING,
        Sound.BLOCK_NOTE_BLOCK_GUITAR
    )

    fun getPing(
        currentStep: Int,
        maxSteps: Int,
        isSending: Boolean,
    ) = AdvanceSound(
        PING,
        scale(
            currentStep.toFloat(),
            0.0f,
            maxSteps.toFloat(),
            PING_MIN_PITCH,
            PING_MAX_PITCH
        ),
        if (isSending) PING_SEND_VOLUME else PING_RECEIVE_VOLUME
    )

    fun getTeleportBoom(distanceSquared: Double) = AdvanceSound(
        TELEPORT_BOOM,
        scale(
            distanceSquared,
            0.0,
            Teleport.FAR_DISTANCE.toDouble().square(),
            TELEPORT_MIN_PITCH.toDouble(),
            TELEPORT_MAX_PITCH.toDouble()
        ).toFloat(),
        scale(
            distanceSquared,
            0.0,
            Teleport.FAR_DISTANCE.toDouble().square(),
            TELEPORT_MIN_VOLUME.toDouble(),
            TELEPORT_MAX_VOLUME.toDouble()
        ).toFloat()
    )

    fun getTeleportPreparing(distanceSquared: Double): Pair<AdvanceSound, Int> {
        val sound = AdvanceSound(
            TELEPORT_PREPARING,
            scale(
                distanceSquared,
                0.0,
                Teleport.FAR_DISTANCE.toDouble().square(),
                TELEPORT_MIN_PITCH.toDouble(),
                TELEPORT_MAX_PITCH.toDouble()
            ).toFloat(),
            scale(
                distanceSquared,
                0.0,
                Teleport.FAR_DISTANCE.toDouble().square(),
                TELEPORT_MIN_VOLUME.toDouble(),
                TELEPORT_MAX_VOLUME.toDouble()
            ).toFloat()
        )
        val duration = Duration.ofMillis(
            (TELEPORT_PREPARING_DURATION.toMillis().toDouble() / sound.pitch.toDouble()).toLong()
        )
        return sound to duration.toTicks()
    }


    fun AdvanceSound.playAt(vararg players: Player) {
        players.forEach { this.playAt(it.location) }
    }

    fun AdvanceSound.playAt(vararg locations: Location) {
        locations.forEach {
            val world = it.world
            if (world == null) {
                warn { "Cannot play sound '$this': location has no world" }
                return
            }

            world.playSound(it, this.sound, this.volume, this.pitch)
        }
    }

    fun Array<AdvanceSound>.playAt(vararg players: Player) {
        this.forEach { it.playAt(*players) }
    }

    fun Array<AdvanceSound>.playAt(vararg locations: Location) {
        this.forEach { it.playAt(*locations) }
    }
}

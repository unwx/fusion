package unwx.fusion.util.particle

import org.bukkit.Particle.*

object Particles {
    private val FUSION_CONNECT = HEART
    private val DISTRIBUTE_DEATH = SCULK_SOUL
    private val DISTRIBUTE0_POTION_EFFECT = SPELL_WITCH
    private val DISTRIBUTE_DAMAGE = LAVA
    private val DISTRIBUTE_HEALTH = GLOW
    private val DISTRIBUTE_FOOD = TOTEM
    private val COMPASS_PING = END_ROD
    private val TELEPORT_TRACE = SONIC_BOOM

    val distributeDeathPainter = LinearPainter(
        DISTRIBUTE_DEATH,
        35,
        2.0,
    )

    val distributePotionEffectPainter = LinearPainter(
        DISTRIBUTE0_POTION_EFFECT,
        12,
        2.6,
        0.2
    )

    val distributeDamagePainter = LinearPainter(
        DISTRIBUTE_DAMAGE,
        12,
        2.7,
        0.4
    )

    val distributeHealthPainter = LinearPainter(
        DISTRIBUTE_HEALTH,
        11,
        2.8,
        0.2
    )

    val distributeFoodPainter = LinearPainter(
        DISTRIBUTE_FOOD,
        9,
        3.0,
        0.2
    )

    val compassPingPainter = GeometricPainter(
        COMPASS_PING,
        1.6
    )

    val teleportTracePainter = GeometricPainter(
        TELEPORT_TRACE,
        2.1
    )
}

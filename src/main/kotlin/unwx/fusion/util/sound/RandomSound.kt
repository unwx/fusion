package unwx.fusion.util.sound

import org.bukkit.Sound
import unwx.fusion.util.sound.Sounds.HIGHEST
import unwx.fusion.util.sound.Sounds.LOWEST
import unwx.fusion.util.sound.Sounds.NORMAL
import kotlin.random.Random

class RandomSound(
    private val pitchFrom: Float = NORMAL,
    private val pitchTo: Float = NORMAL,
    private val volumeFrom: Float = NORMAL,
    private val volumeTo: Float = NORMAL,
    private vararg val sounds: Sound
) {
    constructor(
        pitchFrom: Float = NORMAL,
        pitchTo: Float = NORMAL,
        vararg sounds: Sound
    ): this(pitchFrom, pitchTo, NORMAL, NORMAL, *sounds)

    init {
        require(pitchFrom in LOWEST..HIGHEST) { "pitchFrom '$pitchFrom' must be >= $LOWEST && <= $HIGHEST" }
        require(pitchTo in LOWEST..HIGHEST) { "pitchTo '$pitchTo' must be >= $LOWEST && <= $HIGHEST" }
        require(pitchFrom <= pitchTo) { "pitchFrom '$pitchFrom' must be <= pitchTo '$pitchTo'" }

        require(volumeFrom in LOWEST..HIGHEST) { "volumeFrom '$volumeFrom' must be >= $LOWEST && <= $HIGHEST" }
        require(volumeTo in LOWEST..HIGHEST) { "volumeTo '$volumeTo' must be >= $LOWEST && <= $HIGHEST" }
        require(volumeFrom <= volumeTo) { "volumeFrom '$volumeFrom' must be <= volumeTo '$volumeTo'" }
    }

    fun sound() = AdvanceSound(
        sounds[sounds.indices.random()],
        Random.nextDouble(pitchFrom.toDouble(), pitchTo.toDouble()).toFloat(),
        Random.nextDouble(volumeFrom.toDouble(), volumeTo.toDouble()).toFloat(),
    )
}

package unwx.fusion.util.sound

class RandomSounds(
    private val min: Int,
    private val max: Int,
    private vararg val sounds: RandomSound
) {
    init {
        require(min >= 0 && max >= 0) { "min ($min) or max ($max) is negative" }
        require(min <= max) { "min ($min) must be <= max ($max)" }
    }

    fun sounds(): Array<AdvanceSound> {
        val result = arrayOfNulls<AdvanceSound>((min until max).random())
        for (i in 0 until max) result[i] = sounds[sounds.indices.random()].sound()

        @Suppress("UNCHECKED_CAST")
        return result as Array<AdvanceSound>
    }
}

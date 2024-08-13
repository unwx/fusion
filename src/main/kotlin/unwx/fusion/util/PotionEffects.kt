@file:Suppress("NOTHING_TO_INLINE")

package unwx.fusion.util

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.bukkit.NamespacedKey
import org.bukkit.potion.PotionEffect
import unwx.fusion.plugin

object PotionEffects {
    private val distributed = NamespacedKey(plugin(), "fusion_pe_distributed")

    fun Collection<PotionEffect>.unwrapHidden(): List<PotionEffect> {
        val result = ObjectArrayList<PotionEffect>()
        for (effect in this) {
            var current: PotionEffect? = effect
            while (current != null) {
                result.add(current)
                current = current.hiddenPotionEffect
            }
        }

        return result
    }

    fun PotionEffect.isDistributed() = this.key == distributed

    fun PotionEffect.toDistributed() = this.setKey(distributed)

    inline fun Collection<PotionEffect>.toDistributed() = this.map { it.toDistributed() }

    private fun PotionEffect.setKey(key: NamespacedKey) = PotionEffect(
        this.type,
        this.duration,
        this.amplifier,
        this.isAmbient,
        this.hasParticles(),
        this.hasIcon(),
        key
    )
}

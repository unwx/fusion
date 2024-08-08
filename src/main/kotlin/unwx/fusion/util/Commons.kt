@file:Suppress("NOTHING_TO_INLINE")

package unwx.fusion.util

import org.bukkit.Bukkit
import org.bukkit.event.Event

inline fun callEvent(event: Event) = Bukkit.getPluginManager().callEvent(event)

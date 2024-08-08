package unwx.fusion

import com.google.gson.Gson
import org.bukkit.plugin.java.JavaPlugin

val JSON = Gson()

fun plugin() = JavaPlugin.getPlugin(Plugin::class.java)

class Plugin : JavaPlugin() {
}

package unwx.fusion

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.bukkit.plugin.java.JavaPlugin

//val JSON = Gson()

fun plugin() = JavaPlugin.getPlugin(Plugin::class.java)

class Plugin : JavaPlugin() {
    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator
}

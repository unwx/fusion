package unwx.fusion.command

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class HintProvider<T : Any>(clazz: KClass<T>) : TabCompleter {
    private val hints: Array<List<String>?>

    init {
        // @formatter:off
        val constructor = clazz.primaryConstructor ?: throw IllegalArgumentException("Class '$clazz' must have a primary constructor")

        val parameters = constructor.parameters
        hints = Array(parameters.size) { null }

        for (i in parameters.indices) {
            val param = parameters[i]
            val fieldHints = when (val fieldType = param.type.classifier as KClass<*>) {
                Boolean::class -> listOf("true", "false")

                Enum::class -> {
                    @Suppress("UNCHECKED_CAST")
                    (fieldType.java as Class<out Enum<*>>).enumConstants.map { it.name.lowercase() }
                }

                else -> null
            }

            hints[i] = fieldHints
        }
        // @formatter:on
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): List<String>? {
        if (args.isEmpty()) return null
        return hints[args.size - 1]?.filter { it.startsWith(args.last()) }?.takeIf { it.isNotEmpty() }
    }
}

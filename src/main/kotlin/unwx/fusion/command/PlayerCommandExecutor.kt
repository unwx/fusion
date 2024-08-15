package unwx.fusion.command

import jakarta.validation.Validator
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import kotlin.reflect.KClass

abstract class PlayerCommandExecutor<T : Any>(
    private val parser: ArgumentParser<T>,
    private val hintProvider: HintProvider<T>
) : CommandExecutor, TabCompleter {
    constructor(clazz: KClass<T>, validator: Validator) : this(ArgumentParser(clazz, validator), HintProvider(clazz))

    final override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage(Component.text("I suspect you are a robot...", NamedTextColor.DARK_RED))
            return true
        }

        val parsedCommand = parser.parse(args)
        if (parsedCommand.second != null) {
            sender.sendMessage(parsedCommand.second!!)
            return true
        }

        onCommand(sender, parsedCommand.first!!)
        return false
    }

    final override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): List<String>? = hintProvider.onTabComplete(sender, command, label, args)


    abstract fun onCommand(
        player: Player,
        command: T
    )
}

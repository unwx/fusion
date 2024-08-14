package unwx.fusion.command

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validator
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class ArgumentParser<T : Any>(
    clazz: KClass<T>,
    private val validator: Validator
) {
    private val constructor: KFunction<T>
    private val strategies: Array<FieldStrategy>

    init {
        // @formatter:off
        val constructor = clazz.primaryConstructor ?: throw IllegalArgumentException("Class '$clazz' must have a primary constructor")

        val parameters = constructor.parameters
        val strategies = Array<FieldStrategy?>(parameters.size) { null }

        for (i in parameters.indices) {
            val param = parameters[i]
            val failureMessage: TextComponent
            val fieldParseFunc: (String) -> Any

            when (val fieldType = param.type.classifier as KClass<*>) {
                String::class -> {
                    fieldParseFunc = { it }
                    failureMessage = Component.text("What?", NamedTextColor.DARK_PURPLE)
                }

                Int::class -> {
                    fieldParseFunc = { it.toInt() }
                    failureMessage = Component
                        .text("Not a ", NamedTextColor.WHITE)
                        .append(Component.text("number", NamedTextColor.BLUE))
                }

                Boolean::class -> {
                    fieldParseFunc = { it.toBoolean() }
                    failureMessage = Component
                        .text("Not a ", NamedTextColor.WHITE)
                        .append(Component.text("true", NamedTextColor.GREEN))
                        .append(Component.text("/", NamedTextColor.WHITE))
                        .append(Component.text("false", NamedTextColor.RED))
                }

                Enum::class -> {
                    @Suppress("UNCHECKED_CAST")
                    val constants = (fieldType.java as Class<out Enum<*>>).enumConstants.associateBy { it.name.lowercase() }
                    fieldParseFunc = { constants[it] ?: throw IllegalArgumentException("Unknown enum constant '$it'") }

                    var messageBuilder = Component.text("Unknown value, valid: [", NamedTextColor.WHITE)
                    val iterator = constants.keys.iterator()
                    for (y in 0 until constants.keys.size - 1) {
                        messageBuilder = messageBuilder
                            .append(Component.text(iterator.next(), NamedTextColor.BLUE))
                            .append(Component.text(", ", NamedTextColor.WHITE))
                    }

                    messageBuilder = messageBuilder
                        .append(Component.text(iterator.next(), NamedTextColor.BLUE))
                        .append(Component.text("]", NamedTextColor.WHITE))

                    failureMessage = messageBuilder
                }

                else -> throw IllegalArgumentException("Class '$clazz' has property with unexpected type '$fieldType'")
            }

            strategies[i] = FieldStrategy(param, failureMessage, fieldParseFunc)
        }

        @Suppress("UNCHECKED_CAST")
        this.strategies = strategies as Array<FieldStrategy>
        this.constructor = constructor
        // @formatter:on
    }

    fun parse(args: Array<String>): Pair<T?, Component?> {
        val error = { err: Component -> null to err }
        val ok = { value: T -> value to null }

        if (args.size > strategies.size) {
            return error(
                Component
                    .text("Invalid number of arguments. Maximum args allowed: ", NamedTextColor.RED)
                    .append(Component.text(strategies.size, NamedTextColor.GREEN))
                    .append(Component.text(", actual: ", NamedTextColor.RED))
                    .append(Component.text(args.size, NamedTextColor.DARK_RED))
            )
        }

        val parsedArgs = Object2ObjectOpenHashMap<KParameter, Any?>()
        for (i in strategies.indices) {
            val arg = args.getOrNull(i)
            val strategy = strategies[i]
            val param = strategy.param

            if (arg.isNullOrBlank() || arg == "?") {
                if (param.isOptional) {
                    continue
                }
                if (param.type.isMarkedNullable) {
                    parsedArgs[param] = null
                    continue
                }
                return error(
                    Component
                        .text("Parameter ", NamedTextColor.RED)
                        .append(Component.text(param.name ?: (i + 1).toString(), NamedTextColor.BLUE))
                        .append(Component.text(" is mandatory", NamedTextColor.RED))
                )
            }

            try {
                parsedArgs[param] = strategy.parse(arg)
            } catch (e: Exception) {
                return error(strategy.failureMessage)
            }
        }

        val constructed = constructor.callBy(parsedArgs)
        val violations = validator.validate(constructed)

        if (violations.isNotEmpty()) {
            val iterator = violations.iterator()
            var messageBuilder = Component
                .text("Invalid args: ", NamedTextColor.RED)
                .append(Component.newline())

            val writeViolation = { violation: ConstraintViolation<*> ->
                Component
                    .text("- ", NamedTextColor.RED)
                    .append(Component.text(violation.propertyPath.last().name, NamedTextColor.BLUE))
                    .append(Component.text(": ${violation.message}", NamedTextColor.RED))
            }

            for (i in 0 until violations.size - 1) {
                messageBuilder = messageBuilder
                    .append(writeViolation(iterator.next()))
                    .append(Component.newline())
            }

            messageBuilder = messageBuilder.append(writeViolation(iterator.next()))
            return error(messageBuilder)
        }

        return ok(constructed)
    }

    private class FieldStrategy(
        val param: KParameter,
        val failureMessage: TextComponent,
        val parse: (String) -> Any
    )
}

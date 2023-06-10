package ru.meldren.abc

import ru.meldren.abc.common.CommandData
import ru.meldren.abc.exception.CommandInvocationException
import ru.meldren.abc.processor.ArgumentParser
import ru.meldren.abc.processor.ExceptionHandler
import ru.meldren.abc.processor.PermissionHandler
import ru.meldren.abc.processor.SuggestionProvider
import ru.meldren.abc.processor.cooldown.CooldownHandler
import ru.meldren.abc.processor.validator.ArgumentValidator
import ru.meldren.abc.service.*
import kotlin.reflect.*

@Suppress("NOTHING_TO_INLINE")
open class CommandManager<S : Any>(val commandPrefix: String = "/") {

    private val _registeredCommands = mutableSetOf<CommandData>()
    private val _defaultParsers = mutableMapOf<KClass<*>, ArgumentParser<*>>()
    private val _parsers = mutableMapOf<KClass<out ArgumentParser<*>>, ArgumentParser<*>>()
    private val _validators = mutableMapOf<KClass<out Annotation>, ArgumentValidator<*, *>>()
    private val _handlers = mutableMapOf<KClass<out CommandInvocationException>, ExceptionHandler<*, *>>()
    private val _suggestions = mutableMapOf<KClass<out SuggestionProvider<S>>, SuggestionProvider<S>>()

    @PublishedApi
    internal val commandRegistry = CommandRegistry(_registeredCommands)

    @PublishedApi
    internal val processorRegistry = ProcessorRegistry(_defaultParsers, _parsers, _validators, _handlers, _suggestions)

    @PublishedApi
    internal val commandInvoker = CommandInvoker(
        commandPrefix, processorRegistry, _registeredCommands,
        _defaultParsers, _parsers, _validators, _handlers, _suggestions
    )

    val registeredCommands
        get() = _registeredCommands.toSet()
    val defaultParsers
        get() = _defaultParsers.toMap()
    val parsers
        get() = _parsers.toMap()
    val validators
        get() = _validators.toMap()
    val handlers
        get() = _handlers.toMap()
    val suggestions
        get() = _suggestions.toMap()

    /* Permission handler */

    inline fun registerPermissionHandler(handler: PermissionHandler<S>) =
        processorRegistry.registerPermissionHandler(handler)

    inline fun unregisterPermissionHandler() = processorRegistry.unregisterPermissionHandler()

    /* Cooldown handler */

    inline fun registerCooldownHandler(handler: CooldownHandler<S>) = processorRegistry.registerCooldownHandler(handler)

    inline fun unregisterCooldownHandler() = processorRegistry.unregisterCooldownHandler()

    /* Commands registry */

    inline fun registerCommand(command: Any) = commandRegistry.registerCommand(command)

    inline fun <reified T> unregisterCommand() = commandRegistry.unregisterCommand(T::class)

    inline fun <T> invokeCommand(sender: S, input: String) = commandInvoker.invokeCommand<T>(sender, input)

    inline fun generateSuggestions(sender: S, input: String): List<String> =
        commandInvoker.generateSuggestions(sender, input)

    /* Parsers registry */

    inline fun <T : Any> registerDefaultParser(parser: ArgumentParser<T>) =
        processorRegistry.registerDefaultParser(parser)

    inline fun <reified T : Any> registerDefaultParser(crossinline parser: (String) -> T) {
        processorRegistry.registerDefaultParser(object : ArgumentParser<T> {
            override fun parse(arg: String) = parser(arg)
        }, T::class)
    }

    inline fun <reified T> unregisterDefaultParser() = processorRegistry.unregisterDefaultParser(T::class)

    inline fun registerParser(parser: ArgumentParser<*>) = processorRegistry.registerParser(parser)

    inline fun <reified T : ArgumentParser<*>> unregisterParser() = processorRegistry.unregisterParser(T::class)

    /* Validators registry */

    inline fun registerValidator(validator: ArgumentValidator<*, *>) = processorRegistry.registerValidator(validator)

    inline fun <T : Any, reified A : Annotation> registerValidator(crossinline validator: (T, A) -> Unit) {
        processorRegistry.registerValidator(object : ArgumentValidator<T, A> {
            override fun validate(arg: T, annotation: A) = validator(arg, annotation)
        }, A::class)
    }

    inline fun <reified T : Annotation> unregisterValidator() = processorRegistry.unregisterValidator(T::class)

    /* Exception handlers */

    inline fun <T : CommandInvocationException> registerExceptionHandler(handler: ExceptionHandler<T, S>) =
        processorRegistry.registerExceptionHandler(handler)

    inline fun <reified T : CommandInvocationException> registerExceptionHandler(crossinline handler: (T, S) -> Unit) {
        processorRegistry.registerExceptionHandler(object : ExceptionHandler<T, S> {
            override fun handle(exception: T, sender: S) = handler(exception, sender)
        }, T::class)
    }

    inline fun <reified T : CommandInvocationException> unregisterExceptionHandler() =
        processorRegistry.unregisterExceptionHandler(T::class)

    /* Suggestions providers */

    inline fun registerSuggestionProvider(provider: SuggestionProvider<S>) =
        processorRegistry.registerSuggestionProvider(provider)

    inline fun <reified T : SuggestionProvider<S>> unregisterSuggestionProvider() =
        processorRegistry.unregisterSuggestionProvider(T::class)
}
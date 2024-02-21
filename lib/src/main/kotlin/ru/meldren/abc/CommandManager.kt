package ru.meldren.abc

import ru.meldren.abc.common.CommandData
import ru.meldren.abc.exception.invocation.CommandInvocationException
import ru.meldren.abc.processor.*
import ru.meldren.abc.service.*
import kotlin.reflect.*

@Suppress("NOTHING_TO_INLINE")
open class CommandManager<S : Any, C : Any>(val commandPrefix: String = "/") {

    private val _registeredCommands = mutableSetOf<CommandData<C>>()
    private val _defaultParsers = mutableMapOf<KClass<*>, ArgumentParser<*>>()
    private val _parsers = mutableMapOf<KClass<out ArgumentParser<*>>, ArgumentParser<*>>()
    private val _validators = mutableMapOf<KClass<out Annotation>, ArgumentValidator<*, *, S>>()
    private val _handlers = mutableMapOf<KClass<out CommandInvocationException>, ExceptionHandler<*, *>>()
    private val _defaultSuggestions = mutableMapOf<KClass<*>, SuggestionProvider<S>>()
    private val _suggestions = mutableMapOf<KClass<out SuggestionProvider<S>>, SuggestionProvider<S>>()
    private val commandsByAliases = mutableMapOf<String, CommandData<C>>()

    @PublishedApi
    internal val commandRegistry = CommandRegistry(_registeredCommands, commandsByAliases)

    @PublishedApi
    internal val processorRegistry = ProcessorRegistry<S, C>(
        _defaultParsers, _parsers, _validators,
        _handlers, _defaultSuggestions, _suggestions
    )

    @PublishedApi
    internal val commandInvoker = CommandInvoker(
        commandPrefix, processorRegistry, _registeredCommands,
        _defaultParsers, _parsers, _validators, _handlers,
        _defaultSuggestions, _suggestions, commandsByAliases
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
    val defaultSuggestions
        get() = _defaultSuggestions.toMap()
    val suggestions
        get() = _suggestions.toMap()
    val permissionHandler
        get() = processorRegistry.permissionHandler
    val cooldownHandler
        get() = processorRegistry.cooldownHandler

    /* Permission handler */

    inline fun registerPermissionHandler(handler: PermissionHandler<S>) =
        processorRegistry.registerPermissionHandler(handler)

    /* Cooldown handler */

    inline fun registerCooldownHandler(handler: CooldownHandler<S, C>) = processorRegistry.registerCooldownHandler(handler)

    /* Commands registry */

    inline fun registerCommand(command: C) = commandRegistry.registerCommand(command)

    inline fun <T : C> unregisterCommand(commandClass: Class<T>) = commandRegistry.unregisterCommand(commandClass)

    inline fun <T> invokeCommand(sender: S, input: String) = commandInvoker.invokeCommand<T>(sender, input)

    @JvmName(" ")
    inline fun invokeCommand(sender: S, input: String) = commandInvoker.invokeCommand<Unit>(sender, input)

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

    inline fun registerParser(parser: ArgumentParser<*>) = processorRegistry.registerParser(parser)

    /* Validators registry */

    inline fun registerValidator(validator: ArgumentValidator<*, *, S>) = processorRegistry.registerValidator(validator)

    inline fun <T : Any, reified A : Annotation> registerValidator(crossinline validator: (T, A, S) -> Unit) {
        processorRegistry.registerValidator(object : ArgumentValidator<T, A, S> {
            override fun validate(sender: S, arg: T, annotation: A) = validator(arg, annotation, sender)
        }, A::class)
    }

    /* Exception handlers */

    inline fun <T : CommandInvocationException> registerExceptionHandler(handler: ExceptionHandler<T, S>) =
        processorRegistry.registerExceptionHandler(handler)

    inline fun <reified T : CommandInvocationException> registerExceptionHandler(crossinline handler: (T, S) -> Unit) {
        processorRegistry.registerExceptionHandler(object : ExceptionHandler<T, S> {
            override fun handle(exception: T, sender: S) = handler(exception, sender)
        }, T::class)
    }

    /* Suggestions providers */

    inline fun registerSuggestionProvider(vararg types: Class<*>, provider: SuggestionProvider<S>) =
        processorRegistry.registerSuggestionProvider(types = types, provider)
}
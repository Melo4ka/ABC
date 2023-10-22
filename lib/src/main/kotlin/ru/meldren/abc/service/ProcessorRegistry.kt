package ru.meldren.abc.service

import ru.meldren.abc.exception.*
import ru.meldren.abc.exception.invocation.CommandInvocationException
import ru.meldren.abc.processor.ArgumentParser
import ru.meldren.abc.processor.ExceptionHandler
import ru.meldren.abc.processor.PermissionHandler
import ru.meldren.abc.processor.SuggestionProvider
import ru.meldren.abc.processor.cooldown.CooldownHandler
import ru.meldren.abc.processor.validator.ArgumentValidator
import ru.meldren.abc.util.checkNotNullOrThrow
import ru.meldren.abc.util.checkNullOrThrow
import ru.meldren.abc.util.checkOrThrow
import ru.meldren.abc.util.supertypeTypeParameters
import kotlin.reflect.KClass

@PublishedApi
internal class ProcessorRegistry<S : Any, C : Any>(
    private val defaultParsers: MutableMap<KClass<*>, ArgumentParser<*>>,
    private val parsers: MutableMap<KClass<out ArgumentParser<*>>, ArgumentParser<*>>,
    private val validators: MutableMap<KClass<out Annotation>, ArgumentValidator<*, *>>,
    private val handlers: MutableMap<KClass<out CommandInvocationException>, ExceptionHandler<*, *>>,
    private val suggestions: MutableMap<KClass<out SuggestionProvider<S>>, SuggestionProvider<S>>
) {

    var permissionHandler: PermissionHandler<S, C>? = null
        private set
    var cooldownHandler: CooldownHandler<S, C>? = null
        private set

    fun registerPermissionHandler(handler: PermissionHandler<S, C>) {
        checkNullOrThrow(permissionHandler) {
            CommandRegistrationException("Permission handler is already registered.")
        }

        permissionHandler = handler
    }

    fun unregisterPermissionHandler() {
        checkNotNullOrThrow(permissionHandler) {
            CommandRegistrationException("Permission handler is not registered.")
        }

        permissionHandler = null
    }

    fun registerCooldownHandler(handler: CooldownHandler<S, C>) {
        checkNullOrThrow(cooldownHandler) {
            CommandRegistrationException("Cooldown handler is already registered.")
        }

        cooldownHandler = handler
    }

    fun unregisterCooldownHandler() {
        checkNotNullOrThrow(cooldownHandler) {
            CommandRegistrationException("Cooldown handler is not registered.")
        }

        cooldownHandler = null
    }

    fun registerDefaultParser(
        parser: ArgumentParser<*>,
        type: KClass<*> = parser::class.supertypeTypeParameters<ArgumentParser<*>>()[0]
    ) {
        checkOrThrow(type !in defaultParsers) {
            CommandRegistrationException("Default ${type.simpleName} parser is already registered.")
        }

        defaultParsers[type] = parser
        parsers[parser::class] = parser
    }

    fun unregisterDefaultParser(type: KClass<*>) {
        checkOrThrow(type in defaultParsers) {
            CommandRegistrationException("Default ${type.simpleName} parser is not registered.")
        }

        val parser = defaultParsers.remove(type)!!
        parsers.remove(parser::class)
    }

    fun registerParser(parser: ArgumentParser<*>) {
        val parserClass = parser::class

        checkOrThrow(parserClass !in parsers) {
            CommandRegistrationException("Parser ${parserClass.simpleName} is already registered.")
        }

        parsers[parserClass] = parser
    }

    fun unregisterParser(parserClass: KClass<out ArgumentParser<*>>) {
        checkOrThrow(parserClass in parsers) {
            CommandRegistrationException("Parser ${parserClass.simpleName} is not registered.")
        }

        val parser = parsers[parserClass]
        checkOrThrow(!defaultParsers.containsValue(parser)) {
            CommandRegistrationException("Default parsers must be unregistered via ${::unregisterDefaultParser.name}.")
        }

        parsers.remove(parserClass)
    }

    fun registerValidator(
        validator: ArgumentValidator<*, *>,
        annotationClass: KClass<*> = validator::class.supertypeTypeParameters<ArgumentValidator<*, *>>()[1]
    ) {
        checkOrThrow(annotationClass !in validators) {
            CommandRegistrationException("${annotationClass.simpleName} validator is already registered.")
        }

        @Suppress("UNCHECKED_CAST")
        validators[annotationClass as KClass<Annotation>] = validator
    }

    fun unregisterValidator(annotationClass: KClass<out Annotation>) {
        checkOrThrow(annotationClass in validators) {
            CommandRegistrationException("${annotationClass.simpleName} validator is not registered.")
        }

        validators.remove(annotationClass)
    }

    fun <T : CommandInvocationException, I : Any> registerExceptionHandler(
        handler: ExceptionHandler<T, I>,
        type: KClass<*> = handler::class.supertypeTypeParameters<ExceptionHandler<*, *>>()[0]
    ) {
        checkOrThrow(type !in handlers) {
            CommandRegistrationException("Exception handler for ${type.simpleName} is already registered.")
        }

        @Suppress("UNCHECKED_CAST")
        handlers[type as KClass<out CommandInvocationException>] = handler
    }

    fun unregisterExceptionHandler(type: KClass<out CommandException>) {
        checkOrThrow(type in handlers) {
            CommandRegistrationException("Exception handler for ${type.simpleName} is not registered.")
        }

        handlers.remove(type)
    }

    fun registerSuggestionProvider(provider: SuggestionProvider<S>) {
        val providerClass = provider::class

        checkOrThrow(providerClass !in suggestions) {
            CommandRegistrationException("Suggestion provider ${providerClass.simpleName} is already registered.")
        }

        suggestions[providerClass] = provider
    }

    fun unregisterSuggestionProvider(providerClass: KClass<out SuggestionProvider<S>>) {
        checkOrThrow(providerClass in suggestions) {
            CommandRegistrationException("Suggestion provider ${providerClass.simpleName} is not registered.")
        }

        suggestions.remove(providerClass)
    }
}
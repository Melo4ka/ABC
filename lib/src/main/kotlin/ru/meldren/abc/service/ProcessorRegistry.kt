package ru.meldren.abc.service

import ru.meldren.abc.exception.*
import ru.meldren.abc.exception.invocation.CommandInvocationException
import ru.meldren.abc.processor.*
import ru.meldren.abc.util.checkNotNullOrThrow
import ru.meldren.abc.util.checkNullOrThrow
import ru.meldren.abc.util.checkOrThrow
import ru.meldren.abc.util.supertypeTypeParameters
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@PublishedApi
internal class ProcessorRegistry<S : Any, C : Any>(
    private val defaultParsers: MutableMap<KClass<*>, ArgumentParser<*>>,
    private val parsers: MutableMap<KClass<out ArgumentParser<*>>, ArgumentParser<*>>,
    private val validators: MutableMap<KClass<out Annotation>, ArgumentValidator<*, *, S>>,
    private val handlers: MutableMap<KClass<out CommandInvocationException>, ExceptionHandler<*, *>>,
    private val defaultSuggestions: MutableMap<KClass<*>, SuggestionProvider<S>>,
    private val suggestions: MutableMap<KClass<out SuggestionProvider<S>>, SuggestionProvider<S>>
) {

    var permissionHandler: PermissionHandler<S>? = null
        private set
    var cooldownHandler: CooldownHandler<S, C>? = null
        private set

    fun registerPermissionHandler(handler: PermissionHandler<S>) {
        checkNullOrThrow(permissionHandler) {
            CommandRegistrationException("Permission handler is already registered.")
        }

        permissionHandler = handler
    }

    fun registerCooldownHandler(handler: CooldownHandler<S, C>) {
        checkNullOrThrow(cooldownHandler) {
            CommandRegistrationException("Cooldown handler is already registered.")
        }

        cooldownHandler = handler
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

    fun registerParser(parser: ArgumentParser<*>) {
        val parserClass = parser::class

        checkOrThrow(parserClass !in parsers) {
            CommandRegistrationException("Parser ${parserClass.simpleName} is already registered.")
        }

        parsers[parserClass] = parser
    }

    fun registerValidator(
        validator: ArgumentValidator<*, *, S>,
        annotationClass: KClass<*> = validator::class.supertypeTypeParameters<ArgumentValidator<*, *, S>>()[1]
    ) {
        checkOrThrow(annotationClass !in validators) {
            CommandRegistrationException("${annotationClass.simpleName} validator is already registered.")
        }

        @Suppress("UNCHECKED_CAST")
        validators[annotationClass as KClass<Annotation>] = validator
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

    fun registerSuggestionProvider(vararg types: Class<*>, provider: SuggestionProvider<S>) {
        types.forEach {
            checkOrThrow(it.kotlin !in defaultSuggestions) {
                CommandRegistrationException("Default suggestion provider for ${it.simpleName} is already registered.")
            }
        }

        val providerClass = provider::class

        checkOrThrow(providerClass !in suggestions) {
            CommandRegistrationException("Suggestion provider ${providerClass.simpleName} is already registered.")
        }

        suggestions[providerClass] = provider
        defaultSuggestions += types.associate { it.kotlin to provider }
    }
}
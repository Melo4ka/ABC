package ru.meldren.abc.service

import ru.meldren.abc.annotation.*
import ru.meldren.abc.common.CommandData
import ru.meldren.abc.common.CommandParameter
import ru.meldren.abc.common.SubcommandData
import ru.meldren.abc.exception.CommandRegistrationException
import ru.meldren.abc.processor.ArgumentParser
import ru.meldren.abc.util.*
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.*

@PublishedApi
internal class CommandRegistry<C : Any>(
    private val registeredCommands: MutableSet<CommandData<C>>,
    private val commandsByAliases: MutableMap<String, CommandData<C>>
) {

    fun registerCommand(command: C): CommandData<C> {
        return constructCommand(command).also {
            registeredCommands.add(it)
            it.aliases.forEach { alias ->
                commandsByAliases[alias.lowercase()] = it
            }
        }
    }

    private fun constructCommand(command: C): CommandData<C> {
        val commandClass = command::class
        checkClassFitsRequirements(commandClass)
        val aliases = getAliases(commandClass)
        val cooldown = commandClass.findAnnotation<Cooldown>()?.let {
            it.unit.toSeconds(it.value.toLong())
        } ?: 0
        val defaultSubcommands = getDefaultSubcommands(commandClass)
        val subcommands = getSubcommands(commandClass)
        checkOrThrow(subcommands.isNotEmpty() || defaultSubcommands.isNotEmpty()) {
            CommandRegistrationException("${commandClass.simpleName} does not have @${Subcommand::class.simpleName} or @${Default::class.simpleName} functions.")
        }
        val children = getChildren(command)
        val beforeCommands = getBeforeCommands(commandClass)
        return CommandData(
            aliases, cooldown, command,
            commandClass.annotations, defaultSubcommands,
            subcommands, children, beforeCommands
        )
    }

    private fun checkClassFitsRequirements(commandClass: KClass<*>) {
        checkOrThrow(commandClass.isPOJO()) {
            CommandRegistrationException("${commandClass.simpleName} must be POJO.")
        }

        val subcommandFunctions = commandClass.functions.filter { it.hasAnnotation<Subcommand>() }
        subcommandFunctions.forEach { function ->
            checkOrThrow(function.valueParameters.none { it.isVararg && it.index == function.valueParameters.size - 1 }) {
                CommandRegistrationException("Vararg must be the last parameter of function ${function.name} in ${commandClass.simpleName}.")
            }
            checkOrThrow(function.valueParameters.all {
                val annotation = it.findAnnotation<Parser>() ?: return@all true
                val (parserType) = annotation.parserClass.supertypeTypeParameters<ArgumentParser<*>>()
                val paramType = if (it.isVararg) it.arrayType() else it.type.classifier
                paramType == parserType
            }) {
                CommandRegistrationException("Parameters of function ${function.name} in ${commandClass.simpleName} must have the same type as parser.")
            }
        }
    }

    private fun getAliases(commandClass: KClass<*>): List<String> {
        val commandAnnotation = commandClass.findAnnotation<Command>()
        checkNotNullOrThrow(commandAnnotation) {
            CommandRegistrationException("${commandClass.simpleName} must be annotated with @${Command::class.simpleName}.")
        }

        val aliases = parseAliases(commandClass, commandAnnotation.name, commandAnnotation.aliases)
        checkOrThrow(registeredCommands.map { it.aliases }.none { it.intersect(aliases.toSet()).isNotEmpty() }) {
            CommandRegistrationException("${commandClass.simpleName} child aliases conflict with subcommand aliases.")
        }

        return aliases
    }

    private fun getBeforeCommands(commandClass: KClass<*>): List<KFunction<*>> {
        val beforeCommands = getAnnotatedFunctionsWithPriority<Before>(commandClass)

        checkOrThrow(beforeCommands.all { it.returnType.classifier == Boolean::class }) {
            CommandRegistrationException("${Before::class.simpleName} commands in ${commandClass.simpleName} do not return boolean.")
        }

        return beforeCommands
    }

    private fun getSubcommands(commandClass: KClass<*>): Map<List<String>, List<SubcommandData>> {
        val functions = getAnnotatedFunctionsWithPriority<Subcommand>(commandClass)

        val subcommands = mutableMapOf<List<String>, MutableList<SubcommandData>>()
        functions.forEach {
            val annotation = it.findAnnotation<Subcommand>()!!
            val aliases = parseAliases(commandClass, annotation.name, annotation.aliases)
            val data = constructSubcommand(it)
            subcommands.getOrPut(aliases, ::mutableListOf).add(data)
        }

        return subcommands.mapValues { it.value.toList() }.toMap()
    }

    private fun getDefaultSubcommands(commandClass: KClass<*>): List<SubcommandData> {
        val defaultFunctions = getAnnotatedFunctionsWithPriority<Default>(commandClass)
        return defaultFunctions.map(::constructSubcommand)
    }

    private fun constructSubcommand(function: KFunction<*>): SubcommandData {
        val params = function.valueParameters.filterNot { it.hasAnnotation<Sender>() }.map {
            CommandParameter((it.type.classifier!! as KClass<*>).java, it.annotations)
        }
        return SubcommandData(params, function.annotations, function)
    }

    private fun getChildren(command: C): List<CommandData<C>> {
        return command::class.nestedClasses.map {
            val constructor = it.constructors.firstOrNull()
            checkOrThrow(constructor != null && constructor.valueParameters.isEmpty()) {
                CommandRegistrationException("${it::class.simpleName} must have a single no-arg constructor.")
            }

            @Suppress("Unchecked_cast")
            val child = try {
                (if (it.isInner) constructor.call(command) else constructor.call()) as C
            } catch (ex: ClassCastException) {
                throw CommandRegistrationException("${it::class.simpleName} must implement global command interface: ${ex.message}")
            }
            constructCommand(child)
        }
    }

    private fun parseAliases(commandClass: KClass<*>, main: String, secondary: Array<out String>): List<String> {
        val aliases = (listOf(main) + secondary)
            .filter(String::isNotBlank)
            .map {
                it.lowercase().replace(' ', '_')
            }
            .distinct()

        checkOrThrow(aliases.isNotEmpty()) {
            CommandRegistrationException("${commandClass.simpleName} aliases are empty.")
        }

        return aliases
    }

    private inline fun <reified A : Annotation> getAnnotatedFunctionsWithPriority(commandClass: KClass<*>): List<KFunction<*>> {
        val functions = commandClass.declaredFunctions
            .filter { it.hasAnnotation<A>() }
            .sortedByDescending { it.findAnnotation<Priority>()?.value ?: 0u }

        functions.forEach {
            val uniqueAnnotationsNumber = it.annotations.count { annotation ->
                annotation is Subcommand || annotation is Default || annotation is Before
            }
            checkOrThrow(uniqueAnnotationsNumber <= 1) {
                CommandRegistrationException("${Subcommand::class.simpleName} function ${it.name} in ${commandClass.simpleName} contains invalid combination of annotations.")
            }

            checkOrThrow(it.valueParameters.count { it.hasAnnotation<Sender>() } <= 1) {
                CommandRegistrationException("${Subcommand::class.simpleName} function ${it.name} in ${commandClass.simpleName} has more than one @${Sender::class.simpleName} argument.")
            }
        }

        return functions
    }
}
package ru.meldren.abc.service

import ru.meldren.abc.annotation.Cooldown
import ru.meldren.abc.annotation.Parser
import ru.meldren.abc.annotation.Sender
import ru.meldren.abc.annotation.Suggest
import ru.meldren.abc.common.CommandData
import ru.meldren.abc.common.SubcommandData
import ru.meldren.abc.exception.*
import ru.meldren.abc.exception.invocation.*
import ru.meldren.abc.exception.processing.ArgumentParseException
import ru.meldren.abc.processor.ArgumentParser
import ru.meldren.abc.processor.ExceptionHandler
import ru.meldren.abc.processor.SuggestionProvider
import ru.meldren.abc.processor.validator.ArgumentValidator
import ru.meldren.abc.util.arrayType
import ru.meldren.abc.util.checkOrThrow
import ru.meldren.abc.util.toInt
import ru.meldren.abc.util.toTypedArray
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.*

@PublishedApi
internal class CommandInvoker<S : Any>(
    private val commandPrefix: String,
    private val processorRegistry: ProcessorRegistry<S>,
    private val registeredCommands: MutableSet<CommandData>,
    private val defaultParsers: MutableMap<KClass<*>, ArgumentParser<*>>,
    private val parsers: MutableMap<KClass<out ArgumentParser<*>>, ArgumentParser<*>>,
    private val validators: MutableMap<KClass<out Annotation>, ArgumentValidator<*, *>>,
    private val handlers: MutableMap<KClass<out CommandInvocationException>, ExceptionHandler<*, *>>,
    private val suggestions: MutableMap<KClass<out SuggestionProvider<S>>, SuggestionProvider<S>>
) {

    @Suppress("UNCHECKED_CAST")
    fun <T> invokeCommand(sender: S, input: String): T? {
        return try {
            val args = parseInput(input)
            val rawArgs = args.toList()
            val commandData = findCommand(args, rawArgs)
            val subcommandsData = findSubcommands(args, rawArgs, commandData)
            val (subcommandData, params) = findSubcommand(sender, args, subcommandsData)

            if (!checkPermission(sender, commandData, subcommandData)) {
                throw NotEnoughPermissionException(rawArgs, commandData, subcommandData)
            }

            if (!checkBeforeCommands(sender, commandData)) {
                throw BeforeCommandFailException(rawArgs, commandData, subcommandData)
            }

            val cooldown = checkCooldown(sender, commandData)
            if (cooldown > 0) {
                throw CommandCooldownException(cooldown)
            }

            callSubcommand(commandData, subcommandData, params) as T?
        } catch (ex: CommandInvocationException) {
            (handlers[ex::class] as? ExceptionHandler<CommandInvocationException, S>)?.handle(ex, sender)
            null
        }
    }

    fun generateSuggestions(sender: S, input: String): List<String> {
        try {
            val args = parseInput(input)
            if (args.all(String::isBlank)) {
                return registeredCommands.flatMap(CommandData::aliases)
            }
            val commandData = findCommand(args, args)
            val subcommandsData = try {
                findSubcommands(args, args, commandData)
            } catch (ex: IncorrectArgumentCountException) {
                ex.subcommandsData
            }

            val suggestions = mutableListOf<String>()
            subcommandsData.forEach { subcommandData ->
                val param = subcommandData.parameters[args.size - 1]
                val annotation = param.annotations.find { it is Suggest } ?: return@forEach
                val id = (annotation as Suggest).providerClass
                val generatedSuggestions = this.suggestions[id]?.suggest(sender, param) ?: return@forEach
                val unfinishedSuggestions = if (args.isNotEmpty()) generatedSuggestions.filter {
                    it.startsWith(args.last())
                } else emptyList()
                suggestions += unfinishedSuggestions.ifEmpty { generatedSuggestions }
            }
            return suggestions.distinct()
        } catch (ex: Exception) {
            return emptyList()
        }
    }

    private fun parseInput(input: String): MutableList<String> {
        val handledInput = input.trim()
        if (!handledInput.startsWith(commandPrefix)) {
            throw CommandPlainTextException()
        }
        return handledInput
            .removePrefix(commandPrefix)
            .split("\\s+".toRegex())
            .toMutableList()
    }

    private fun findCommand(args: MutableList<String>, rawArgs: List<String>): CommandData {
        var command = registeredCommands.find { args.getOrNull(0)?.lowercase() in it.aliases }
            ?: throw CommandNotFoundException(rawArgs)
        var i = 0
        for (j in 1 until args.size) {
            val alias = args[j].lowercase()
            command.children.find { alias in it.aliases }?.let {
                command = it
                i = j
            } ?: break
        }
        repeat(i + 1) { args.removeFirst() }
        return command
    }

    private fun findSubcommands(
        args: MutableList<String>,
        rawArgs: List<String>,
        commandData: CommandData
    ): List<SubcommandData> {
        val alias = args.getOrNull(0)?.lowercase()
        var subcommands = commandData.subcommands.entries.find { (aliases, _) ->
            alias in aliases
        }?.value
        if (subcommands == null) {
            subcommands = commandData.defaultSubcommands.toMutableList()
        } else {
            args.removeFirst()
        }

        if (subcommands.isEmpty()) {
            throw SubcommandNotFoundException(rawArgs, commandData)
        }

        val incorrectSubcommands = subcommands.filter { subcommand ->
            val params = subcommand.function.valueParameters

            val hasSender = params.any { it.hasAnnotation<Sender>() }
            val hasVararg = params.lastOrNull()?.isVararg ?: false

            val argsNumber = args.size
            val requiredArgsNumber = params.size - hasSender.toInt()
            argsNumber != requiredArgsNumber && !(hasVararg && argsNumber >= requiredArgsNumber)
        }

        if (subcommands.size == incorrectSubcommands.size) {
            throw IncorrectArgumentCountException(rawArgs, commandData, incorrectSubcommands)
        }

        return subcommands - incorrectSubcommands.toSet()
    }

    private fun findSubcommand(
        sender: S,
        args: MutableList<String>,
        subcommandsData: List<SubcommandData>
    ): Pair<SubcommandData, Array<Any>> {
        lateinit var exception: ArgumentParseException
        for (i in subcommandsData.indices) {
            try {
                val subcommandData = subcommandsData[i]
                val params = parseSubcommandArguments(sender, args, subcommandData)
                return subcommandData to params.map { it.first }.toTypedArray()
            } catch (ex: ArgumentParseException) {
                exception = ex
            }
        }
        throw exception
    }

    private fun parseSubcommandArguments(
        sender: S,
        args: MutableList<String>,
        subcommandData: SubcommandData
    ): MutableList<Pair<Any, List<Annotation>>> {
        val params: MutableList<Pair<Any, List<Annotation>>> = args
            .map { it to emptyList<Annotation>() }.toMutableList()

        val functionParams = subcommandData.function.valueParameters

        addSenderArgToParams(params, functionParams, sender)

        val hasVararg = functionParams.lastOrNull()?.isVararg ?: false
        val commonArgsNumber = functionParams.size - hasVararg.toInt()
        addCommonArgsToParams(params, functionParams, commonArgsNumber)

        validateParams(params)

        if (hasVararg) {
            addVarargToParams(params, functionParams)
        }

        return params
    }

    private fun validateParams(params: List<Pair<Any, List<Annotation>>>) {
        params.forEach { (param, annotations) ->
            annotations.forEach annotation@{ annotation ->
                val validator = validators[annotation.annotationClass] ?: return@annotation
                try {
                    @Suppress("UNCHECKED_CAST")
                    (validator as ArgumentValidator<Any, Annotation>).validate(param, annotation)
                } catch (_: ClassCastException) {
                    throw CommandException("@${annotation.annotationClass.simpleName} validator is not compatible with ${param::class.simpleName}.")
                }
            }
        }
    }

    private fun addSenderArgToParams(
        params: MutableList<Pair<Any, List<Annotation>>>,
        functionParams: List<KParameter>,
        sender: S
    ) {
        val senderArgIndex = functionParams.indexOfFirst { it.hasAnnotation<Sender>() }
        if (senderArgIndex != -1) {
            val paramData = sender to functionParams[senderArgIndex].annotations
            params.add(senderArgIndex, paramData)
        }
    }

    private fun addCommonArgsToParams(
        params: MutableList<Pair<Any, List<Annotation>>>,
        functionParams: List<KParameter>,
        number: Int
    ) {
        for (index in 0 until number) {
            val parameter = functionParams[index]
            if (parameter.hasAnnotation<Sender>()) {
                continue
            }
            val parser = getParser(parameter)
            val param = params[index].first.toString()
            val parsedParam = parser?.parse(param) ?: param
            val paramData = parsedParam to parameter.annotations
            params[index] = paramData
        }
    }

    private fun addVarargToParams(params: MutableList<Pair<Any, List<Annotation>>>, functionParams: List<KParameter>) {
        val varargParam = functionParams.last()
        val arrayType = varargParam.arrayType()!!
        val parser = getParser(varargParam, arrayType)

        val array = mutableListOf<Any>()
        for (index in functionParams.size - 1 until params.size) {
            val param = params[index].first.toString()
            val parsedParam = parser?.parse(param) ?: param
            array.add(parsedParam)
        }

        validateParams(array.map { it to varargParam.annotations })

        val sizeOfVarargs = params.size - functionParams.size + 1
        repeat(sizeOfVarargs) { params.removeLast() }

        val paramData = array.toTypedArray(arrayType) to varargParam.annotations
        params.add(paramData)
    }

    private fun getParser(parameter: KParameter, type: KClassifier? = parameter.type.classifier): ArgumentParser<*>? {
        val annotation = parameter.findAnnotation<Parser>()
        return if (annotation == null) {
            if (type == String::class && type !in defaultParsers) {
                return null
            }
            checkOrThrow(type in defaultParsers) {
                type as KClass<*>
                CommandException("Default ${type.simpleName} parser is not registered.")
            }
            defaultParsers[type]
        } else {
            checkOrThrow(annotation.parserClass in parsers) {
                CommandException("Parser ${annotation.parserClass.simpleName} must be registered before using.")
            }
            parsers[annotation.parserClass]
        }
    }

    private fun callSubcommand(commandData: CommandData, subcommandData: SubcommandData, params: Array<Any>): Any? {
        return try {
            subcommandData.function.call(commandData.instance, *params)
        } catch (ex: ClassCastException) {
            throw CommandException("Requested return type is not compatible with function ${subcommandData.function.name} in ${commandData.instance::class.simpleName}.")
        }
    }

    private fun checkPermission(sender: S, commandData: CommandData, subcommandData: SubcommandData): Boolean {
        val permissionHandler = processorRegistry.permissionHandler ?: return true
        return (commandData.permission == null || permissionHandler.hasPermission(sender, commandData.permission)) &&
                (subcommandData.permission == null || permissionHandler.hasPermission(
                    sender,
                    subcommandData.permission
                ))
    }

    private fun checkBeforeCommands(sender: S, commandData: CommandData): Boolean {
        return commandData.beforeCommands.all {
            val params = mutableListOf(commandData.instance)
            if (it.valueParameters.isNotEmpty()) {
                params.add(sender)
            }
            it.call(*params.toTypedArray()) as Boolean
        }
    }

    private fun checkCooldown(sender: S, commandData: CommandData): Long {
        val annotation = commandData.instance::class.findAnnotation<Cooldown>() ?: return 0
        val left = processorRegistry.cooldownHandler?.test(sender, commandData, annotation)
        return left ?: 0
    }
}
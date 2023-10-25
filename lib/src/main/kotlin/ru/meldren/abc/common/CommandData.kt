package ru.meldren.abc.common

import kotlin.reflect.KFunction

class CommandData<C : Any> internal constructor(
    val aliases: List<String>,
    val cooldownInSeconds: Long,
    val instance: C,
    override val annotations: List<Annotation>,
    val defaultSubcommands: List<SubcommandData>,
    val subcommands: Map<List<String>, List<SubcommandData>>,
    val children: List<CommandData<C>>,
    internal val beforeCommands: List<KFunction<*>>
) : AbstractCommandData() {

    internal val subcommandByAliases = mutableMapOf<String, List<SubcommandData>>()
    internal val childByAliases = mutableMapOf<String, CommandData<C>>()

    init {
        subcommands.forEach { (aliases, subcommandsData) ->
            aliases.forEach { alias ->
                subcommandByAliases[alias] = subcommandsData
            }
        }
        children.forEach { commandData ->
            commandData.aliases.forEach { alias ->
                childByAliases[alias] = commandData
            }
        }
    }
}
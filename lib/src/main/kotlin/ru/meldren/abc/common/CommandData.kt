package ru.meldren.abc.common

import kotlin.reflect.KFunction

data class CommandData internal constructor(
    val aliases: List<String>,
    val description: String?,
    val permission: String?,
    val instance: Any,
    val defaultSubcommands: List<SubcommandData>,
    val subcommands: Map<List<String>, List<SubcommandData>>,
    val children: List<CommandData>,
    internal val beforeCommands: List<KFunction<*>>
)
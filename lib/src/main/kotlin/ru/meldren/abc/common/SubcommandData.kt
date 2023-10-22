package ru.meldren.abc.common

import kotlin.reflect.KFunction

data class SubcommandData internal constructor(
    val description: String?,
    val syntax: String?,
    val parameters: List<CommandParameter>,
    internal val function: KFunction<*>
)
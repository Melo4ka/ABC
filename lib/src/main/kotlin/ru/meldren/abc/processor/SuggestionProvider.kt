package ru.meldren.abc.processor

import ru.meldren.abc.common.CommandParameter

fun interface SuggestionProvider<S : Any> {

    fun suggest(sender: S, input: String, parameter: CommandParameter): List<String>
}
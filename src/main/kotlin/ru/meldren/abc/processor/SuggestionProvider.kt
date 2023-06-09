package ru.meldren.abc.processor

import ru.meldren.abc.common.CommandParameter

fun interface SuggestionProvider<S : Any> {

    fun suggest(sender: S, parameter: CommandParameter): List<String>
}
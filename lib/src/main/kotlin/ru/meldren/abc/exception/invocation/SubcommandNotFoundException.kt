package ru.meldren.abc.exception.invocation

import ru.meldren.abc.common.CommandData

class SubcommandNotFoundException(
    args: List<String>,
    val commandData: CommandData
) : CommandNotFoundException(args)
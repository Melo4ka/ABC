package ru.meldren.abc.exception.invocation

import ru.meldren.abc.common.CommandData
import ru.meldren.abc.common.SubcommandData

class BeforeCommandFailException(
    val args: List<String>,
    val commandData: CommandData<*>,
    val subcommandData: SubcommandData
) : CommandInvocationException()
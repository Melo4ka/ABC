package ru.meldren.abc.exception.invocation

import ru.meldren.abc.common.CommandData
import ru.meldren.abc.common.SubcommandData

class IncorrectArgumentCountException(
    val args: List<String>,
    val commandData: CommandData<*>,
    val subcommandsData: List<SubcommandData>
) : CommandInvocationException()
package ru.meldren.abc.exception.invocation

import ru.meldren.abc.common.CommandData
import ru.meldren.abc.common.SubcommandData
import ru.meldren.abc.exception.CommandInvocationException

class IncorrectArgumentCountException(
    val args: List<String>,
    val commandData: CommandData,
    val subcommandsData: List<SubcommandData>
) : CommandInvocationException()
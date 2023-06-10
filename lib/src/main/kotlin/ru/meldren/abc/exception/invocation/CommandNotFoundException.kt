package ru.meldren.abc.exception.invocation

import ru.meldren.abc.exception.CommandInvocationException

open class CommandNotFoundException(
    val args: List<String>
) : CommandInvocationException()
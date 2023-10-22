package ru.meldren.abc.exception.invocation

open class CommandNotFoundException(
    val args: List<String>
) : CommandInvocationException()
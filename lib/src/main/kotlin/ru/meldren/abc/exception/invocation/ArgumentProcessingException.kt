package ru.meldren.abc.exception.invocation

import ru.meldren.abc.exception.invocation.CommandInvocationException

sealed class ArgumentProcessingException(override val message: String) : CommandInvocationException()
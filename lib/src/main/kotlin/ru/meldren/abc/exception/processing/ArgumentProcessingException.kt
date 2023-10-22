package ru.meldren.abc.exception.processing

import ru.meldren.abc.exception.invocation.CommandInvocationException

open class ArgumentProcessingException(override val message: String) : CommandInvocationException()
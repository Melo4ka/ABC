package ru.meldren.abc.exception.invocation

sealed class ArgumentProcessingException(override val message: String) : CommandInvocationException()
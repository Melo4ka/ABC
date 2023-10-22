package ru.meldren.abc.exception.invocation

class CommandCooldownException(val millisecondsLeft: Long) : CommandInvocationException()
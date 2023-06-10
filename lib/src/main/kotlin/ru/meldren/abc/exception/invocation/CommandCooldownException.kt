package ru.meldren.abc.exception.invocation

import ru.meldren.abc.exception.CommandInvocationException

class CommandCooldownException(val millisecondsLeft: Long) : CommandInvocationException()
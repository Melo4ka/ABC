package ru.meldren.abc.exception.invocation

class NotEnoughPermissionException(override val message: String) : CommandInvocationException()
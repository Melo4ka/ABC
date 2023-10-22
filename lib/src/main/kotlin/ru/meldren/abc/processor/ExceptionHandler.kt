package ru.meldren.abc.processor

import ru.meldren.abc.exception.invocation.CommandInvocationException

interface ExceptionHandler<in T : CommandInvocationException, in S : Any> {

    fun handle(exception: T, sender: S)
}
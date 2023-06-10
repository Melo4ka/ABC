package ru.meldren.abc.processor

import ru.meldren.abc.exception.CommandInvocationException

interface ExceptionHandler<in T : CommandInvocationException, in S : Any> {

    fun handle(exception: T, sender: S)
}
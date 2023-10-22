package ru.meldren.abc.processor.validator

import ru.meldren.abc.exception.processing.ArgumentParseException
import ru.meldren.abc.exception.processing.ArgumentValidationException
import kotlin.jvm.Throws

interface ArgumentValidator<in S : Any, in T : Any, in A : Annotation> {

    @Throws(ArgumentValidationException::class)
    fun validate(sender: S, arg: T, annotation: A)
}
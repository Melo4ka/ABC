package ru.meldren.abc.processor.validator

import ru.meldren.abc.exception.processing.ArgumentParseException
import ru.meldren.abc.exception.processing.ArgumentValidationException
import kotlin.jvm.Throws

interface ArgumentValidator<in T : Any, in A : Annotation> {

    @Throws(ArgumentValidationException::class)
    fun validate(arg: T, annotation: A)
}
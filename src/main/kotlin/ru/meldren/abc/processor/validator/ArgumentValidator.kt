package ru.meldren.abc.processor.validator

interface ArgumentValidator<in T : Any, in A : Annotation> {

    fun validate(arg: T, annotation: A)
}
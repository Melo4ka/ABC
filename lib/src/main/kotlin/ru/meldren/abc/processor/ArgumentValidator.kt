package ru.meldren.abc.processor

interface ArgumentValidator<in S : Any, in T : Any, in A : Annotation> {

    fun validate(sender: S, arg: T, annotation: A)
}
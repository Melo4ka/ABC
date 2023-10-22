package ru.meldren.abc.processor

interface ArgumentValidator<in T : Any, in A : Annotation, in S : Any> {

    fun validate(sender: S, arg: T, annotation: A)
}
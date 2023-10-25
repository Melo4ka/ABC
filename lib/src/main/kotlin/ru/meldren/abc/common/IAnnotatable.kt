package ru.meldren.abc.common

interface IAnnotatable {

    val annotations: List<Annotation>

    @Suppress("UNCHECKED_CAST")
    fun <T : Annotation> findAnnotation(annotationClass: Class<T>) =
        annotations.find { it.annotationClass.java == annotationClass } as? T
}

inline fun <reified T : Annotation> IAnnotatable.findAnnotation() = annotations.firstOrNull { it is T } as T?
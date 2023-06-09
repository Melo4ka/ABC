package ru.meldren.abc.common

import kotlin.reflect.KClass

data class CommandParameter internal constructor(
    val type: KClass<*>,
    val annotations: List<Annotation>
)
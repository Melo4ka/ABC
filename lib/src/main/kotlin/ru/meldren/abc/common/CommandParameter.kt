package ru.meldren.abc.common

import ru.meldren.abc.annotation.Description

class CommandParameter internal constructor(
    val type: Class<*>,
    override val annotations: List<Annotation>
) : IAnnotatable
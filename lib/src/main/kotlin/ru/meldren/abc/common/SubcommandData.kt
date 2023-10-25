package ru.meldren.abc.common

import ru.meldren.abc.annotation.Syntax
import kotlin.reflect.KFunction

class SubcommandData internal constructor(
    val parameters: List<CommandParameter>,
    override val annotations: List<Annotation>,
    internal val function: KFunction<*>
) : AbstractCommandData() {

    val syntax by lazy { findAnnotation<Syntax>()?.syntax }
}
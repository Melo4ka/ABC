package ru.meldren.abc.processor.cooldown

import ru.meldren.abc.annotation.Cooldown
import ru.meldren.abc.common.CommandData

fun interface CooldownHandler<S : Any> {

    fun test(sender: S, commandData: CommandData, annotation: Cooldown): Long
}
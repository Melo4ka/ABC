package ru.meldren.abc.processor.cooldown

import ru.meldren.abc.annotation.Cooldown
import ru.meldren.abc.common.CommandData

fun interface CooldownHandler<S : Any, C : Any> {

    fun test(sender: S, commandData: CommandData<C>, annotation: Cooldown): Long
}
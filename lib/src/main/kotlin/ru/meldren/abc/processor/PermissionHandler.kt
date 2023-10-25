package ru.meldren.abc.processor

import ru.meldren.abc.common.AbstractCommandData
import ru.meldren.abc.common.CommandData
import ru.meldren.abc.common.SubcommandData

fun interface PermissionHandler<S : Any> {

    fun checkPermission(sender: S, commandData: AbstractCommandData)
}
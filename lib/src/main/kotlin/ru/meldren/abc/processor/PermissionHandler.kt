package ru.meldren.abc.processor

fun interface PermissionHandler<S : Any, C : Any> {

    fun checkPermission(command: C, sender: S)
}
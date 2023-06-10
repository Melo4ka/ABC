package ru.meldren.abc.processor

fun interface PermissionHandler<S : Any> {

    fun hasPermission(sender: S, permission: String): Boolean
}
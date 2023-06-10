package ru.meldren.abc.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
internal inline fun checkOrThrow(value: Boolean, lazyException: () -> Exception) {
    contract {
        returns() implies value
    }
    if (!value) {
        throw lazyException()
    }
}

internal inline fun checkNullOrThrow(value: Any?, lazyException: () -> Exception) {
    checkOrThrow(value == null, lazyException)
}

@OptIn(ExperimentalContracts::class)
internal inline fun checkNotNullOrThrow(value: Any?, lazyException: () -> Exception) {
    contract {
        returns() implies (value != null)
    }
    checkOrThrow(value != null, lazyException)
}
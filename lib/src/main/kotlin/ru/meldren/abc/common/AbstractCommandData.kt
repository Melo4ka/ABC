package ru.meldren.abc.common

import ru.meldren.abc.annotation.Description

sealed class AbstractCommandData : IAnnotatable {

    val description by lazy { findAnnotation<Description>()?.description }
}
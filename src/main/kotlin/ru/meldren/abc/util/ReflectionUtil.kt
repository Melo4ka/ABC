package ru.meldren.abc.util

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.allSupertypes

internal fun Collection<*>.toTypedArray(type: KClass<*>): Any {
    val arrayType = if (type.java.isPrimitive) type.javaPrimitiveType else type.javaObjectType
    val javaArray = java.lang.reflect.Array.newInstance(arrayType, size)
    forEachIndexed { index, element ->
        java.lang.reflect.Array.set(javaArray, index, element)
    }
    return javaArray
}

internal fun KParameter.arrayType() = (type.classifier as KClass<*>).java.componentType?.kotlin

internal fun KClass<*>.isPOJO() = java.run {
    !isInterface && !isAnnotation && !isEnum && !isPrimitive && !isArray && superclass == Any::class.java
}

internal fun KClass<*>.supertypeTypeParameters(supertypeClass: KClass<*>): List<KClass<*>> {
    val supertype = allSupertypes.firstOrNull { it.classifier == supertypeClass }
    require(supertype != null) {
        "Class does not have specified supertype."
    }
    try {
        return supertype.arguments.mapNotNull { it.type?.classifier?.let { it as KClass<*> } }
    } catch (ex: ClassCastException) {
        throw IllegalArgumentException("Could not extract type parameters of $simpleName. Create a class with defined type parameters.")
    }
}

internal inline fun <reified T> KClass<*>.supertypeTypeParameters() = supertypeTypeParameters(T::class)

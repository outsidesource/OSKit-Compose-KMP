package com.outsidesource.oskitcompose.lib

import kotlin.math.absoluteValue


fun Int.snapTo(value: Int): Int {
    require(value > 0f)
    val diff = (this % value).absoluteValue
    return if (this > 0) {
        if (diff >= (value / 2)) this + (value - diff) else this - diff
    } else {
        if (diff >= (value / 2)) this - (value - diff) else this + diff
    }
}

fun Float.snapTo(value: Float): Float {
    require(value > 0f)
    val diff = (this % value).absoluteValue
    return if (this > 0) {
        if (diff >= (value / 2)) this + (value - diff) else this - diff
    } else {
        if (diff >= (value / 2)) this - (value - diff) else this + diff
    }
}

operator fun ClosedRange<Float>.times(value: Float) = (start * value)..(endInclusive * value)
operator fun ClosedRange<Float>.div(value: Float) = (start / value)..(endInclusive / value)

fun <T> List<T>.containsAny(list: List<T>) = any { it in list }
fun <T> List<T>.containsAny(vararg list: T) = any { it in list }
fun <T> List<T>.containsAll(vararg list: T) = containsAll(list.toList())

inline fun <reified R> Iterable<*>.filterIsInstance(predicate: (R) -> Boolean): List<R> = filterIsInstanceTo(mutableListOf(), predicate)

inline fun <reified R, C : MutableCollection<in R>> Iterable<*>.filterIsInstanceTo(destination: C, predicate: (R) -> Boolean): C {
    for (element in this) if (element is R && predicate(element)) destination.add(element)
    return destination
}

fun <T> List<List<T>>.intersect(): List<T> {
    if (this.isEmpty()) return listOf()
    var res = this[0].toSet()
    forEach { res = res.intersect(it.toSet()) }
    return res.toList()
}
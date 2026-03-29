package net.dodian.util.math

import kotlin.jvm.JvmName

class Range(
    private val floorRaw: Int,
    private val ceilingRaw: Int,
) {
    fun getFloor(): Int = floorRaw

    @get:JvmName("getFloorValue")
    val floor: Int
        get() = getFloor()

    fun getCeiling(): Int = ceilingRaw

    @get:JvmName("getCeilingValue")
    val ceiling: Int
        get() = getCeiling()

    @get:JvmName("getValueProperty")
    val value: Int
        get() = getValue()

    fun getValue(): Int = floorRaw + (Math.random() * (ceilingRaw - floorRaw + 1)).toInt()
}

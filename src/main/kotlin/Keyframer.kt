package org.operndr.extra.keyframer

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.openrndr.color.ColorRGBa
import org.openrndr.extras.easing.Easing
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.math.Vector4
import java.io.File
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

open class Keyframer {
    private var currentTime = 0.0
    operator fun invoke(time: Double) {
        currentTime = time
    }

    open inner class CompoundChannel(val keys: Array<String>, private val defaultValues: Array<Double>) {
        private var channelTimes: Array<Double> = Array(keys.size) { Double.NEGATIVE_INFINITY }
        private var compoundChannels: Array<KeyframerChannel?> = Array(keys.size) { null }
        private var cachedValues: Array<Double?> = Array(keys.size) { null }

        open fun reset() {
            for (i in channelTimes.indices) {
                channelTimes[i] = Double.NEGATIVE_INFINITY
            }
        }

        fun getValue(compound: Int): Double {
            if (compoundChannels[compound] == null) {
                compoundChannels[compound] = channels[keys[compound]]
            }
            return if (compoundChannels[compound] != null) {
                if (channelTimes[compound] == currentTime && cachedValues[compound] != null) {
                    cachedValues[compound] ?: defaultValues[compound]
                } else {
                    val value = compoundChannels[compound]?.value(currentTime) ?: defaultValues[compound]
                    cachedValues[compound] = value
                    value
                }
            } else {
                defaultValues[compound]
            }
        }
    }

    inner class DoubleChannel(key: String, defaultValue: Double = 0.0) :
        CompoundChannel(arrayOf(key), arrayOf(defaultValue)) {
        operator fun getValue(keyframer: Keyframer, property: KProperty<*>): Double = getValue(0)
    }

    inner class Vector2Channel(keys: Array<String>, defaultValue: Vector2 = Vector2.ZERO) :
        CompoundChannel(keys, arrayOf(defaultValue.x, defaultValue.y)) {
        operator fun getValue(keyframer: Keyframer, property: KProperty<*>): Vector2 = Vector2(getValue(0), getValue(1))
    }

    inner class Vector3Channel(keys: Array<String>, defaultValue: Vector3 = Vector3.ZERO) :
        CompoundChannel(keys, arrayOf(defaultValue.x, defaultValue.y, defaultValue.z)) {
        operator fun getValue(keyframer: Keyframer, property: KProperty<*>): Vector3 =
            Vector3(getValue(0), getValue(1), getValue(2))
    }

    inner class Vector4Channel(keys: Array<String>, defaultValue: Vector4 = Vector4.ZERO) :
        CompoundChannel(keys, arrayOf(defaultValue.x, defaultValue.y, defaultValue.z, defaultValue.w)) {
        operator fun getValue(keyframer: Keyframer, property: KProperty<*>): Vector4 =
            Vector4(getValue(0), getValue(1), getValue(2), getValue(3))
    }

    inner class RGBaChannel(keys: Array<String>, defaultValue: ColorRGBa = ColorRGBa.WHITE) :
        CompoundChannel(keys, arrayOf(defaultValue.r, defaultValue.g, defaultValue.b, defaultValue.a)) {
        operator fun getValue(keyframer: Keyframer, property: KProperty<*>): ColorRGBa =
            ColorRGBa(getValue(0), getValue(1), getValue(2), getValue(3))
    }

    inner class RGBChannel(keys: Array<String>, defaultValue: ColorRGBa = ColorRGBa.WHITE) :
        CompoundChannel(keys, arrayOf(defaultValue.r, defaultValue.g, defaultValue.b)) {
        operator fun getValue(keyframer: Keyframer, property: KProperty<*>): ColorRGBa =
            ColorRGBa(getValue(0), getValue(1), getValue(2))
    }

    val channels = mutableMapOf<String, KeyframerChannel>()

    fun loadFromJson(file: File) {
        val type = object : TypeToken<List<MutableMap<String, Any>>>() {}.type
        val keys: List<MutableMap<String, Any>> = Gson().fromJson(file.readText(), type)
        loadFromObjects(keys)
    }

    fun loadFromObjects(keys: List<Map<String, Any>>) {
        var lastTime = 0.0

        val channelDelegates = this::class.memberProperties
            .mapNotNull { it as? KProperty1<Keyframer, Any> }
            .filter { it.isAccessible = true; it.getDelegate(this) is CompoundChannel }
            .associate { Pair(it.name, it.getDelegate(this) as CompoundChannel) }

        val channelKeys = channelDelegates.values.flatMap {
            it.keys.map { it }
        }.toSet()

        for (delegate in channelDelegates.values) {
            delegate.reset()
        }

        for (key in keys) {
            val time = when (val timeCandidate = key["time"]) {
                null -> lastTime
                is String -> timeCandidate.toDoubleOrNull()
                    ?: error { "unknown value format for time : $timeCandidate" }
                is Double -> timeCandidate
                is Int -> timeCandidate.toDouble()
                is Float -> timeCandidate.toDouble()
                else -> error("unknown time format for '$timeCandidate'")
            }

            val easing = when (val easingCandidate = key["easing"]) {
                null -> Easing.Linear.function
                is String -> when (easingCandidate) {
                    "linear" -> Easing.Linear.function
                    "cubic-in" -> Easing.CubicIn.function
                    "cubic-out" -> Easing.CubicOut.function
                    "cubic-in-out" -> Easing.CubicInOut.function
                    else -> error { "unknown easing name '$easingCandidate" }
                }
                else -> error { "unknown easing for '$easingCandidate" }
            }

            val holdCandidate = key["hold"]
            val hold = Hold.HoldNone

            val reservedKeys = setOf("time", "easing", "hold")

            for (channelCandidate in key.filter { it.key !in reservedKeys }) {
                if (channelCandidate.key in channelKeys) {
                    val channel = channels.getOrPut(channelCandidate.key) {
                        KeyframerChannel()
                    }
                    val value = when (val valueCandidate = channelCandidate.value) {
                        is Double -> valueCandidate
                        is String -> valueCandidate.toDoubleOrNull()
                            ?: error { "unknown value format for key '${channelCandidate.key}' : $valueCandidate" }
                        is Int -> valueCandidate.toDouble()
                        else -> error { "unknown value type for key '${channelCandidate.key}' : $valueCandidate" }
                    }
                    channel.add(time, value, easing, hold)
                }
            }
            lastTime = time
        }
    }
}

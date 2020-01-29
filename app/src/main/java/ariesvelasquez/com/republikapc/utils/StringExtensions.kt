package ariesvelasquez.com.republikapc.utils

inline fun String.translateBack() : String = with(Regex("([abcdfghjklmnpqrstvwxz])o\\1", RegexOption.IGNORE_CASE)) {
    replace(this) { "${it.value.first()}" }
}
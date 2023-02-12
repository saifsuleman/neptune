package net.saifs.neptune.util

import java.text.DecimalFormat

private val DECIMAL_FORMAT = DecimalFormat("#,###")

fun formatNumber(number: Number): String {
    return DECIMAL_FORMAT.format(number)
}
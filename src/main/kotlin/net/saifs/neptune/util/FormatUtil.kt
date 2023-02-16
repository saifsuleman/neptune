package net.saifs.neptune.util

import java.text.DecimalFormat

private val DECIMAL_FORMAT = DecimalFormat("#,###.00")

fun formatNumber(number: Number): String {
    return DECIMAL_FORMAT.format(number)
}
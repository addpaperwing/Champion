package com.zzy.champions.data.local

import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal.roundToInt(): Int {
    return this.setScale(0, RoundingMode.HALF_UP).toInt()
}

fun BigDecimal.roundToFloat(): Float {
    return if (this.scale() > 2) {
        this.setScale(2, RoundingMode.HALF_UP).toFloat()
    } else {
        this.toFloat()
    }
}

fun BigDecimal.adaptiveScale(): BigDecimal {
    val rounded = if (this.scale() > 2) {
        this.setScale(2, RoundingMode.HALF_UP)
    } else {
        this
    }
//
//    int scale = noZero.scale();
//int precision = noZero.precision();
//if (scale < 0) { // Adjust for negative scale
//    precision -= scale;
//    scale = 0;
//}

    val stz = rounded.stripTrailingZeros()
    val scale = stz.scale()

    return if (scale < 0) {
        this.setScale(0, RoundingMode.HALF_UP)
    } else {
        stz
    }
}
package pl.llp.aircasting.util.extensions

import java.util.*

fun Calendar.addHours(time: Date, hours: Int): Date {
    this.time = time
    add(Calendar.HOUR_OF_DAY, hours)
    return this.time
}

fun calendar(): Calendar = Calendar.getInstance()
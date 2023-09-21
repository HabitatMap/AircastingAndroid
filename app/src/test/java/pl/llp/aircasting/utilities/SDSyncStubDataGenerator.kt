package pl.llp.aircasting.utilities

import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object SDSyncStubDataGenerator {
    fun generateSDSyncABData(
        uuid: String,
        deviceType: DeviceItem.Type,
        amount: Int,
        timeUnit: TimeUnit,
        date: LocalDateTime? = null,
    ): String {
        var date = date ?: LocalDateTime.now()
        val secondsOfGenerating = timeUnit.toSeconds(amount.toLong()).toInt()
        val resultStringBuilder = StringBuilder()
        for (i: Int in 1..secondsOfGenerating) {
            date = date.plusSeconds(1)

            resultStringBuilder.append(nextLine(secondsOfGenerating, i, deviceType, date, uuid))
        }
        return resultStringBuilder.toString()
    }

    private fun nextLine(
        secondsOfGenerating: Int,
        i: Int,
        deviceType: DeviceItem.Type,
        date: LocalDateTime,
        uuid: String
    ): String {
        val endCharacter = if (secondsOfGenerating == i) "" else "\n"

        return if (deviceType == DeviceItem.Type.AIRBEAM3)
            ab3Line(i, uuid, date, endCharacter)
        else
            abMiniLine(secondsOfGenerating, date, uuid, endCharacter)
    }

    private fun ab3Line(i: Int, uuid: String, date: LocalDateTime, endCharacter: String) =
        "$i,$uuid,${AB3Line(date)}$endCharacter"

    private fun abMiniLine(
        secondsOfGenerating: Int,
        date: LocalDateTime,
        uuid: String,
        endCharacter: String
    ) = if (secondsOfGenerating == 1)
        uuid
    else
        "${ABMiniLine(date)}$endCharacter"

    data class ABMiniLine(
        val date: LocalDateTime,
        val month: String = zeroForFront(date.month.value),
        val day: String = zeroForFront(date.dayOfMonth),
        val year: Int = date.year,
        val h: String = zeroForFront(date.hour),
        val min: String = zeroForFront(date.minute),
        val sec: String = zeroForFront(date.second),
        val lat: String = "50.${(1000000..2000000).random()}",
        val lng: String = "20.${(1000000..2000000).random()}",
        val fahrenheit: Int = (60..100).random(),
        val celsius: Int = (fahrenheit - 32) * 5 / 9,
        val kelvin: Int = celsius + 273,
        val pm1: Int = (0..100).random(),
        val pm25: Int = (0..100).random(),
        val pm10: Int = (0..100).random(),
        val rh: Int = (0..7).random(),

        ) {
        override fun toString(): String {
            return "$month/$day/$year,$h:$min:$sec,$lat,$lng,$fahrenheit,$celsius,$kelvin,$pm1,$pm25,$pm10,$rh"
        }
    }

    data class AB3Line(
        val date: LocalDateTime,
        val month: String = zeroForFront(date.month.value),
        val day: String = zeroForFront(date.dayOfMonth),
        val year: Int = date.year,
        val h: String = zeroForFront(date.hour),
        val min: String = zeroForFront(date.minute),
        val sec: String = zeroForFront(date.second),
        val lat: String = "50.${(1000000..2000000).random()}",
        val lng: String = "20.${(1000000..2000000).random()}",
        val fahrenheit: Int = (60..100).random(),
        val celsius: Int = (fahrenheit - 32) * 5 / 9,
        val kelvin: Int = celsius + 273,
        val pm1: Int = (0..100).random(),
        val pm25: Int = (0..100).random(),
        val pm10: Int = (0..100).random(),
        val rh: Int = (0..7).random(),

        ) {
        override fun toString(): String {
            return "$month/$day/$year,$h:$min:$sec,$lat,$lng,$fahrenheit,$celsius,$kelvin,$pm1,$pm25,$pm10,$rh"
        }
    }

    private fun zeroForFront(num: Int): String {
        return if (num < 10) "0$num"
        else num.toString()
    }
}
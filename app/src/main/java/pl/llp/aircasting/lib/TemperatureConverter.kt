package pl.llp.aircasting.lib

class TemperatureConverter {
    companion object {
        fun temperaturefromFehreinheitToCelcius(fahrenheitTemperature: Double): Double {
            // TODO: add temperature conversion algorithm from the net
            return ((fahrenheitTemperature - 32) * 5) / 9
        }
    }
}

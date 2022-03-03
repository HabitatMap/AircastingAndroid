package pl.llp.aircasting.lib

import pl.llp.aircasting.models.MeasurementStream

class TemperatureConverter private constructor(settings: Settings) {
    companion object {

        private var singleton: TemperatureConverter? = null

        fun setup(settings: Settings) {
            if (singleton == null) singleton = TemperatureConverter(settings)
        }

        fun get(): TemperatureConverter? {
            return singleton
        }

        fun setAppropriateDetailedType(stream: MeasurementStream) {
            if (singleton?.mSettings?.isCelsiusScaleEnabled() == true) stream.detailedType = "C"
        }

        fun isCelsiusToggleEnabled(): Boolean {
            return singleton?.mSettings?.isCelsiusScaleEnabled() == true
        }
    }

    private var mSettings: Settings? = settings
}

package pl.llp.aircasting.ui.viewmodel

import android.util.Log
import androidx.annotation.IdRes
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.params.CreateThresholdAlertData
import pl.llp.aircasting.data.api.params.ThresholdAlertResponse
import pl.llp.aircasting.data.api.repository.ThresholdAlertRepository
import pl.llp.aircasting.data.api.util.StringConstants.O3
import pl.llp.aircasting.data.api.util.StringConstants.PM2_5
import pl.llp.aircasting.data.api.util.StringConstants.responseOpenAQSensorNameOzone
import pl.llp.aircasting.data.api.util.StringConstants.responseOpenAQSensorNamePM
import pl.llp.aircasting.data.api.util.StringConstants.responsePurpleAirSensorName
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.TemperatureConverter
import pl.llp.aircasting.util.TimezoneHelper
import javax.inject.Inject

class CreateThresholdAlertBottomSheetViewModel @Inject constructor(
    private val alertRepository: ThresholdAlertRepository
) : ViewModel() {
    private lateinit var alerts: List<ThresholdAlertResponse>
    private lateinit var uiAlerts: List<ThresholdAlertUiRepresentation>

    fun getAlertsForDisplaying(session: Session?): Flow<Result<List<ThresholdAlertUiRepresentation>>> {
        session ?: return flow { emit(Result.failure(Exception("Session was null"))) }

        return flow {
            alertRepository.activeAlerts().collect {
                alerts = it
            }
            val sessionAlerts = alerts.filter {
                it.sessionUuid == session.uuid
            }
            uiAlerts = session.streams.map { stream ->
                val existingAlert =
                    sessionAlerts.find { it.sensorName == map(stream.sensorName) }
                Log.v("alert", existingAlert.toString())
                if (existingAlert != null) {
                    build(existingAlert, stream)
                } else {
                    buildEmptyAlert(stream)
                }
            }
            // We are giving Deep copy to view for modifying and later compare it with original
            val copy = uiAlerts.map { it.copy() }
            emit(Result.success(copy))
        }
    }

    private fun map(localSensorName: String) = when (localSensorName) {
        responsePurpleAirSensorName -> PM2_5
        responseOpenAQSensorNamePM -> PM2_5
        responseOpenAQSensorNameOzone -> O3
        else -> localSensorName
    }

    private fun build(existingAlert: ThresholdAlertResponse, stream: MeasurementStream) =
        ThresholdAlertUiRepresentation(
            stream,
            existingAlert.sensorName,
            true,
            existingAlert.thresholdValue,
            ThresholdAlertFrequency.build(existingAlert.frequency)
        )

    private fun buildEmptyAlert(stream: MeasurementStream) = ThresholdAlertUiRepresentation(stream)

    fun saveEditedAlerts(
        viewUiAlerts: List<ThresholdAlertUiRepresentation>,
        session: Session?
    ): Flow<Result<Unit>> = flow {
        var success = true
        val modified = viewUiAlerts.minus(uiAlerts.toSet())
        modified.forEach { updatedAlert ->
            val oldAlert = uiAlerts.find { it.streamTitle == updatedAlert.streamTitle }

            val savingChangesResult = when {
                alertWasCreated(oldAlert, updatedAlert) -> createAlert(
                    updatedAlert,
                    session
                )
                alertWasDeleted(oldAlert, updatedAlert) -> deleteAlert(
                    updatedAlert,
                    session
                )
                else -> replaceAlert(oldAlert, updatedAlert, session)
            }

            if (savingChangesResult.isFailure) success = false
        }
        val result = if (success) Result.success(Unit)
        else Result.failure(Exception("Something went wrong. Some data has not been saved"))
        emit(result)
    }

    private fun alertWasDeleted(
        oldAlert: ThresholdAlertUiRepresentation?,
        updatedAlert: ThresholdAlertUiRepresentation
    ) = oldAlert?.enabled == true && !updatedAlert.enabled

    private fun alertWasCreated(
        oldAlert: ThresholdAlertUiRepresentation?,
        updatedAlert: ThresholdAlertUiRepresentation
    ) = oldAlert?.enabled == false && updatedAlert.enabled

    private suspend fun createAlert(
        uiAlert: ThresholdAlertUiRepresentation,
        session: Session?
    ): Result<Unit> {
        session ?: return Result.failure(Exception("Session was null"))
        uiAlert.threshold ?: return Result.failure(Exception("Threshold value was null"))

        val alert = CreateThresholdAlertData(
            uiAlert.frequency.value,
            uiAlert.sensorName,
            session.uuid,
            uiAlert.threshold.toString(),
            TimezoneHelper.getTimezoneOffsetInSeconds().toString()
        )
        return runCatching { alertRepository.create(alert) }
    }

    private suspend fun deleteAlert(
        uiAlert: ThresholdAlertUiRepresentation?,
        session: Session?
    ): Result<Unit> {
        val oldAlert = alerts.find {
            it.sessionUuid == session?.uuid && it.sensorName == uiAlert?.sensorName
        }
        oldAlert ?: return Result.failure(
            Exception("Could not delete: Alert with ${uiAlert?.streamTitle} and ${session?.uuid} was not found ")
        )

        return runCatching { alertRepository.delete(oldAlert.id) }
    }

    private suspend fun replaceAlert(
        oldAlert: ThresholdAlertUiRepresentation?,
        updatedAlert: ThresholdAlertUiRepresentation,
        session: Session?
    ): Result<Unit> = runCatching {
        deleteAlert(oldAlert, session)
        createAlert(updatedAlert, session)
    }
}

class ThresholdAlertUiRepresentation(
    val stream: MeasurementStream,
    val sensorName: String = stream.sensorName,
    _enabled: Boolean = false,
    _threshold: Double? = null,
    _frequency: ThresholdAlertFrequency = ThresholdAlertFrequency.HOURLY,
) : BaseObservable() {
    val streamTitle: String = stream.detailedType ?: "Unnamed stream"

    @Bindable
    var enabled: Boolean = _enabled
        set(value) {
            Log.v("Alert", "value is: $enabled")
            if (value != enabled) {
                Log.v("Alert", "Changes to: $value")
                field = value
                notifyPropertyChanged(BR.alert)

            }
        }

    var threshold: Double? = _threshold

    @Bindable
    fun getThresholdUi(): Double? = if (stream.isDetailedTypeCelsius())
        TemperatureConverter.fahrenheitToCelsius(threshold)
    else threshold

    fun setThresholdUi(value: Double?) {
        Log.v("Alert", "value is $threshold")
        if (value != threshold) {
            threshold = if (stream.isDetailedTypeCelsius())
                TemperatureConverter.celsiusToFahrenheit(value)
            else
                value
            Log.v("Alert", "Changed to: $threshold")
            notifyPropertyChanged(BR.alert)
        }
    }

    @Bindable
    var frequency: ThresholdAlertFrequency = _frequency
        set(value) {
            Log.v("Alert", "value is $frequency")
            if (value != frequency) {
                Log.v("Alert", "Changes to: $value")
                field = value
                notifyPropertyChanged(BR.alert)
            }
        }

    fun copy() = ThresholdAlertUiRepresentation(
        this.stream,
        this.sensorName,
        this.enabled,
        this.threshold,
        this.frequency
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ThresholdAlertUiRepresentation

        if (streamTitle != other.streamTitle) return false
        if (enabled != other.enabled) return false
        if (threshold != other.threshold) return false
        if (frequency != other.frequency) return false

        return true
    }

    override fun hashCode(): Int {
        var result = streamTitle.hashCode()
        result = 31 * result + enabled.hashCode()
        result = 31 * result + (threshold?.hashCode() ?: 0)
        result = 31 * result + frequency.hashCode()
        return result
    }
}

enum class ThresholdAlertFrequency {
    HOURLY {
        override val value = "1"
        override val buttonId = R.id.hourly_frequency_button
    },
    DAILY {
        override val value = "24"
        override val buttonId = R.id.daily_frequency_button
    };

    abstract val value: String
    abstract val buttonId: Int

    companion object {
        fun build(frequency: Int) = when (frequency) {
            1 -> HOURLY
            else -> DAILY
        }

        fun buildFromButtonId(@IdRes checkedButtonId: Int) = when (checkedButtonId) {
            R.id.daily_frequency_button -> DAILY
            else -> HOURLY
        }
    }
}
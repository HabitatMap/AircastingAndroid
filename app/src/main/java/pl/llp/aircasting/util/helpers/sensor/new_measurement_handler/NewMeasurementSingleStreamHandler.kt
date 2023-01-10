package pl.llp.aircasting.util.helpers.sensor.new_measurement_handler

import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler

open class NewMeasurementSingleStreamHandler(
    private val settings: Settings,
    private val errorHandler: ErrorHandler,
    private val sessionsRepository: SessionsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository,
) : NewMeasurementAirBeamHandler(
    settings,
    errorHandler,
    sessionsRepository,
    measurementStreamsRepository,
    measurementsRepository,
    activeSessionMeasurementsRepository
) {
    override val numberOfStreams: Int
        get() = 1
}
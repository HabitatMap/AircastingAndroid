package io.lunarlogic.aircasting.lib

class ProgressBarCounter(
  var initialStepNumber: Int
) {
    private val STEP_PROGRESS = 10

    var currentProgressMax = STEP_PROGRESS * initialStepNumber

    fun increaseMaxProgress(stepsNumber: Int) {
        currentProgressMax += STEP_PROGRESS * stepsNumber
    }

    fun decreaseMaxProgress(stepsNumber: Int) {
        //todo: maybe needed when going back after selecting device type == bluetooth device
        currentProgressMax -= STEP_PROGRESS * stepsNumber
    }

    companion object {
        val DEFAULT_INITIAL_STEP_NUMBER_NEW_SESSION_FLOW = 4
        val ADDITIONAL_STEPS_LOCATION_OFF = 1
        val ADDITIONAL_STEPS_BLUETOOTH_OFF = 1
        val ADDITIONAL_STEPS_DISABLED_MAPS = 1
        val ADDITIONAL_STEPS_BLUETOOTH_DEVICE = 4
    }
}

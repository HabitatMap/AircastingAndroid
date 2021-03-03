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
}

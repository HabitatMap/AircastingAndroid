package io.lunarlogic.aircasting.lib

class ProgressBarCounter(
  var initialStepNumber: Int = DEFAULT_STEP_NUMBER
) {
    private val STEP_PROGRESS = 10


    var currentProgressMax = STEP_PROGRESS * initialStepNumber

    fun increaseMaxProgress(stepsNumber: Int = DEFAULT_MAX_PROGRESS_INCREASE) {
        currentProgressMax += STEP_PROGRESS * stepsNumber
    }

    fun decreaseMaxProgress(stepsNumber: Int) {
        //todo: maybe needed when going back after selecting device type == bluetooth device
        currentProgressMax -= STEP_PROGRESS * stepsNumber
    }

    companion object {
        val DEFAULT_STEP_NUMBER = 4  // we got 3 basic steps in the flow (progress bar should have 1 more to look better)
        val DEFAULT_ONBOARDING_STEP_NUMBER = 7
        val DEFAULT_MAX_PROGRESS_INCREASE = 1
    }
}

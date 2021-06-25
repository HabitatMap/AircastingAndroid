package pl.llp.aircasting.lib

class ProgressBarCounter(
  var initialStepNumber: Int = DEFAULT_STEP_NUMBER
) {
    var currentProgressMax = STEP_PROGRESS * initialStepNumber

    fun increaseMaxProgress(stepsNumber: Int = DEFAULT_MAX_PROGRESS_INCREASE) {
        currentProgressMax += STEP_PROGRESS * stepsNumber
    }

    companion object {
        val DEFAULT_STEP_NUMBER = 4  // we got 3 basic steps in the flow (progress bar should have 1 more to look better)
        val DEFAULT_ONBOARDING_STEP_NUMBER = 6
        val DEFAULT_SYNC_STEP_NUMBER = 6
        val DEFAULT_MAX_PROGRESS_INCREASE = 1
        private val STEP_PROGRESS = 10
    }
}

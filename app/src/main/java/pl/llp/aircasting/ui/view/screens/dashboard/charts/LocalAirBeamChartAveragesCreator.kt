package pl.llp.aircasting.ui.view.screens.dashboard.charts

import java.util.*

class LocalAirBeamChartAveragesCreator : ChartAveragesCreator() {
    override fun modifyHours(date: Date, hours: Int): Date {
        return super.modifyHours(date, 1)
    }
}
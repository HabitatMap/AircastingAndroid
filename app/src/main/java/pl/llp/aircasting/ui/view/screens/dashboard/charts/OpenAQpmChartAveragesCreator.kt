package pl.llp.aircasting.ui.view.screens.dashboard.charts

import java.util.*

class OpenAQpmChartAveragesCreator : ExternalChartAveragesCreator() {
    override fun modifyHours(date: Date, hours: Int): Date {
        return super.modifyHours(date, -2)
    }
}
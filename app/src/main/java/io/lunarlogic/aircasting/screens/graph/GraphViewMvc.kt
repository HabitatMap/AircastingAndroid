package io.lunarlogic.aircasting.screens.graph

import io.lunarlogic.aircasting.screens.common.HLUListener
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.screens.common.SessionViewMvc

interface GraphViewMvc: ObservableViewMvc<GraphViewMvc.Listener>, SessionViewMvc {
    interface Listener: HLUListener
}

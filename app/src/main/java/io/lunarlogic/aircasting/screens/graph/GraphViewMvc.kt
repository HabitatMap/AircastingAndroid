package io.lunarlogic.aircasting.screens.graph

import io.lunarlogic.aircasting.screens.common.hlu.HLUListener
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.screens.common.session_view.SessionViewMvc

interface GraphViewMvc: ObservableViewMvc<GraphViewMvc.Listener>, SessionViewMvc {
    interface Listener: HLUListener
}

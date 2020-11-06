package io.lunarlogic.aircasting.screens.session_view.graph

import io.lunarlogic.aircasting.screens.session_view.hlu.HLUListener
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.screens.session_view.SessionViewMvc

interface GraphViewMvc: ObservableViewMvc<GraphViewMvc.Listener>, SessionViewMvc {
    interface Listener: HLUListener
}

package feature.splash.presentation

import base.compose.mvi.Effect
import base.compose.mvi.Event
import base.compose.mvi.Intent
import base.compose.mvi.State

data object SplashState : State

sealed class SplashIntent : Intent {
    data object Init : SplashIntent()
}

sealed class SplashEffect : Effect {
    data object NavigateToDashboard : SplashEffect()
}

sealed class SplashEvent : Event {
    data object NavigateToDashboard : SplashEvent()
}

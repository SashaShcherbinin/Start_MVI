package feature.splash.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.navigation.NavOptions
import base.compose.local.LocalNavigation
import base.compose.view.LoadingView
import feature.dashboard.domain.navigation.NavDashboard
import feature.splash.domain.navigation.NavSplash
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashScreen(viewModel: SplashViewModel = koinViewModel()) {
    val navController = LocalNavigation.current
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is SplashEvent.NavigateToDashboard -> navController.navigate(
                    NavDashboard,
                    NavOptions.Builder().setPopUpTo<NavSplash>(inclusive = true).build()
                )
            }
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LoadingView()
    }
}

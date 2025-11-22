package app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import base.compose.local.LocalNavigation
import base.compose.theme.AppTheme
import feature.dashboard.domain.navigation.NavDashboard
import feature.dashboard.presentation.DashboardScreen
import feature.splash.domain.navigation.NavSplash
import feature.splash.presentation.SplashScreen
import feature.user.domain.navigation.NavUser
import feature.user.presentation.UserScreen

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                MainContent()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainContent() {
        val navController = rememberNavController()

        val provider: ProvidedValue<NavController> = LocalNavigation.provides(navController)
        CompositionLocalProvider(provider) {
            NavHost(navController, startDestination = NavSplash) {
                composable<NavSplash> { SplashScreen() }
                composable<NavDashboard> { DashboardScreen() }
                composable<NavUser> { backStackEntry ->
                    UserScreen(
                        userId = backStackEntry.toRoute<NavUser>().userId,
                    )
                }
            }
        }
    }
}

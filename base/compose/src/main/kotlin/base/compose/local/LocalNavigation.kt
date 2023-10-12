package base.compose.local

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavController

val LocalNavigation: ProvidableCompositionLocal<NavController> =
    compositionLocalOf { error("CompositionLocal NavController not present") }

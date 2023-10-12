package base.compose.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.navigation.NavController

fun NavController.navBack(context: Context) {
    if (popBackStack().not()) {
        context.findActivity().finish()
    }
}

private fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    error("no activity")
}

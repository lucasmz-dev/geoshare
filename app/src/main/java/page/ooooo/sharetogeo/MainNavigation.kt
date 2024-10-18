package page.ooooo.sharetogeo

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("about") {
            AboutScreen(onNavigateToMainScreen = { navController.navigate("main") })
        }
        composable("main") {
            MainScreen(onNavigateToAboutScreen = { navController.navigate("about") })
        }
    }
}

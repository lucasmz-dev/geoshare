package page.ooooo.geoshare

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("about") {
            AboutScreen(
                onNavigateToMainScreen = { navController.navigate("main") },
            )
        }
        composable("faq") {
            FaqScreen(
                onNavigateToMainScreen = { navController.navigate("main") },
                onNavigateToUserPreferencesScreen = { navController.navigate("user_preferences") }
            )
        }
        composable("main") {
            MainScreen(
                onNavigateToAboutScreen = { navController.navigate("about") },
                onNavigateToFaqScreen = { navController.navigate("faq") },
                onNavigateToUserPreferencesScreen = { navController.navigate("user_preferences") }
            )
        }
        composable("user_preferences") {
            UserPreferencesScreen(
                onNavigateToMainScreen = { navController.navigate("main") },
                onNavigateToFaqScreen = { navController.navigate("faq") },
            )
        }
    }
}

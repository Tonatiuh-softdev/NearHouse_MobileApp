package com.equipo4.nearhome

import android.graphics.Color
import com.equipo4.nearhome.ui.theme.NearHomeTheme
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.equipo4.nearhome.ui.admin.AdminPanelScreen
import com.equipo4.nearhome.ui.admin.listings.AdminListingDetailScreen
import com.equipo4.nearhome.ui.admin.listings.AdminListingsScreen
import com.equipo4.nearhome.ui.admin.listings.ListingActionScreen
import com.equipo4.nearhome.ui.admin.listings.ListingFiltersScreen
import com.equipo4.nearhome.ui.admin.listings.AdminListingsViewModel
import com.equipo4.nearhome.domain.model.ListingAction
import com.equipo4.nearhome.ui.admin.reports.ReportsScreen
import com.equipo4.nearhome.ui.admin.reports.ReportDetailScreen
import com.equipo4.nearhome.ui.admin.reports.ReportConfirmationScreen
import com.equipo4.nearhome.ui.auth.completeprofile.CompleteProfileScreen
import com.equipo4.nearhome.ui.auth.forgot_password.ForgotPasswordScreen
import com.equipo4.nearhome.ui.auth.forgot_password.ForgotPasswordViewModel
import com.equipo4.nearhome.ui.auth.login.LoginScreen
import com.equipo4.nearhome.ui.auth.register.SignUpScreen
import com.equipo4.nearhome.ui.auth.verification.PhoneVerificationScreen
import com.equipo4.nearhome.ui.home.list.HomeScreen
import com.equipo4.nearhome.ui.home.map.MapScreen
import com.equipo4.nearhome.ui.legal.PoliticaPrivacidadScreen
import com.equipo4.nearhome.ui.legal.TerminosCondicionesScreen
import com.equipo4.nearhome.ui.profile.ProfileScreen
import com.equipo4.nearhome.ui.saved.SavedScreen
import com.equipo4.nearhome.ui.publicacion.detail.DetailPublicacionScreen
import com.equipo4.nearhome.ui.splash.SplashScreen
import com.equipo4.nearhome.util.SessionManager

object AppRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val FORGOT_PASSWORD = "forgot_password"
    const val COMPLETE_PROFILE = "complete_profile"
    const val PHONE_VERIFICATION = "phone_verification"
    const val HOME = "home"
    const val MAP = "home_map"
    const val SAVED = "saved"
    const val PROFILE = "profile"
    const val PUBLICACION_DETAIL = "publicacion_detail"
    const val TERMS_AND_CONDITIONS = "terms_and_conditions"
    const val PRIVACY_POLICY = "privacy_policy"
    const val ADMIN_PANEL = "admin_panel"
    const val ADMIN_REPORTS = "admin_reports"
    const val ADMIN_REPORT_DETAIL = "admin_report_detail"
    const val ADMIN_REPORT_CONFIRMATION = "admin_report_confirmation"
    const val ADMIN_LISTINGS = "admin_listings"
    const val ADMIN_LISTING_FILTERS = "admin_listing_filters"
    const val ADMIN_LISTING_DETAIL = "admin_listing_detail"
    const val ADMIN_LISTING_ACTION = "admin_listing_action"
    /** Clave del aviso verde que se pasa entre pantallas de Anuncios. */
    const val LISTING_MESSAGE = "listing_message"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        setContent {
            NearHomeTheme {
                NearHouseApp()
            }
        }
    }
}

@Composable
fun NearHouseApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.SPLASH
    ) {
        // Pantalla 0: Animacion
        composable(AppRoutes.SPLASH) {
            SplashScreen(
                onFinished = { isLoggedIn ->
                    val destination =
                        if (isLoggedIn) AppRoutes.HOME
                        else AppRoutes.LOGIN

                    navController.navigate(destination) {
                        popUpTo(AppRoutes.SPLASH) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Pantalla 1: Login
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(AppRoutes.SIGN_UP)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(AppRoutes.FORGOT_PASSWORD)
                },
                onLoginSuccess = {
                    sessionManager.setLoggedIn(true)
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla 2: Registro (Sign Up)
        composable(AppRoutes.SIGN_UP) {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onSignUpSuccess = {
                    navController.navigate(AppRoutes.COMPLETE_PROFILE) {
                        popUpTo(AppRoutes.SIGN_UP) { inclusive = true }
                    }
                },
                onNavigateToTerms = {
                    navController.navigate(AppRoutes.TERMS_AND_CONDITIONS)
                },
                onNavigateToPrivacy = {
                    navController.navigate(AppRoutes.PRIVACY_POLICY)
                }
            )
        }

        // Pantalla Términos y Condiciones
        composable(AppRoutes.TERMS_AND_CONDITIONS) {
            TerminosCondicionesScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla Política de Privacidad
        composable(AppRoutes.PRIVACY_POLICY) {
            PoliticaPrivacidadScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla 3: Completa tu Perfil
        composable(AppRoutes.COMPLETE_PROFILE) {
            CompleteProfileScreen(
                onCompleteProfileSuccess = { phoneNumber ->
                    val encodedPhone = Uri.encode(phoneNumber)
                    navController.navigate("${AppRoutes.PHONE_VERIFICATION}/$encodedPhone")
                }
            )
        }

        // Pantalla 4: Verificación de Teléfono
        composable(
            route = "${AppRoutes.PHONE_VERIFICATION}/{phoneNumber}",
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            PhoneVerificationScreen(
                phoneNumber = phoneNumber,
                onNavigateToLogin = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla 5: Recuperación de contraseña
        composable(AppRoutes.FORGOT_PASSWORD) {
            val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel()
            ForgotPasswordScreen(
                viewModel = forgotPasswordViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToSignUp = {
                    navController.navigate(AppRoutes.SIGN_UP) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = false }
                    }
                }
            )
        }

        // Pantalla 6: Home (Lista de inmuebles)
        val onBottomTab: (Int) -> Unit = { index ->
            when (index) {
                0 -> navController.navigate(AppRoutes.HOME) {
                    popUpTo(AppRoutes.HOME) { inclusive = false }
                    launchSingleTop = true
                }
                1 -> navController.navigate(AppRoutes.SAVED) {
                    popUpTo(AppRoutes.HOME) { inclusive = false }
                    launchSingleTop = true
                }
                3 -> navController.navigate(AppRoutes.PROFILE) {
                    popUpTo(AppRoutes.HOME) { inclusive = false }
                    launchSingleTop = true
                }
                else -> Unit // 2 Notificaciones: aún sin pantalla
            }
        }

        composable(AppRoutes.HOME) {
            HomeScreen(
                onPropertyClick = { propertyId ->
                    navController.navigate("${AppRoutes.PUBLICACION_DETAIL}/$propertyId")
                },
                onToggleToMap = {
                    navController.navigate(AppRoutes.MAP) { launchSingleTop = true }
                },
                onBottomTabSelected = onBottomTab
            )
        }

        // Pantalla: Propiedades guardadas
        composable(AppRoutes.SAVED) {
            SavedScreen(
                onPropertyClick = { propertyId ->
                    navController.navigate("${AppRoutes.PUBLICACION_DETAIL}/$propertyId")
                },
                onBottomTabSelected = onBottomTab
            )
        }

        // Pantalla: Perfil (Vendedor/Arrendador/Admin)
        composable(AppRoutes.PROFILE) {
            ProfileScreen(
                onMyListingsClick = {},
                onAccountClick = {},
                onAdminPanelClick = {
                    navController.navigate(AppRoutes.ADMIN_PANEL)
                },
                onTermsClick = { navController.navigate(AppRoutes.TERMS_AND_CONDITIONS) },
                onPrivacyClick = { navController.navigate(AppRoutes.PRIVACY_POLICY) },
                onLogoutClick = {
                    sessionManager.clearSession()
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBottomTabSelected = onBottomTab
            )
        }

        // Pantalla: Panel de administración
        composable(AppRoutes.ADMIN_PANEL) {
            AdminPanelScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToReports = {
                    navController.navigate(AppRoutes.ADMIN_REPORTS)
                },
                onNavigateToUsers = {},
                onNavigateToAds = {
                    navController.navigate(AppRoutes.ADMIN_LISTINGS)
                },
                onBottomTabSelected = onBottomTab
            )
        }

        // Pantalla: Reportes
        composable(AppRoutes.ADMIN_REPORTS) {
            ReportsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onReportClick = { reportId ->
                    navController.navigate("${AppRoutes.ADMIN_REPORT_DETAIL}/$reportId")
                },
                onBottomTabSelected = onBottomTab
            )
        }

        // Pantalla: Detalle del Reporte
        composable(
            route = "${AppRoutes.ADMIN_REPORT_DETAIL}/{reportId}",
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
            ReportDetailScreen(
                reportId = reportId,
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToConfirmation = { id ->
                    navController.navigate("${AppRoutes.ADMIN_REPORT_CONFIRMATION}/$id") {
                        popUpTo("${AppRoutes.ADMIN_REPORT_DETAIL}/$reportId") { inclusive = true }
                    }
                },
                onPropertyClick = { propertyId ->
                    navController.navigate("${AppRoutes.PUBLICACION_DETAIL}/$propertyId")
                }
            )
        }

        // Pantalla: Confirmación de Reporte Atendido
        composable(
            route = "${AppRoutes.ADMIN_REPORT_CONFIRMATION}/{reportId}",
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
            ReportConfirmationScreen(
                reportId = reportId,
                onReturnToList = {
                    navController.navigate(AppRoutes.ADMIN_REPORTS) {
                        popUpTo(AppRoutes.ADMIN_REPORTS) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla: Anuncios (administración)
        composable(AppRoutes.ADMIN_LISTINGS) { entry ->
            val message by entry.savedStateHandle
                .getStateFlow<String?>(AppRoutes.LISTING_MESSAGE, null).collectAsState()
            AdminListingsScreen(
                message = message,
                onMessageShown = { entry.savedStateHandle[AppRoutes.LISTING_MESSAGE] = null },
                onBackClick = { navController.popBackStack() },
                onFiltersClick = { navController.navigate(AppRoutes.ADMIN_LISTING_FILTERS) },
                onListingClick = { id -> navController.navigate("${AppRoutes.ADMIN_LISTING_DETAIL}/$id") },
                onBottomTabSelected = onBottomTab
            )
        }

        // Pantalla: Filtrar anuncios (comparte el ViewModel de la lista)
        composable(AppRoutes.ADMIN_LISTING_FILTERS) { entry ->
            val parent = remember(entry) { navController.getBackStackEntry(AppRoutes.ADMIN_LISTINGS) }
            ListingFiltersScreen(
                viewModel = viewModel<AdminListingsViewModel>(parent),
                onBackClick = { navController.popBackStack() },
                onApplied = { navController.popBackStack() }
            )
        }

        // Pantalla: Detalle del anuncio
        composable(
            route = "${AppRoutes.ADMIN_LISTING_DETAIL}/{listingId}",
            arguments = listOf(navArgument("listingId") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("listingId") ?: ""
            val message by entry.savedStateHandle
                .getStateFlow<String?>(AppRoutes.LISTING_MESSAGE, null).collectAsState()
            AdminListingDetailScreen(
                listingId = id,
                message = message,
                onMessageShown = { entry.savedStateHandle[AppRoutes.LISTING_MESSAGE] = null },
                onBackClick = { navController.popBackStack() },
                onActionClick = { action ->
                    navController.navigate("${AppRoutes.ADMIN_LISTING_ACTION}/$id/${action.name}")
                }
            )
        }

        // Pantalla: Confirmación (Aprobar, Rechazar, Desactivar, Reactivar o Eliminar)
        composable(
            route = "${AppRoutes.ADMIN_LISTING_ACTION}/{listingId}/{action}",
            arguments = listOf(
                navArgument("listingId") { type = NavType.StringType },
                navArgument("action") { type = NavType.StringType }
            )
        ) { entry ->
            val id = entry.arguments?.getString("listingId") ?: ""
            val action = runCatching { ListingAction.valueOf(entry.arguments?.getString("action") ?: "") }.getOrNull()
            if (action != null) {
                ListingActionScreen(
                    listingId = id,
                    action = action,
                    onBackClick = { navController.popBackStack() },
                    onConfirmed = { message ->
                        // Regresa al Detalle con el aviso verde.
                        navController.previousBackStackEntry?.savedStateHandle?.set(AppRoutes.LISTING_MESSAGE, message)
                        navController.popBackStack()
                    },
                    onDeleted = { message ->
                        // Borra Detalle y Eliminar del historial y vuelve a la lista.
                        navController.getBackStackEntry(AppRoutes.ADMIN_LISTINGS)
                            .savedStateHandle[AppRoutes.LISTING_MESSAGE] = message
                        navController.popBackStack(AppRoutes.ADMIN_LISTINGS, inclusive = false)
                    }
                )
            }
        }

        // Pantalla 6b: Home (Mapa de inmuebles)
        composable(AppRoutes.MAP) {
            MapScreen(
                onToggleToList = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onPropertyClick = { propertyId ->
                    navController.navigate("${AppRoutes.PUBLICACION_DETAIL}/$propertyId")
                },
                onBottomTabSelected = onBottomTab
            )
        }

        // Pantalla 7: Detalle de Publicación
        composable(
            route = "${AppRoutes.PUBLICACION_DETAIL}/{publicacionId}",
            arguments = listOf(navArgument("publicacionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val publicacionId = backStackEntry.arguments?.getString("publicacionId") ?: ""

            DetailPublicacionScreen(
                publicacionId = publicacionId,
                onBackClick = {
                    navController.popBackStack()
                },
                onReportClick = {
                },
                onEditClick = {
                }
            )
        }
    }
}

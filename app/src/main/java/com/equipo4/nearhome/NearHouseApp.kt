package com.equipo4.nearhome

import android.graphics.Color
import com.equipo4.nearhome.ui.theme.NearHomeTheme
import androidx.compose.foundation.isSystemInDarkTheme
import com.equipo4.nearhome.data.local.SettingsManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.equipo4.nearhome.ui.admin.AdminPanelScreen
import com.equipo4.nearhome.domain.model.ListingAction
import com.equipo4.nearhome.ui.admin.listings.AdminListingDetailScreen
import com.equipo4.nearhome.ui.admin.listings.AdminListingsScreen
import com.equipo4.nearhome.ui.admin.listings.AdminListingsViewModel
import com.equipo4.nearhome.ui.admin.listings.ListingActionScreen
import com.equipo4.nearhome.ui.admin.listings.ListingFiltersScreen
import com.equipo4.nearhome.ui.admin.reports.ReportsScreen
import com.equipo4.nearhome.ui.admin.reports.ReportDetailScreen
import com.equipo4.nearhome.ui.admin.reports.ReportConfirmationScreen
import com.equipo4.nearhome.ui.admin.users_crud.UsersListScreen
import com.equipo4.nearhome.ui.admin.users_crud.UsersListViewModel
import com.equipo4.nearhome.ui.admin.users_crud.FilterUsersScreen
import com.equipo4.nearhome.ui.admin.users_crud.FilterUsersViewModel
import com.equipo4.nearhome.ui.admin.users_crud.FilterUsersViewModelFactory
import com.equipo4.nearhome.ui.admin.users_crud.detail.UserDetailScreen
import com.equipo4.nearhome.ui.admin.users_crud.block.BlockUserScreen
import com.equipo4.nearhome.ui.admin.users_crud.delete.DeleteUserScreen
import com.equipo4.nearhome.ui.auth.completeprofile.CompleteProfileScreen
import com.equipo4.nearhome.ui.auth.forgot_password.ForgotPasswordScreen
import com.equipo4.nearhome.ui.auth.forgot_password.ForgotPasswordViewModel
import com.equipo4.nearhome.ui.auth.login.LoginScreen
import com.equipo4.nearhome.ui.auth.register.SignUpScreen
import com.equipo4.nearhome.ui.auth.verification.PhoneVerificationScreen
import com.equipo4.nearhome.ui.home.list.HomeScreen
import com.equipo4.nearhome.ui.home.map.MapScreen
import com.equipo4.nearhome.ui.home.filter.SearchFiltersScreen
import com.equipo4.nearhome.ui.legal.PoliticaPrivacidadScreen
import com.equipo4.nearhome.ui.legal.TerminosCondicionesScreen
import com.equipo4.nearhome.ui.profile.ProfileScreen
import com.equipo4.nearhome.ui.profile.UserRole
import com.equipo4.nearhome.ui.profile.seller.EditProfileScreen
import com.equipo4.nearhome.ui.profile.seller.MyListingsScreen
import com.equipo4.nearhome.ui.saved.SavedScreen
import com.equipo4.nearhome.ui.publicacion.detail.DetailPublicacionScreen
import com.equipo4.nearhome.ui.splash.SplashScreen
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.session.CurrentUserStore
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.network.AuthApi
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.navigation
import kotlinx.coroutines.launch
import com.equipo4.nearhome.ui.createlisting.CreateListingStep1Screen
import com.equipo4.nearhome.ui.createlisting.CreateListingViewModel
import com.equipo4.nearhome.ui.createlisting.canCreateListings
import com.equipo4.nearhome.ui.createlisting.CreateListingStep2Screen
import com.equipo4.nearhome.ui.createlisting.CreateListingStep3Screen
import com.equipo4.nearhome.ui.createlisting.CreateListingStep4Screen
import com.equipo4.nearhome.ui.createlisting.CreateListingPreviewScreen
import com.equipo4.nearhome.ui.auth.CompletarPerfilGoogleScreen
import com.equipo4.nearhome.ui.auth.GoogleAuthState
import com.equipo4.nearhome.ui.auth.GoogleAuthViewModel
import com.equipo4.nearhome.ui.auth.GoogleAuthViewModelFactory

object AppRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val FORGOT_PASSWORD = "forgot_password"
    const val COMPLETE_PROFILE = "complete_profile"
    const val COMPLETAR_PERFIL_GOOGLE = "completar_perfil_google"
    const val PHONE_VERIFICATION = "phone_verification"
    const val HOME = "home"
    const val MAP = "home_map"
    const val SEARCH_FILTERS = "search_filters"
    const val SAVED = "saved"
    const val PROFILE = "profile"
    const val MY_LISTINGS = "my_listings"
    const val EDIT_PROFILE = "edit_profile"
    const val PUBLICACION_DETAIL = "publicacion_detail"
    const val TERMS_AND_CONDITIONS = "terms_and_conditions"
    const val PRIVACY_POLICY = "privacy_policy"
    const val ADMIN_PANEL = "admin_panel"
    const val ADMIN_REPORTS = "admin_reports"
    const val ADMIN_REPORT_DETAIL = "admin_report_detail"
    const val ADMIN_REPORT_CONFIRMATION = "admin_report_confirmation"
    const val ADMIN_USERS = "admin_users"
    const val ADMIN_USERS_FILTER = "admin_users_filter"
    const val ADMIN_USER_DETAIL = "admin_user_detail"
    const val ADMIN_USER_BLOCK = "admin_user_block"
    const val ADMIN_USER_DELETE = "admin_user_delete"
    const val ADMIN_LISTINGS = "admin_listings"
    const val ADMIN_LISTING_FILTERS = "admin_listing_filters"
    const val ADMIN_LISTING_DETAIL = "admin_listing_detail"
    const val ADMIN_LISTING_ACTION = "admin_listing_action"
    /** Clave del aviso verde que se pasa entre pantallas de Anuncios. */
    const val LISTING_MESSAGE = "listing_message"
    const val CREATE_LISTING_GRAPH = "create_listing_graph"
    const val CREATE_LISTING_STEP1 = "create_listing_step1"
    const val CREATE_LISTING_STEP2 = "create_listing_step2"
    const val CREATE_LISTING_STEP3 = "create_listing_step3"
    const val CREATE_LISTING_STEP4 = "create_listing_step4"
    const val CREATE_LISTING_PREVIEW = "create_listing_preview"
}

class MainActivity : AppCompatActivity() {
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
            val context = LocalContext.current
            remember { SettingsManager(context) }
            val darkModeState by SettingsManager.darkModeState.collectAsState()
            val systemInDark = isSystemInDarkTheme()
            val useDarkTheme = darkModeState ?: systemInDark

            NearHomeTheme(darkTheme = useDarkTheme) {
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
    val scope = rememberCoroutineScope()
    val authApi = remember {
        NetworkModule.provideRetrofit(NetworkModule.provideOkHttpClient(sessionManager))
            .create(AuthApi::class.java)
    }

    // ViewModel global para la autenticación con Google usando su Factory
    val googleAuthViewModel: GoogleAuthViewModel = viewModel(
        factory = GoogleAuthViewModelFactory(context)
    )
    val googleAuthState by googleAuthViewModel.uiState.collectAsState()

    // Escucha global de respuestas de la API de Google
    LaunchedEffect(googleAuthState) {
        when (googleAuthState) {
            is GoogleAuthState.SessionSuccess -> {
                sessionManager.setLoggedIn(true)
                navController.navigate(AppRoutes.HOME) {
                    popUpTo(AppRoutes.LOGIN) { inclusive = true }
                }
                googleAuthViewModel.reiniciarEstado()
            }
            is GoogleAuthState.RequiresRegistration -> {
                navController.navigate(AppRoutes.COMPLETAR_PERFIL_GOOGLE)
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.SPLASH
    ) {
        // Pantalla 0: Animación
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

        // Pantalla 3: Completa tu Perfil tradicional
        composable(AppRoutes.COMPLETE_PROFILE) {
            CompleteProfileScreen(
                onCompleteProfileSuccess = { phoneNumber ->
                    val encodedPhone = Uri.encode(phoneNumber)
                    navController.navigate("${AppRoutes.PHONE_VERIFICATION}/$encodedPhone")
                }
            )
        }

        // Pantalla 3b: Completar Perfil con Google (Caso C - Usuario nuevo)
        composable(AppRoutes.COMPLETAR_PERFIL_GOOGLE) {
            val state = googleAuthState
            if (state is GoogleAuthState.RequiresRegistration) {
                CompletarPerfilGoogleScreen(
                    proofData = state.proofData,
                    viewModel = googleAuthViewModel,
                    onRegistroExitoso = {
                        sessionManager.setLoggedIn(true)
                        navController.navigate(AppRoutes.HOME) {
                            popUpTo(AppRoutes.LOGIN) { inclusive = true }
                        }
                    },
                    onVolverAlLogin = {
                        googleAuthViewModel.reiniciarEstado()
                        navController.popBackStack(AppRoutes.LOGIN, inclusive = false)
                    }
                )
            }
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
                onFilterClick = {
                    navController.navigate(AppRoutes.SEARCH_FILTERS)
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

        // Pantalla: Perfil
        composable(AppRoutes.PROFILE) {
            ProfileScreen(
                onMyListingsClick = {
                    scope.launch {
                        if (CurrentUserStore.roleOrNull() == null) {
                            CurrentUserStore.refresh(authApi)
                        }
                        when (CurrentUserStore.roleOrNull()) {
                            UserRole.COMPRADOR -> navController.navigate(AppRoutes.CREATE_LISTING_GRAPH)
                            null -> Unit // sin datos no se decide, para no mandar a un vendedor a suscripciones
                            else -> navController.navigate(AppRoutes.MY_LISTINGS)
                        }
                    }
                },
                onAccountClick = {},
                onAdminPanelClick = {
                    navController.navigate(AppRoutes.ADMIN_PANEL)
                },
                onTermsClick = { navController.navigate(AppRoutes.TERMS_AND_CONDITIONS) },
                onPrivacyClick = { navController.navigate(AppRoutes.PRIVACY_POLICY) },
                onLogoutClick = {
                    sessionManager.clearSession()
                    CurrentUserStore.clear()
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSessionExpired = {
                    CurrentUserStore.clear()
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBottomTabSelected = onBottomTab
            )
        }

        // Pantalla: Mis Anuncios (Vendedor / Admin / SuperAdmin)
        composable(AppRoutes.MY_LISTINGS) {
            MyListingsScreen(
                onBackClick = { navController.popBackStack() },
                onEditProfileClick = { navController.navigate(AppRoutes.EDIT_PROFILE) },
                onCreateListingClick = { navController.navigate(AppRoutes.CREATE_LISTING_GRAPH) },
                onPropertyClick = { propertyId ->
                    navController.navigate("${AppRoutes.PUBLICACION_DETAIL}/$propertyId")
                },
                onBottomTabSelected = onBottomTab
            )
        }

        // Pantalla: Editar Perfil (Vendedor / Admin / SuperAdmin)
        composable(AppRoutes.EDIT_PROFILE) {
            EditProfileScreen(
                onNavigateToMyListingsWithSnackbar = { message ->
                    if (message.isNotEmpty()) {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("snackbar_msg", message)
                    }
                    navController.popBackStack()
                },
                onNavigateToAccount = {
                    navController.navigate(AppRoutes.PROFILE) {
                        popUpTo(AppRoutes.EDIT_PROFILE) { inclusive = true }
                    }
                },
                onBottomTabSelected = onBottomTab
            )
        }

        // Flujo: Crear anuncio (sub-gráfico con un solo ViewModel compartido)
        navigation(
            route = AppRoutes.CREATE_LISTING_GRAPH,
            startDestination = AppRoutes.CREATE_LISTING_STEP1
        ) {
            composable(AppRoutes.CREATE_LISTING_STEP1) { entry ->
                val canCreate = remember { canCreateListings(CurrentUserStore.roleOrNull()) }
                // Comprador: se cierra el flujo y regresa a Cuenta
                LaunchedEffect(canCreate) {
                    if (!canCreate) navController.popBackStack(AppRoutes.PROFILE, inclusive = false)
                }
                if (canCreate) {
                    val parent = remember(entry) { navController.getBackStackEntry(AppRoutes.CREATE_LISTING_GRAPH) }
                    val vm: CreateListingViewModel = viewModel(parent)
                    CreateListingStep1Screen(
                        viewModel = vm,
                        onExit = { navController.popBackStack(AppRoutes.CREATE_LISTING_GRAPH, inclusive = true) },
                        onContinue = { navController.navigate(AppRoutes.CREATE_LISTING_STEP2) },
                        onBottomTabSelected = onBottomTab
                    )
                }
            }
            composable(AppRoutes.CREATE_LISTING_STEP2) { entry ->
                val parent = remember(entry) { navController.getBackStackEntry(AppRoutes.CREATE_LISTING_GRAPH) }
                val vm: CreateListingViewModel = viewModel(parent)
                CreateListingStep2Screen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(AppRoutes.CREATE_LISTING_STEP3) },
                    onBottomTabSelected = onBottomTab
                )
            }
            composable(AppRoutes.CREATE_LISTING_STEP3) { entry ->
                val parent = remember(entry) { navController.getBackStackEntry(AppRoutes.CREATE_LISTING_GRAPH) }
                val vm: CreateListingViewModel = viewModel(parent)
                CreateListingStep3Screen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(AppRoutes.CREATE_LISTING_STEP4) },
                    onBottomTabSelected = onBottomTab
                )
            }
            composable(AppRoutes.CREATE_LISTING_STEP4) { entry ->
                val parent = remember(entry) { navController.getBackStackEntry(AppRoutes.CREATE_LISTING_GRAPH) }
                val vm: CreateListingViewModel = viewModel(parent)
                CreateListingStep4Screen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onEditData = { navController.popBackStack(AppRoutes.CREATE_LISTING_STEP1, inclusive = false) },
                    onEditContent = { navController.popBackStack(AppRoutes.CREATE_LISTING_STEP3, inclusive = false) },
                    onContinue = { navController.navigate(AppRoutes.CREATE_LISTING_PREVIEW) },
                    onBottomTabSelected = onBottomTab
                )
            }
            composable(AppRoutes.CREATE_LISTING_PREVIEW) { entry ->
                val parent = remember(entry) { navController.getBackStackEntry(AppRoutes.CREATE_LISTING_GRAPH) }
                val vm: CreateListingViewModel = viewModel(parent)
                CreateListingPreviewScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.popBackStack(AppRoutes.CREATE_LISTING_STEP1, inclusive = false) },
                    onFinished = {
                        vm.reset()
                        navController.navigate(AppRoutes.MY_LISTINGS) {
                            popUpTo(AppRoutes.CREATE_LISTING_GRAPH) { inclusive = true }
                        }
                    }
                )
            }
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
                onNavigateToUsers = {
                    navController.navigate(AppRoutes.ADMIN_USERS)
                },
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

        // Pantalla: Usuarios (Admin)
        composable(AppRoutes.ADMIN_USERS) {
            val parentEntry = remember { navController.getBackStackEntry(AppRoutes.ADMIN_USERS) }
            val usersViewModel: UsersListViewModel = viewModel(parentEntry)
            UsersListScreen(
                viewModel = usersViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onFilterClick = {
                    navController.navigate(AppRoutes.ADMIN_USERS_FILTER)
                },
                onUserClick = { userId ->
                    navController.navigate("${AppRoutes.ADMIN_USER_DETAIL}/$userId")
                },
                onBottomTabSelected = onBottomTab
            )
        }

        // Pantalla: Filtrar Usuarios
        composable(AppRoutes.ADMIN_USERS_FILTER) {
            val parentEntry = remember { navController.getBackStackEntry(AppRoutes.ADMIN_USERS) }
            val usersViewModel: UsersListViewModel = viewModel(parentEntry)
            val initialRoles by usersViewModel.selectedRolesFilter.collectAsState()
            val initialStatuses by usersViewModel.selectedStatusesFilter.collectAsState()
            val filterViewModel: FilterUsersViewModel = viewModel(
                factory = FilterUsersViewModelFactory(initialRoles, initialStatuses)
            )

            FilterUsersScreen(
                viewModel = filterViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onApplyFilters = { roles, statuses ->
                    usersViewModel.applyFilters(roles, statuses)
                    navController.popBackStack()
                }
            )
        }

        // Pantalla: Detalle de Usuario (Admin)
        composable(
            route = "${AppRoutes.ADMIN_USER_DETAIL}/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserDetailScreen(
                userId = userId,
                onBackClick = { navController.popBackStack() },
                onBlockClick = { id -> navController.navigate("${AppRoutes.ADMIN_USER_BLOCK}/$id") },
                onDeleteClick = { id -> navController.navigate("${AppRoutes.ADMIN_USER_DELETE}/$id") }
            )
        }

        // Pantalla: Bloquear / Suspender Usuario
        composable(
            route = "${AppRoutes.ADMIN_USER_BLOCK}/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            BlockUserScreen(
                userId = userId,
                onBackClick = { navController.popBackStack() },
                onBlockSuccess = { _ ->
                    navController.popBackStack()
                }
            )
        }

        // Pantalla: Eliminar Usuario
        composable(
            route = "${AppRoutes.ADMIN_USER_DELETE}/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            DeleteUserScreen(
                userId = userId,
                onBackClick = { navController.popBackStack() },
                onDeleteSuccess = { _ ->
                    navController.popBackStack(AppRoutes.ADMIN_USERS, inclusive = false)
                }
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
                        navController.previousBackStackEntry?.savedStateHandle?.set(AppRoutes.LISTING_MESSAGE, message)
                        navController.popBackStack()
                    },
                    onDeleted = { message ->
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
                onFilterClick = {
                    navController.navigate(AppRoutes.SEARCH_FILTERS)
                },
                onPropertyClick = { propertyId ->
                    navController.navigate("${AppRoutes.PUBLICACION_DETAIL}/$propertyId")
                },
                onBottomTabSelected = onBottomTab
            )
        }

        // Pantalla: Filtros de búsqueda
        composable(AppRoutes.SEARCH_FILTERS) {
            SearchFiltersScreen(
                onBackClick = { navController.popBackStack() },
                onApplySuccess = { navController.popBackStack() }
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
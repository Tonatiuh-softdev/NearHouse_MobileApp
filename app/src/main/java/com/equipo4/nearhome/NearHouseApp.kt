package com.equipo4.nearhome

import android.graphics.Color
import android.widget.Toast
import com.equipo4.nearhome.ui.theme.NearHomeTheme
import androidx.compose.foundation.isSystemInDarkTheme
import com.equipo4.nearhome.data.local.SettingsManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import com.equipo4.nearhome.data.repository.Repositories
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import com.equipo4.nearhome.ui.admin.users_crud.detail.UserDetailScreen
import com.equipo4.nearhome.ui.auth.completeprofile.CompleteProfileScreen
import com.equipo4.nearhome.ui.auth.recover.RecoverPasswordScreen
import com.equipo4.nearhome.ui.auth.recover.RecoverPasswordViewModel
import com.equipo4.nearhome.ui.auth.login.LoginScreen
import com.equipo4.nearhome.ui.auth.register.SignUpScreen
import com.equipo4.nearhome.ui.auth.verification.PhoneVerificationScreen
import com.equipo4.nearhome.ui.home.list.HomeScreen
import com.equipo4.nearhome.ui.home.map.MapScreen
import com.equipo4.nearhome.ui.home.filter.SearchFiltersScreen
import com.equipo4.nearhome.ui.legal.PoliticaPrivacidadScreen
import com.equipo4.nearhome.ui.legal.TerminosCondicionesScreen
import com.equipo4.nearhome.ui.notifications.NotificationsScreen
import com.equipo4.nearhome.ui.notifications.NotificationsViewModel
import com.equipo4.nearhome.ui.profile.ProfileScreen
import com.equipo4.nearhome.ui.profile.UserRole
import com.equipo4.nearhome.ui.profile.account.AccountAdminRoute
import com.equipo4.nearhome.ui.profile.publicprofile.UserProfileScreen
import com.equipo4.nearhome.ui.profile.seller.EditProfileScreen
import com.equipo4.nearhome.ui.profile.seller.MyListingsScreen
import com.equipo4.nearhome.ui.subscription.CheckoutScreen
import com.equipo4.nearhome.ui.subscription.ChoosePlanScreen
import com.equipo4.nearhome.ui.saved.SavedScreen
import com.equipo4.nearhome.ui.publicacion.detail.DetailPublicacionScreen
import com.equipo4.nearhome.ui.publicacion.report.ReportCategoryScreen
import com.equipo4.nearhome.ui.publicacion.report.ReportReasonScreen
import com.equipo4.nearhome.ui.publicacion.report.ReportViewModel
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
import com.equipo4.nearhome.ui.createlisting.DestinoCrearAnuncio
import com.equipo4.nearhome.ui.createlisting.destinoCrearAnuncio
import com.equipo4.nearhome.data.model.RegistroTemporal
import com.equipo4.nearhome.ui.auth.GoogleAuthState
import com.equipo4.nearhome.ui.auth.GoogleAuthViewModel
import com.equipo4.nearhome.ui.auth.GoogleAuthViewModelFactory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.equipo4.nearhome.ui.notifications.NotificacionesHelper
import com.equipo4.nearhome.ui.notifications.PedirPermisoNotificaciones
import com.equipo4.nearhome.ui.notifications.TokenManager
import com.equipo4.nearhome.ui.notifications.ContadorNotificaciones

object AppRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val FORGOT_PASSWORD = "forgot_password"
    const val COMPLETE_PROFILE = "complete_profile"
    const val PHONE_VERIFICATION = "phone_verification"
    const val HOME = "home"
    const val MAP = "home_map"
    const val SEARCH_FILTERS = "search_filters"
    const val SAVED = "saved"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val ACCOUNT_ADMIN = "account_admin"
    const val MY_LISTINGS = "my_listings"
    const val CHOOSE_PLAN = "choose_plan"
    const val CHECKOUT = "checkout"
    const val EDIT_PROFILE = "edit_profile"
    const val PUBLICACION_DETAIL = "publicacion_detail"
    const val USER_PROFILE = "user_profile"
    const val REPORT_GRAPH = "report_graph"
    const val REPORT_CATEGORY = "report_category"
    const val REPORT_REASON = "report_reason"
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
    const val SUBSCRIPTION = "subscription"
    const val CREATE_LISTING_EDIT = "create_listing_edit/{listingId}"
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

        Repositories.init(applicationContext)
        NotificacionesHelper.crearCanal(applicationContext)

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

    override fun onResume() {
        super.onResume()
        ContadorNotificaciones.refrescar(applicationContext)
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

    val googleAuthViewModel: GoogleAuthViewModel = viewModel(
        factory = GoogleAuthViewModelFactory(context)
    )
    val googleAuthState by googleAuthViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        val intentData = (context as? MainActivity)?.intent?.data
        if (intentData != null) {
            val ruta = intentData.toString().removePrefix("nearhouse://")
            if (ruta.isNotEmpty()) {
                if (sessionManager.isLoggedIn()) {
                    navController.navigate(ruta) {
                        popUpTo(AppRoutes.HOME) { inclusive = false }
                    }
                }
            }
            (context as? MainActivity)?.intent?.data = null
        }
    }

    LaunchedEffect(Unit) {
        if (sessionManager.isLoggedIn()) {
            TokenManager.registrarTokenActual(context, scope)
        }
    }

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
                // Usuario nuevo con Google: completa su perfil y verifica su teléfono por SMS
                // con las mismas pantallas del registro normal. No necesita contraseña.
                val proof = (googleAuthState as GoogleAuthState.RequiresRegistration).proofData
                val apellidos = proof.apellidos.trim().split(Regex("\\s+"), limit = 2)
                RegistroTemporal.limpiar()
                RegistroTemporal.googleProofToken = proof.googleProofToken
                RegistroTemporal.correo = proof.correo
                RegistroTemporal.nombre = proof.nombre.trim()
                RegistroTemporal.primerApellido = apellidos.getOrNull(0).orEmpty()
                RegistroTemporal.segundoApellido = apellidos.getOrNull(1)
                googleAuthViewModel.reiniciarEstado()
                navController.navigate(AppRoutes.COMPLETE_PROFILE)
            }
            is GoogleAuthState.Error -> {
                Toast.makeText(context, (googleAuthState as GoogleAuthState.Error).mensaje, Toast.LENGTH_LONG).show()
                googleAuthViewModel.reiniciarEstado()
            }
            else -> {}
        }
    }

    (googleAuthState as? GoogleAuthState.RequiresReactivation)?.let { estado ->
        AlertDialog(
            onDismissRequest = { googleAuthViewModel.reiniciarEstado() },
            title = {
                Text(
                    stringResource(
                        if (estado.esEliminacion) R.string.login_keep_title else R.string.login_reactivate_title
                    )
                )
            },
            text = {
                Text(
                    if (estado.esEliminacion) {
                        estado.mensaje ?: stringResource(R.string.login_keep_title)
                    } else {
                        stringResource(R.string.login_reactivate_text)
                    }
                )
            },
            confirmButton = {
                Button(onClick = { googleAuthViewModel.procesarTokenGoogle(estado.idToken, reactivar = true) }) {
                    Text(
                        stringResource(
                            if (estado.esEliminacion) R.string.login_keep_confirm else R.string.login_reactivate_confirm
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { googleAuthViewModel.reiniciarEstado() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    /** Abre el perfil de un usuario. Si es el propio vendedor con sesión, lo lleva a "Mis anuncios". */
    val openUserProfile: (Int) -> Unit = { userId ->
        val esPropio = userId == sessionManager.getUserId()
        if (esPropio && sessionManager.getUserRole() == "VENDEDOR") {
            navController.navigate(AppRoutes.MY_LISTINGS)
        } else {
            navController.navigate("${AppRoutes.USER_PROFILE}/$userId")
        }
    }

    /**
     * Único punto de entrada para "Crear anuncio". Todas las pantallas lo usan.
     * Decide según el usuario cargado: directo a crear, o a planes.
     */
    val irACrearAnuncio: () -> Unit = {
        when (destinoCrearAnuncio(CurrentUserStore.user.value)) {
            DestinoCrearAnuncio.CREAR -> navController.navigate(AppRoutes.CREATE_LISTING_GRAPH)
            DestinoCrearAnuncio.PLANES -> navController.navigate("${AppRoutes.SUBSCRIPTION}?motivo=crear")
        }
    }

    var homeReselectTrigger by remember { mutableStateOf(0L) }
    var savedReselectTrigger by remember { mutableStateOf(0L) }
    var notificationsReselectTrigger by remember { mutableStateOf(0L) }
    var profileReselectTrigger by remember { mutableStateOf(0L) }

    val sectionRoots = remember {
        mapOf(
            0 to AppRoutes.HOME,
            1 to AppRoutes.SAVED,
            2 to AppRoutes.NOTIFICATIONS,
            3 to AppRoutes.PROFILE
        )
    }

    fun getSectionForRoute(route: String?): Int {
        if (route == null) return 0
        return when {
            route == AppRoutes.HOME || route == AppRoutes.MAP -> 0
            route == AppRoutes.SAVED -> 1
            route == AppRoutes.NOTIFICATIONS -> 2
            route == AppRoutes.PROFILE ||
                    route == AppRoutes.ACCOUNT_ADMIN ||
                    route == AppRoutes.MY_LISTINGS ||
                    route == AppRoutes.EDIT_PROFILE ||
                    route == AppRoutes.ADMIN_PANEL ||
                    route == AppRoutes.ADMIN_REPORTS ||
                    route == AppRoutes.ADMIN_USERS ||
                    route == AppRoutes.ADMIN_LISTINGS ||
                    route == AppRoutes.ADMIN_LISTING_FILTERS ||
                    route.startsWith(AppRoutes.SUBSCRIPTION) ||
                    route.startsWith(AppRoutes.CHECKOUT) ||
                    route.startsWith(AppRoutes.CREATE_LISTING_GRAPH) ||
                    route.startsWith(AppRoutes.CREATE_LISTING_EDIT) ||
                    route.startsWith(AppRoutes.ADMIN_USER_DETAIL) ||
                    route.startsWith(AppRoutes.ADMIN_REPORT_DETAIL) ||
                    route.startsWith(AppRoutes.ADMIN_LISTING_DETAIL) ||
                    route.startsWith(AppRoutes.ADMIN_LISTING_ACTION) -> 3
            else -> -1
        }
    }

    val onBottomTab: (Int) -> Unit = { index ->
        val currentRoute = navController.currentDestination?.route
        val currentSection = getSectionForRoute(currentRoute)
        val targetRoot = sectionRoots[index] ?: AppRoutes.HOME

        if (currentSection == index) {
            if (currentRoute == targetRoot) {
                when (index) {
                    0 -> homeReselectTrigger++
                    1 -> savedReselectTrigger++
                    2 -> notificationsReselectTrigger++
                    3 -> profileReselectTrigger++
                }
            } else {
                navController.popBackStack(targetRoot, inclusive = false)
            }
        } else {
            navController.navigate(targetRoot) {
                popUpTo(AppRoutes.HOME) {
                    inclusive = false
                    saveState = false
                }
                launchSingleTop = true
                restoreState = false
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.SPLASH,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
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

        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(AppRoutes.SIGN_UP)
                },
                onNavigateToForgotPassword = { prefilledEmail ->
                    navController.navigate("${AppRoutes.FORGOT_PASSWORD}?email=${prefilledEmail.trim()}")
                },
                onLoginSuccess = {
                    sessionManager.setLoggedIn(true)
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
                onGoogleAuthTokenReceived = { token ->
                    googleAuthViewModel.procesarTokenGoogle(token)
                }
            )
        }

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
                },
                onGoogleAuthTokenReceived = { token ->
                    googleAuthViewModel.procesarTokenGoogle(token)
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
                },
                // Registro con Google: se guarda la sesión y entra directo a la app
                onSessionStarted = { sesion ->
                    sessionManager.saveSession(
                        token = sesion.token,
                        userId = sesion.usuario.id,
                        nombre = sesion.usuario.nombre,
                        rol = sesion.usuario.rol,
                        correo = sesion.usuario.correo
                    )
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla 5: Recuperación de contraseña
        composable(
            route = "${AppRoutes.FORGOT_PASSWORD}?email={email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType; defaultValue = "" })
        ) { entry ->
            val prefilledEmail = entry.arguments?.getString("email") ?: ""
            val recoverViewModel: RecoverPasswordViewModel = viewModel()
            val context = LocalContext.current
            RecoverPasswordScreen(
                viewModel = recoverViewModel,
                prefilledEmail = prefilledEmail,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSuccessFinished = { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    navController.popBackStack(AppRoutes.LOGIN, inclusive = false)
                }
            )
        }

        // Pantalla 6: Home (Lista de inmuebles)
        composable(AppRoutes.HOME) {
            // NUEVO: Pedir permiso de notificaciones (Android 13+)
            PedirPermisoNotificaciones()

            // Registrar este celular para recibir avisos cada vez que se llega a Home con sesión
            // (login, registro, Google...). Sin esto, tras cerrar sesión e iniciar de nuevo,
            // el servidor no tiene el token y no llegan notificaciones hasta reiniciar la app.
            LaunchedEffect(Unit) {
                if (sessionManager.isLoggedIn()) {
                    TokenManager.registrarTokenActual(context, scope)
                    ContadorNotificaciones.refrescar(context)
                }
            }

            HomeScreen(
                reselectTrigger = homeReselectTrigger,
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
                reselectTrigger = savedReselectTrigger,
                onPropertyClick = { propertyId ->
                    navController.navigate("${AppRoutes.PUBLICACION_DETAIL}/$propertyId")
                },
                onBottomTabSelected = onBottomTab
            )
        }

        // Pantalla: Notificaciones
        composable(AppRoutes.NOTIFICATIONS) {
            val viewModel: NotificationsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return NotificationsViewModel(Repositories.notificaciones) as T
                    }
                }
            )
            NotificationsScreen(
                reselectTrigger = notificationsReselectTrigger,
                viewModel = viewModel,
                onNavigate = { route ->
                    when {
                        route == "account_management/current_plan" -> {
                            navController.navigate(AppRoutes.ACCOUNT_ADMIN)
                        }
                        route.startsWith("publication_detail/") -> {
                            val id = route.removePrefix("publication_detail/")
                            if (id.isNotEmpty()) {
                                navController.navigate("${AppRoutes.PUBLICACION_DETAIL}/$id")
                            }
                        }
                        else -> {
                            navController.navigate(route)
                        }
                    }
                },
                onBottomTabSelected = onBottomTab
            )
        }

        // Pantalla: Perfil
        composable(AppRoutes.PROFILE) {
            ProfileScreen(
                reselectTrigger = profileReselectTrigger,
                onMyListingsClick = { navController.navigate(AppRoutes.MY_LISTINGS) },
                onAccountClick = { navController.navigate(AppRoutes.ACCOUNT_ADMIN) },
                onAdminPanelClick = {
                    navController.navigate(AppRoutes.ADMIN_PANEL)
                },
                onTermsClick = { navController.navigate(AppRoutes.TERMS_AND_CONDITIONS) },
                onPrivacyClick = { navController.navigate(AppRoutes.PRIVACY_POLICY) },
                onLogoutClick = {
                    // NUEVO: Borrar el token de la API antes de cerrar sesión
                    TokenManager.borrarTokenActual(context, scope) {
                        sessionManager.clearSession()
                        CurrentUserStore.clear()
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
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
                onCreateListingClick = irACrearAnuncio,
                onEditListingClick = { id -> navController.navigate("create_listing_edit/$id") },
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

        // Flujo: Crear anuncio
        navigation(
            route = AppRoutes.CREATE_LISTING_GRAPH,
            startDestination = AppRoutes.CREATE_LISTING_STEP1
        ) {
            composable(AppRoutes.CREATE_LISTING_STEP1) { entry ->
                val canCreate = remember { canCreateListings(CurrentUserStore.roleOrNull()) }
                LaunchedEffect(canCreate) {
                    if (!canCreate) navController.popBackStack(AppRoutes.PROFILE, inclusive = false)
                }
                if (canCreate) {
                    val parent = remember(entry) {
                        navController.getBackStackEntry(AppRoutes.CREATE_LISTING_GRAPH)
                    }
                    val vm: CreateListingViewModel = viewModel(parent)
                    CreateListingStep1Screen(
                        viewModel = vm,
                        onExit = {
                            navController.popBackStack(AppRoutes.CREATE_LISTING_GRAPH, inclusive = true)
                        },
                        onContinue = { navController.navigate(AppRoutes.CREATE_LISTING_STEP2) },
                        onBottomTabSelected = onBottomTab
                    )
                }
            }
            composable(AppRoutes.CREATE_LISTING_STEP2) { entry ->
                val parent = remember(entry) {
                    navController.getBackStackEntry(AppRoutes.CREATE_LISTING_GRAPH)
                }
                val vm: CreateListingViewModel = viewModel(parent)
                CreateListingStep2Screen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(AppRoutes.CREATE_LISTING_STEP3) },
                    onBottomTabSelected = onBottomTab
                )
            }
            composable(AppRoutes.CREATE_LISTING_STEP3) { entry ->
                val parent = remember(entry) {
                    navController.getBackStackEntry(AppRoutes.CREATE_LISTING_GRAPH)
                }
                val vm: CreateListingViewModel = viewModel(parent)
                CreateListingStep3Screen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(AppRoutes.CREATE_LISTING_STEP4) },
                    onBottomTabSelected = onBottomTab
                )
            }
            composable(AppRoutes.CREATE_LISTING_STEP4) { entry ->
                val parent = remember(entry) {
                    navController.getBackStackEntry(AppRoutes.CREATE_LISTING_GRAPH)
                }
                val vm: CreateListingViewModel = viewModel(parent)
                CreateListingStep4Screen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onEditData = {
                        navController.popBackStack(AppRoutes.CREATE_LISTING_STEP1, inclusive = false)
                    },
                    onEditContent = {
                        navController.popBackStack(AppRoutes.CREATE_LISTING_STEP3, inclusive = false)
                    },
                    onContinue = { navController.navigate(AppRoutes.CREATE_LISTING_PREVIEW) },
                    onBottomTabSelected = onBottomTab
                )
            }
            composable(AppRoutes.CREATE_LISTING_PREVIEW) { entry ->
                val parent = remember(entry) {
                    navController.getBackStackEntry(AppRoutes.CREATE_LISTING_GRAPH)
                }
                val vm: CreateListingViewModel = viewModel(parent)
                CreateListingPreviewScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onEdit = {
                        navController.popBackStack(AppRoutes.CREATE_LISTING_STEP1, inclusive = false)
                    },
                    onFinished = {
                        vm.reset()
                        navController.navigate(AppRoutes.MY_LISTINGS) {
                            popUpTo(AppRoutes.CREATE_LISTING_GRAPH) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Flujo: Editar anuncio (mismas pantallas, ViewModel con datos precargados)
        navigation(
            route = AppRoutes.CREATE_LISTING_EDIT,
            startDestination = AppRoutes.CREATE_LISTING_STEP1,
            arguments = listOf(navArgument("listingId") { type = NavType.StringType })
        ) {
            composable(AppRoutes.CREATE_LISTING_STEP1) { entry ->
                val canCreate = remember { canCreateListings(CurrentUserStore.roleOrNull()) }
                LaunchedEffect(canCreate) {
                    if (!canCreate) navController.popBackStack(AppRoutes.PROFILE, inclusive = false)
                }
                if (canCreate) {
                    val parent = remember(entry) {
                        navController.getBackStackEntry(AppRoutes.CREATE_LISTING_EDIT)
                    }
                    val vm: CreateListingViewModel = viewModel(parent)
                    val listingId = parent.arguments?.getString("listingId")?.toIntOrNull()
                    val ctx = LocalContext.current

                    LaunchedEffect(listingId) {
                        if (listingId != null) vm.loadForEdit(ctx, listingId)
                    }

                    val state by vm.state.collectAsState()
                    when {
                        state.loadingEdit -> {
                            Box(
                                Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        state.loadEditError != null -> {
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(state.loadEditError!!)
                                Spacer(Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = {
                                        navController.popBackStack(
                                            AppRoutes.CREATE_LISTING_EDIT,
                                            inclusive = true
                                        )
                                    }
                                ) {
                                    Text(stringResource(R.string.common_back))
                                }
                            }
                        }
                        else -> {
                            CreateListingStep1Screen(
                                viewModel = vm,
                                onExit = {
                                    navController.popBackStack(
                                        AppRoutes.CREATE_LISTING_EDIT,
                                        inclusive = true
                                    )
                                },
                                onContinue = {
                                    navController.navigate(AppRoutes.CREATE_LISTING_STEP2)
                                },
                                onBottomTabSelected = onBottomTab
                            )
                        }
                    }
                }
            }

            composable(AppRoutes.CREATE_LISTING_STEP2) { entry ->
                val parent = remember(entry) {
                    navController.getBackStackEntry(AppRoutes.CREATE_LISTING_EDIT)
                }
                val vm: CreateListingViewModel = viewModel(parent)
                CreateListingStep2Screen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(AppRoutes.CREATE_LISTING_STEP3) },
                    onBottomTabSelected = onBottomTab
                )
            }

            composable(AppRoutes.CREATE_LISTING_STEP3) { entry ->
                val parent = remember(entry) {
                    navController.getBackStackEntry(AppRoutes.CREATE_LISTING_EDIT)
                }
                val vm: CreateListingViewModel = viewModel(parent)
                CreateListingStep3Screen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(AppRoutes.CREATE_LISTING_STEP4) },
                    onBottomTabSelected = onBottomTab
                )
            }

            composable(AppRoutes.CREATE_LISTING_STEP4) { entry ->
                val parent = remember(entry) {
                    navController.getBackStackEntry(AppRoutes.CREATE_LISTING_EDIT)
                }
                val vm: CreateListingViewModel = viewModel(parent)
                CreateListingStep4Screen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onEditData = {
                        navController.popBackStack(AppRoutes.CREATE_LISTING_STEP1, inclusive = false)
                    },
                    onEditContent = {
                        navController.popBackStack(AppRoutes.CREATE_LISTING_STEP3, inclusive = false)
                    },
                    onContinue = { navController.navigate(AppRoutes.CREATE_LISTING_PREVIEW) },
                    onBottomTabSelected = onBottomTab
                )
            }

            composable(AppRoutes.CREATE_LISTING_PREVIEW) { entry ->
                val parent = remember(entry) {
                    navController.getBackStackEntry(AppRoutes.CREATE_LISTING_EDIT)
                }
                val vm: CreateListingViewModel = viewModel(parent)
                CreateListingPreviewScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onEdit = {
                        navController.popBackStack(AppRoutes.CREATE_LISTING_STEP1, inclusive = false)
                    },
                    onFinished = {
                        vm.reset()
                        navController.navigate(AppRoutes.MY_LISTINGS) {
                            popUpTo(AppRoutes.CREATE_LISTING_EDIT) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Pantalla: Detalle de publicación (única ruta)
        composable(
            route = "${AppRoutes.PUBLICACION_DETAIL}/{publicacionId}",
            arguments = listOf(navArgument("publicacionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val publicacionId = backStackEntry.arguments?.getString("publicacionId") ?: ""

            DetailPublicacionScreen(
                publicacionId = publicacionId,
                onBackClick = { navController.popBackStack() },
                onReportClick = {
                    if (!sessionManager.isLoggedIn()) {
                        navController.navigate(AppRoutes.LOGIN)
                    } else {
                        navController.navigate("${AppRoutes.REPORT_GRAPH}/$publicacionId")
                    }
                },
                onEditClick = {
                    navController.navigate("create_listing_edit/$publicacionId")
                },
                onSellerClick = { sellerId ->
                    openUserProfile(sellerId)
                }
            )
        }

        // Pantalla: Perfil de otro usuario (anunciante, quien reportó o cuenta del panel)
        composable(
            route = "${AppRoutes.USER_PROFILE}/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            UserProfileScreen(
                userId = userId,
                onBackClick = { navController.popBackStack() },
                onPropertyClick = { propertyId ->
                    navController.navigate("${AppRoutes.PUBLICACION_DETAIL}/$propertyId")
                },
                onManageAccountClick = {
                    // Si se llegó desde el detalle de esa cuenta, solo se regresa (no se apilan pantallas)
                    val previous = navController.previousBackStackEntry?.destination?.route
                    if (previous?.startsWith(AppRoutes.ADMIN_USER_DETAIL) == true) {
                        navController.popBackStack()
                    } else {
                        navController.navigate("${AppRoutes.ADMIN_USER_DETAIL}/$userId")
                    }
                }
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

        // Pantalla: Usuarios (Admin). Lista real con búsqueda, filtros por rol y estatus, y paginación
        composable(AppRoutes.ADMIN_USERS) {
            UsersListScreen(
                onBackClick = { navController.popBackStack() },
                onUserClick = { userId ->
                    navController.navigate("${AppRoutes.ADMIN_USER_DETAIL}/$userId")
                },
                onBottomTabSelected = onBottomTab
            )
        }

        // Pantalla: Detalle de usuario (Admin), solo lectura
        composable(
            route = "${AppRoutes.ADMIN_USER_DETAIL}/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserDetailScreen(
                userId = userId,
                onBackClick = { navController.popBackStack() },
                onViewProfileClick = {
                    // Igual que arriba: si venimos del perfil, regresamos a él
                    val previous = navController.previousBackStackEntry?.destination?.route
                    if (previous?.startsWith(AppRoutes.USER_PROFILE) == true) {
                        navController.popBackStack()
                    } else {
                        userId.toIntOrNull()?.let { openUserProfile(it) }
                    }
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
                },
                onUserClick = { userId -> openUserProfile(userId) }
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
                },
                onAdvertiserClick = { userId -> openUserProfile(userId) }
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

        // Pantalla: Mapa de inmuebles
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

        // Flujo: Reportar publicación (el ViewModel se comparte entre las dos pantallas)
        navigation(
            route = "${AppRoutes.REPORT_GRAPH}/{publicacionId}",
            startDestination = AppRoutes.REPORT_CATEGORY,
            arguments = listOf(navArgument("publicacionId") { type = NavType.StringType })
        ) {
            composable(AppRoutes.REPORT_CATEGORY) { entry ->
                // El id llega al grafo padre, no a las pantallas hijas
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry("${AppRoutes.REPORT_GRAPH}/{publicacionId}")
                }
                val publicacionId = parentEntry.arguments?.getString("publicacionId") ?: ""
                val reportViewModel: ReportViewModel = viewModel(parentEntry)

                LaunchedEffect(publicacionId) {
                    reportViewModel.initPublicacion(publicacionId)
                }

                ReportCategoryScreen(
                    viewModel = reportViewModel,
                    onBackClick = { navController.popBackStack() },
                    onCategorySelected = { navController.navigate(AppRoutes.REPORT_REASON) }
                )
            }

            composable(AppRoutes.REPORT_REASON) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry("${AppRoutes.REPORT_GRAPH}/{publicacionId}")
                }
                val publicacionId = parentEntry.arguments?.getString("publicacionId") ?: ""
                val reportViewModel: ReportViewModel = viewModel(parentEntry)

                ReportReasonScreen(
                    viewModel = reportViewModel,
                    publicacionId = publicacionId,
                    onBackClick = { navController.popBackStack() },
                    onReportSubmitted = {
                        // Cierra todo el flujo y regresa al detalle de la publicación
                        navController.popBackStack("${AppRoutes.REPORT_GRAPH}/{publicacionId}", inclusive = true)
                    }
                )
            }
        }

        composable(AppRoutes.ACCOUNT_ADMIN) {
            AccountAdminRoute(
                viewModel = viewModel(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlans = { navController.navigate(AppRoutes.SUBSCRIPTION) },
                onNavigateToLogin = { msg ->
                    CurrentUserStore.clear()
                    sessionManager.clearSession()
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBottomTabSelected = onBottomTab
            )
        }

        // Planes: acepta ?motivo=crear cuando viene de "Crear anuncio"
        composable(
            route = "${AppRoutes.SUBSCRIPTION}?motivo={motivo}",
            arguments = listOf(navArgument("motivo") { type = NavType.StringType; defaultValue = "" })
        ) { entry ->
            val motivoCrear = entry.arguments?.getString("motivo") == "crear"
            ChoosePlanScreen(
                motivoCrear = motivoCrear,
                onBackClick = {
                    if (motivoCrear) navController.popBackStack(AppRoutes.PROFILE, inclusive = false)
                    else navController.popBackStack()
                },
                onPlanSelected = { plan ->
                    navController.navigate("${AppRoutes.CHECKOUT}/${plan.id}?motivo=${if (motivoCrear) "crear" else ""}")
                },
                onFreeTrialActivated = {
                    navController.popBackStack(AppRoutes.ACCOUNT_ADMIN, inclusive = false)
                }
            )
        }

        // Checkout: al pagar, refresca el usuario y decide a dónde regresar
        composable(
            route = "${AppRoutes.CHECKOUT}/{planId}?motivo={motivo}",
            arguments = listOf(
                navArgument("planId") { type = NavType.IntType },
                navArgument("motivo") { type = NavType.StringType; defaultValue = "" }
            )
        ) { entry ->
            val motivoCrear = entry.arguments?.getString("motivo") == "crear"
            CheckoutScreen(
                planId = entry.arguments?.getInt("planId") ?: 0,
                onBackClick = {
                    if (motivoCrear) navController.popBackStack(AppRoutes.PROFILE, inclusive = false)
                    else navController.popBackStack()
                },
                onSuccessFinished = {
                    scope.launch {
                        runCatching { CurrentUserStore.refresh(authApi, sessionManager) }
                        if (motivoCrear) {
                            navController.navigate(AppRoutes.CREATE_LISTING_GRAPH) {
                                popUpTo(AppRoutes.SUBSCRIPTION) { inclusive = true }
                            }
                        } else {
                            navController.popBackStack(AppRoutes.ACCOUNT_ADMIN, inclusive = false)
                        }
                    }
                }
            )
        }
    }
}
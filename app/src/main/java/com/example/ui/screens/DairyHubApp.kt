@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.font.FontStyle
import android.content.Intent
import android.net.Uri
import com.example.data.model.*
import com.example.ui.theme.GlassySky
import com.example.ui.theme.GlassyTeal
import com.example.ui.viewmodel.DairyViewModel
import com.example.ui.viewmodel.Screen
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DairyHubApp(viewModel: DairyViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val activeUser by viewModel.activeUser.collectAsStateWithLifecycle()
    val unreadNotifications by viewModel.unreadNotifications.collectAsStateWithLifecycle()

    var showNotificationsDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                is Screen.Auth -> {
                    AuthScreen(viewModel = viewModel)
                }
                else -> {
                    // Main app shell with navigation
                    ResponsiveAppLayout(
                        viewModel = viewModel,
                        activeUser = activeUser,
                        unreadCount = unreadNotifications.size,
                        onNotificationClick = { showNotificationsDialog = true }
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                            },
                            label = "screen_transition"
                        ) { target ->
                            when (target) {
                                Screen.Dashboard -> DashboardScreen(viewModel)
                                Screen.Customers -> CustomersScreen(viewModel)
                                Screen.Collection -> CollectionScreen(viewModel)
                                Screen.Products -> ProductsScreen(viewModel)
                                Screen.Sales -> SalesScreen(viewModel)
                                Screen.Payments -> PaymentsScreen(viewModel)
                                Screen.Expenses -> ExpensesScreen(viewModel)
                                Screen.Reports -> ReportsScreen(viewModel)
                                Screen.Settings -> SettingsScreen(viewModel)
                                Screen.AboutUs -> AboutUsScreen(viewModel)
                                else -> DashboardScreen(viewModel)
                            }
                        }
                    }
                }
            }

            // Global Notification Center Drawer/Dialog
            if (showNotificationsDialog) {
                NotificationsDialog(
                    viewModel = viewModel,
                    onDismiss = { showNotificationsDialog = false }
                )
            }
        }
    }
}

@Composable
fun ResponsiveAppLayout(
    viewModel: DairyViewModel,
    activeUser: UserEntity?,
    unreadCount: Int,
    onNotificationClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    val navItems = listOf(
        Triple(Screen.Dashboard, Icons.Default.Dashboard, "Dashboard"),
        Triple(Screen.Collection, Icons.Default.WaterDrop, "Collection"),
        Triple(Screen.Customers, Icons.Default.People, "Customers"),
        Triple(Screen.Products, Icons.Default.Category, "Products"),
        Triple(Screen.Sales, Icons.Default.ReceiptLong, "POS Sales"),
        Triple(Screen.Payments, Icons.Default.Payments, "Payments"),
        Triple(Screen.Expenses, Icons.Default.LocalShipping, "Expenses"),
        Triple(Screen.Reports, Icons.Default.Analytics, "Reports"),
        Triple(Screen.Settings, Icons.Default.Settings, "Settings")
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 650.dp

        if (isTablet) {
            // Horizontal layout for Tablets / Large Screens
            Row(modifier = Modifier.fillMaxSize()) {
                // Navigation Rail
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight(),
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            DairyHubLogo(
                                logoSize = 52.dp,
                                showText = false,
                                textColor = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Text(
                                    text = "Dairy",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Hub",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = LogoGreen
                                )
                            }
                        }
                    }
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    navItems.forEach { (screen, icon, label) ->
                        NavigationRailItem(
                            selected = currentScreen == screen,
                            onClick = { viewModel.navigateTo(screen) },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 11.sp, overflow = TextOverflow.Ellipsis) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))

                    // Active User Profile Icon / Logout
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Screen Contents
                Column(modifier = Modifier.weight(1f)) {
                    // Tablet Top Header
                    TopAppBarTablet(
                        viewModel = viewModel,
                        activeUser = activeUser,
                        unreadCount = unreadCount,
                        onNotificationClick = onNotificationClick
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        content()
                    }
                }
            }
        } else {
            // Vertical Layout for Mobile Devices
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header Bar
                TopAppBarMobile(
                    viewModel = viewModel,
                    activeUser = activeUser,
                    unreadCount = unreadCount,
                    onNotificationClick = onNotificationClick
                )

                // Screen Contents
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }

                // Bottom Navigation Bar (Sleek Floating iOS 26 Glass Dock)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 2.dp)
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val mobileItems = listOf(
                                Triple(Screen.Dashboard, Icons.Default.Home, "Home"),
                                Triple(Screen.Collection, Icons.Default.WaterDrop, "Collect"),
                                Triple(Screen.Sales, Icons.Default.ReceiptLong, "Sales"),
                                Triple(Screen.Customers, Icons.Default.People, "Farmers"),
                                Triple(Screen.Settings, Icons.Default.Settings, "Settings")
                            )

                            mobileItems.forEach { (screen, icon, label) ->
                                val selected = currentScreen == screen
                                val activeColor = MaterialTheme.colorScheme.primary
                                val inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable { viewModel.navigateTo(screen) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = if (selected) activeColor else inactiveColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selected) activeColor else inactiveColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopAppBarMobile(
    viewModel: DairyViewModel,
    activeUser: UserEntity?,
    unreadCount: Int,
    onNotificationClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DairyHubLogo(
                    logoSize = 38.dp,
                    showText = false,
                    textColor = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Dairy",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Hub",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = LogoGreen
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Language Selector
                var showLanguageDialog by remember { mutableStateOf(false) }
                val language by viewModel.language.collectAsStateWithLifecycle()
                IconButton(onClick = { showLanguageDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Select Language",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (showLanguageDialog) {
                    val context = LocalContext.current
                    AlertDialog(
                        onDismissRequest = { showLanguageDialog = false },
                        title = { Text(L.t("Select Language")) },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                listOf("English", "Hindi (हिन्दी)", "Punjabi (ਪੰਜਾਬੀ)", "Marathi (मराठी)").forEach { opt ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.setLanguage(opt)
                                                showLanguageDialog = false
                                                ToastUtil.show(context, "Language switched to $opt")
                                            }
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = language == opt,
                                            onClick = {
                                                viewModel.setLanguage(opt)
                                                showLanguageDialog = false
                                                ToastUtil.show(context, "Language switched to $opt")
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00A38B))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(opt, fontSize = 15.sp)
                                    }
                                }
                            }
                        },
                        confirmButton = {}
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Theme Toggle
                val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
                IconButton(onClick = { viewModel.toggleTheme() }) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Notification Bell with Badge
                IconButton(onClick = onNotificationClick) {
                    Box {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Profile Dialog Launcher / Logout
                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.testTag("logout_mobile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun TopAppBarTablet(
    viewModel: DairyViewModel,
    activeUser: UserEntity?,
    unreadCount: Int,
    onNotificationClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "DairyHub ERP v1.0",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Role Access: ${activeUser?.role ?: "Staff"} (${activeUser?.fullName ?: "Guest"})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Connection Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Local Sync Offline-First",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Language Selector
                var showLanguageDialog by remember { mutableStateOf(false) }
                val language by viewModel.language.collectAsStateWithLifecycle()
                IconButton(onClick = { showLanguageDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Select Language",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (showLanguageDialog) {
                    val context = LocalContext.current
                    AlertDialog(
                        onDismissRequest = { showLanguageDialog = false },
                        title = { Text(L.t("Select Language")) },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                listOf("English", "Hindi (हिन्दी)", "Punjabi (ਪੰਜਾਬੀ)", "Marathi (मराठी)").forEach { opt ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.setLanguage(opt)
                                                showLanguageDialog = false
                                                ToastUtil.show(context, "Language switched to $opt")
                                            }
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = language == opt,
                                            onClick = {
                                                viewModel.setLanguage(opt)
                                                showLanguageDialog = false
                                                ToastUtil.show(context, "Language switched to $opt")
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00A38B))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(opt, fontSize = 15.sp)
                                    }
                                }
                            }
                        },
                        confirmButton = {}
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = onNotificationClick) {
                    Box {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ======================================================
// MODULE 1: AUTHENTICATION SCREEN
// ======================================================
@Composable
fun AuthScreen(viewModel: DairyViewModel) {
    var isLoginMode by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Owner") }
    var rememberMe by remember { mutableStateOf(true) }

    val rolesList = listOf("Owner", "Manager", "Staff", "Milk Collector", "Accountant")
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val successMsg by viewModel.successMessage.collectAsStateWithLifecycle()

    var showOtpAnimation by remember { mutableStateOf(false) }
    var otpSent by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        val dp by viewModel.dairyProfile.collectAsStateWithLifecycle()
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .widthIn(max = 420.dp)
                    .padding(vertical = 12.dp)
                    .testTag("auth_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo (Sleek iOS 26 style branding)
                DairyHubLogo(
                    logoSize = 120.dp,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Error / Success banners
                if (authError != null) {
                    Text(
                        text = authError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .testTag("auth_error_text"),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (successMsg != null) {
                    Text(
                        text = successMsg ?: "",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (!otpSent) {
                    // Standard Fields
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_username_field"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_field"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isLoginMode) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Role Selector Box
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedRole,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Assigned Access Role") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                rolesList.forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role) },
                                        onClick = {
                                            selectedRole = role
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        // Remember Me / Forgot Password
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it }
                                )
                                Text("Remember me", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }

                            TextButton(
                                onClick = {
                                    if (username.isNotEmpty()) {
                                        otpSent = true
                                    } else {
                                        viewModel.authError.value = "Enter username first to request OTP reset"
                                    }
                                }
                            ) {
                                Text("Forgot Password?", fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Action Button
                    Button(
                        onClick = {
                            if (isLoginMode) {
                                viewModel.login(username, password, rememberMe)
                            } else {
                                viewModel.register(username, password, fullName, selectedRole, phone, email)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_action_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isLoginMode) "Log In Securely" else "Register Enterprise Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = {
                            isLoginMode = !isLoginMode
                            viewModel.authError.value = null
                            viewModel.successMessage.value = null
                        }
                    ) {
                        Text(
                            text = if (isLoginMode) "New to DairyHub? Create an owner account" else "Have an account? Log in",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Bottom hints for Seed Demo Login
                    if (isLoginMode) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("💡 Seed Accounts (Demo access):", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                Text("Owner/Admin: owner / owner123", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                Text("Manager: manager / manager123", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                Text("Collector: collector / collector123", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            }
                        }
                    }
                } else {
                    // Forgot Password OTP flow simulation
                    Text(
                        text = "🔒 Enter OTP Verification Code",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "A temporary security OTP has been dispatched to $username registered endpoint.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { if (it.length <= 6) otpCode = it },
                        label = { Text("6-Digit OTP Code") },
                        leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (otpCode.length >= 4) {
                                otpSent = false
                                viewModel.successMessage.value = "OTP Verified! Your temporary reset password is: owner123"
                            } else {
                                viewModel.authError.value = "Enter valid 6 digit OTP"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Verify Security OTP", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = { otpSent = false }) {
                        Text("Back to Login", fontSize = 12.sp)
                    }
                }
            }
        }
            
        Spacer(modifier = Modifier.height(16.dp))
        val dpVal = dp ?: com.example.data.model.DairyProfileEntity()
        Text(
            text = "${dpVal.dairyName} | 📞 Support: ${dpVal.contactNumber} | ✉️ Support: ${dpVal.emailAddress}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
}

// ======================================================
// MODULE 2: PREMIUM DASHBOARD SCREEN (Stripe style)
// ======================================================
@Composable
fun DashboardScreen(viewModel: DairyViewModel) {
    // Collect all states reactively
    val collections by viewModel.allCollections.collectAsStateWithLifecycle()
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val payments by viewModel.allPayments.collectAsStateWithLifecycle()
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val rateCharts by viewModel.allRateCharts.collectAsStateWithLifecycle()
    val dairyProfile by viewModel.dairyProfile.collectAsStateWithLifecycle()
    val activeUser by viewModel.activeUser.collectAsStateWithLifecycle()
    val logs by viewModel.recentActivityLogs.collectAsStateWithLifecycle()
    val draftItems by viewModel.draftItems.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    
    val connectedPrinterName by viewModel.connectedPrinterName.collectAsStateWithLifecycle()
    val connectedPrinterAddress by viewModel.connectedPrinterAddress.collectAsStateWithLifecycle()
    val printerSize by viewModel.printerSize.collectAsStateWithLifecycle()
    val printerType by viewModel.printerType.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    
    // Core Local states
    var searchQuery by remember { mutableStateOf("") }
    
    // Accordion Expanded States
    var isRateChartExpanded by remember { mutableStateOf(false) }
    var isPrinterExpanded by remember { mutableStateOf(false) }
    var isInvoicesExpanded by remember { mutableStateOf(false) }
    var isLedgersExpanded by remember { mutableStateOf(false) }
    
    // Form Dialog States
    var showQuickCollect by remember { mutableStateOf(false) }
    var showQuickSale by remember { mutableStateOf(false) }
    var showQuickAddFarmer by remember { mutableStateOf(false) }
    var showQuickAddCustomer by remember { mutableStateOf(false) }
    var showRecordPayment by remember { mutableStateOf(false) }
    var showRecordExpense by remember { mutableStateOf(false) }
    
    // Share/Print Preview States
    var selectedCollectionReceipt by remember { mutableStateOf<MilkCollectionEntity?>(null) }
    var selectedSaleInvoice by remember { mutableStateOf<InvoiceEntity?>(null) }
    var selectedPaymentReceipt by remember { mutableStateOf<PaymentEntity?>(null) }
    var showShareDialogText by remember { mutableStateOf<String?>(null) }
    var activeLedgerProfile by remember { mutableStateOf<CustomerEntity?>(null) }
    
    // 1. Calculate live operational statistics
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    val todayCollections = collections.filter { it.timestamp >= todayStart }
    val todayCollectionQty = todayCollections.sumOf { it.quantity }
    val todayCollectionCost = todayCollections.sumOf { it.amount }
    val averageCollectionRate = if (todayCollectionQty > 0) todayCollectionCost / todayCollectionQty else 0.0
    
    val todaySales = invoices.filter { it.timestamp >= todayStart }
    val todaySalesValue = todaySales.sumOf { it.totalAmount }
    
    val todayExpensesValue = expenses.filter { it.dateMillis >= todayStart }.sumOf { it.amount }
    val netProfit = todaySalesValue - todayExpensesValue - todayCollectionCost
    
    val amCollections = todayCollections.filter { it.shift.equals("Morning", ignoreCase = true) }
    val pmCollections = todayCollections.filter { it.shift.equals("Evening", ignoreCase = true) }
    
    val activeRatesCount = rateCharts.filter { it.isActive }.size
    val pendingFarmerPayments = customers.filter { it.customerType == "Farmer" && it.balance > 0 }.sumOf { it.balance }
    
    // Global Center Detail fallbacks
    val dpVal = dairyProfile ?: com.example.data.model.DairyProfileEntity()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDarkTheme) {
                        listOf(Color(0xFF0F1A1B), Color(0xFF070B0B))
                    } else {
                        listOf(Color(0xFFE6F3F1), Color(0xFFFFFFFF))
                    }
                )
            )
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp, top = 16.dp)
    ) {
        // A. SYSTEM OVERVIEW HEADER & GLOBAL UTILITIES
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) Color(0xFF1E2E2F).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.7f)
                ),
                border = BorderStroke(1.dp, if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.04f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Dynamic Premium Logo Circle
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF00A38B), Color(0xFF005B5C))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dpVal.dairyName.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 20.sp
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dpVal.dairyName,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = L.t("Smart Dairy, Strong Future"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF00A38B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Global details rows
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Text(text = dpVal.contactNumber, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Text(text = dpVal.emailAddress, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Text(text = "UPI ID: " + dpVal.upiId, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Text(text = "Manager: " + dpVal.ownerName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sync & Backup secure SQLite strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF00A38B).copy(alpha = 0.08f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00A38B))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = L.t("SQLite Room Secure Database") + " | " + L.t("Backup Status") + ": " + L.t("Saved locally"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF00A38B)
                        )
                    }
                }
            }
        }

        // B. NOTICE BOARD WIDGET
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) Color(0xFF2C2518) else Color(0xFFFFFBEA)
                ),
                border = BorderStroke(1.dp, Color(0xFFEAA300).copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEAA300).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📢", fontSize = 16.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = L.t("Notice Board"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFEAA300)
                        )
                        Text(
                            text = L.t("Notice: Dairy operations are fully digitalized. Keep the Bluetooth printer connected for immediate receipts."),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // C. GLOBAL INSTANT SEARCH ENGINE
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(L.t("Global Search across Farmers, Customers, and Invoices")) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF00A38B)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("global_search_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00A38B),
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    focusedContainerColor = if (isDarkTheme) Color(0xFF1E2E2F).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.7f),
                    unfocusedContainerColor = if (isDarkTheme) Color(0xFF1E2E2F).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.7f)
                )
            )
        }

        // D. LIVE FLOATING SEARCH RESULT BOX (If active query exists)
        if (searchQuery.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) Color(0xFF1E2E2F) else Color(0xFFFFFFFF)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF00A38B).copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = L.t("Search Results") + " for \"$searchQuery\"",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF00A38B)
                            )
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
                            }
                        }
                        
                        val matchedFarmers = customers.filter { 
                            it.customerType == "Farmer" && (it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true)) 
                        }
                        val matchedBuyers = customers.filter { 
                            it.customerType != "Farmer" && (it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true)) 
                        }
                        val matchedInvoices = invoices.filter {
                            it.invoiceNumber.contains(searchQuery, ignoreCase = true) || it.customerName.contains(searchQuery, ignoreCase = true)
                        }
                        
                        if (matchedFarmers.isEmpty() && matchedBuyers.isEmpty() && matchedInvoices.isEmpty()) {
                            Text(
                                text = L.t("No matches found"),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color.Gray
                            )
                        }
                        
                        if (matchedFarmers.isNotEmpty()) {
                            Text(L.t("Farmers"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                            matchedFarmers.take(3).forEach { farmer ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            activeLedgerProfile = farmer
                                            isLedgersExpanded = true
                                            searchQuery = "" 
                                        }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${farmer.name} (${farmer.code})", fontSize = 13.sp)
                                    Text("Balance: ₹${farmer.balance}", fontSize = 12.sp, color = Color(0xFF00A38B), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        
                        if (matchedBuyers.isNotEmpty()) {
                            Text(L.t("Retail/Wholesale Customers"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                            matchedBuyers.take(3).forEach { buyer ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            activeLedgerProfile = buyer
                                            isLedgersExpanded = true
                                            searchQuery = "" 
                                        }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${buyer.name} (${buyer.code})", fontSize = 13.sp)
                                    Text("Balance: ₹${buyer.balance}", fontSize = 12.sp, color = Color(0xFF00A38B), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        
                        if (matchedInvoices.isNotEmpty()) {
                            Text(L.t("Invoices"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                            matchedInvoices.take(3).forEach { invoice ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            selectedSaleInvoice = invoice
                                            isInvoicesExpanded = true
                                            searchQuery = "" 
                                        }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(invoice.invoiceNumber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text("₹${invoice.totalAmount}", fontSize = 13.sp, color = Color(0xFF00A38B))
                                }
                            }
                        }
                    }
                }
            }
        }



        // F. LIQUID OPERATIONS QUICK ACTION PANEL (LIQUID DOCK)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = L.t("Quick Actions Panel"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Grid containing 6 major operational buttons
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showQuickCollect = true },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A38B))
                        ) {
                            Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(L.t("Quick Collect"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showQuickSale = true },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0091FF))
                        ) {
                            Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(L.t("POS Sale"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showQuickAddFarmer = true },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(L.t("Add Farmer"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showQuickAddCustomer = true },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F8C8D))
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(L.t("Add Customer"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showRecordPayment = true },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E44AD))
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(L.t("Record Payment"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showRecordExpense = true },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD35400))
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(L.t("Record Expense"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // G. COLLAPSIBLE ACCORDION PANELS FOR OPERATIONAL MODULES
        
        // MODULE 1: MILK RATE CHART MANAGEMENT
        item {
            AccordionWrapper(
                title = L.t("Milk Rate Chart Management"),
                isExpanded = isRateChartExpanded,
                onToggle = { isRateChartExpanded = !isRateChartExpanded },
                isDarkTheme = isDarkTheme
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text(L.t("Configure FAT/SNF base rates, edit manually, or import/export via CSV format (Excel compatible)."), fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Auto Rate Preview Calculator
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF1E2E2F) else Color(0xFFE6F3F1)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(L.t("Auto Rate Calculator Preview"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF00A38B))
                            Spacer(modifier = Modifier.height(8.dp))
                            var calcType by remember { mutableStateOf("Cow") }
                            var calcFat by remember { mutableStateOf("4.0") }
                            var calcSnf by remember { mutableStateOf("8.5") }
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Cow", "Buffalo").forEach { type ->
                                    val sel = calcType == type
                                    Button(
                                        onClick = { calcType = type },
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = if (sel) Color(0xFF00A38B) else Color.Gray.copy(alpha = 0.2f))
                                    ) {
                                        Text(type, fontSize = 11.sp, color = if (sel) Color.White else MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = calcFat,
                                    onValueChange = { calcFat = it },
                                    label = { Text("FAT (%)", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    textStyle = TextStyle(fontSize = 12.sp)
                                )
                                OutlinedTextField(
                                    value = calcSnf,
                                    onValueChange = { calcSnf = it },
                                    label = { Text("SNF (%)", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    textStyle = TextStyle(fontSize = 12.sp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val fatD = calcFat.toDoubleOrNull() ?: 0.0
                            val snfD = calcSnf.toDoubleOrNull() ?: 0.0
                            val pairRate = viewModel.getClosestRateFromChart(calcType, fatD, snfD)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(L.t("Computed Rate") + ":", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("₹${String.format("%.2f", pairRate.first)} / L", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF00A38B))
                            }
                            Text(
                                text = if (pairRate.second) "✓ Exact Chart Match" else "⚠ Formula Approximation Fallback",
                                fontSize = 9.sp,
                                color = if (pairRate.second) Color(0xFF00A38B) else Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Excel CSV Export & Import box
                    var csvImportText by remember { mutableStateOf("") }
                    Text(L.t("Excel Excel Template CSV Import / Export Box"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = csvImportText,
                        onValueChange = { csvImportText = it },
                        placeholder = { Text("fat,snf,rate\n4.2,8.5,35.0\n4.5,9.0,38.5") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        textStyle = TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val ok = viewModel.importRatesFromCsv("Cow", csvImportText)
                                if (ok) {
                                    Toast.makeText(context, "Successfully imported Cow Rates!", Toast.LENGTH_SHORT).show()
                                    csvImportText = ""
                                } else {
                                    Toast.makeText(context, "Error: Invalid CSV columns. Use: fat,snf,rate", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).height(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Import Cow CSV", fontSize = 10.sp)
                        }
                        Button(
                            onClick = {
                                val ok = viewModel.importRatesFromCsv("Buffalo", csvImportText)
                                if (ok) {
                                    Toast.makeText(context, "Successfully imported Buffalo Rates!", Toast.LENGTH_SHORT).show()
                                    csvImportText = ""
                                } else {
                                    Toast.makeText(context, "Error: Invalid CSV columns. Use: fat,snf,rate", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).height(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Import Buffalo CSV", fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = {
                                csvImportText = "fat,snf,rate\n4.0,8.5,32.0\n4.5,8.8,36.5\n5.0,9.0,41.0\n6.0,9.2,48.0"
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Load Template", fontSize = 11.sp)
                        }
                        TextButton(
                            onClick = {
                                val exported = rateCharts.joinToString("\n") { "${it.fat},${it.snf},${it.rate}" }
                                csvImportText = "fat,snf,rate\n$exported"
                                Toast.makeText(context, "Exported current rates!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Export Current CSV", fontSize = 11.sp)
                        }
                        TextButton(
                            onClick = {
                                viewModel.clearRateChart("Cow")
                                viewModel.clearRateChart("Buffalo")
                                Toast.makeText(context, "Cleared all rates!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset All Charts", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // List of current active rates
                    Text(L.t("Active Rate Grid Rows") + " (${rateCharts.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp).verticalScroll(rememberScrollState())) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            rateCharts.forEach { row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isDarkTheme) Color.Black.copy(alpha = 0.2f) else Color.White, RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${row.milkType} | FAT: ${row.fat}% | SNF: ${row.snf}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("₹${row.rate}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00A38B))
                                        IconButton(onClick = { viewModel.deleteRate(row) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // MODULE 2: BLUETOOTH THERMAL PRINTER STATION
        item {
            AccordionWrapper(
                title = L.t("Bluetooth Thermal Printer Station"),
                isExpanded = isPrinterExpanded,
                onToggle = { isPrinterExpanded = !isPrinterExpanded },
                isDarkTheme = isDarkTheme
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(L.t("Printer Status"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = if (connectedPrinterAddress.isNotEmpty()) "✓ Connected: $connectedPrinterName" else "⚠ Not Connected",
                                fontSize = 11.sp,
                                color = if (connectedPrinterAddress.isNotEmpty()) Color(0xFF00A38B) else Color.Red
                            )
                        }

                        // Connect/Disconnect Toggle
                        Button(
                            onClick = {
                                if (connectedPrinterAddress.isNotEmpty()) {
                                    viewModel.disconnectPrinter()
                                    Toast.makeText(context, "Bluetooth printer disconnected", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Trigger quick connection
                                    viewModel.connectPrinter("Thermal_Printer_58G", "00:11:22:33:44:55")
                                    Toast.makeText(context, "Connected to Thermal_Printer_58G!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (connectedPrinterAddress.isNotEmpty()) Color.Red else Color(0xFF00A38B)),
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (connectedPrinterAddress.isNotEmpty()) L.t("Disconnect") else L.t("Connect"), fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Printer configuration controls
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        // Paper size selector
                        Button(
                            onClick = { viewModel.setPrinterSize("2inch") },
                            modifier = Modifier.weight(1f).height(34.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (printerSize == "2inch" || printerSize.contains("58")) Color(0xFF00A38B) else Color.Gray.copy(alpha = 0.2f))
                        ) {
                            Text("58mm (2 Inch)", fontSize = 10.sp, color = if (printerSize == "2inch" || printerSize.contains("58")) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                        Button(
                            onClick = { viewModel.setPrinterSize("3inch") },
                            modifier = Modifier.weight(1f).height(34.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (printerSize == "3inch" || printerSize.contains("80")) Color(0xFF00A38B) else Color.Gray.copy(alpha = 0.2f))
                        ) {
                            Text("80mm (3 Inch)", fontSize = 10.sp, color = if (printerSize == "3inch" || printerSize.contains("80")) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Test printer button
                    Button(
                        onClick = {
                            val testFields = mapOf(
                                "Receipt Type" to "Test Print Slips",
                                "Center Name" to dpVal.dairyName,
                                "Baud Rate" to "9600 bps",
                                "Database Status" to "Active / Connected",
                                "App Version" to "v4.1.2 Premium"
                            )
                            val simulatedReceipt = PrinterHelper.generateReceiptText(
                                title = "DairyHub Test Slip",
                                centerName = dpVal.dairyName,
                                centerCode = "DH-800",
                                fields = testFields,
                                size = printerSize
                            )
                            showShareDialogText = simulatedReceipt
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A085)),
                        modifier = Modifier.fillMaxWidth().height(38.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(L.t("Test Print Slip"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Fast print triggers of last entries
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                val lastC = collections.lastOrNull()
                                if (lastC != null) {
                                    val farmer = customers.find { it.id == lastC.customerId }
                                    val receipt = PrinterHelper.generateReceiptText(
                                        title = "Milk Collection Receipt",
                                        centerName = dpVal.dairyName,
                                        centerCode = "DH-800",
                                        fields = mapOf(
                                            "Farmer" to (farmer?.name ?: "Unknown"),
                                            "Shift" to lastC.shift,
                                            "Milk Type" to lastC.milkType,
                                            "Qty (Liters)" to "${lastC.quantity} L",
                                            "FAT (%)" to "${lastC.fat}%",
                                            "SNF (%)" to "${lastC.snf}%",
                                            "CLR" to "${lastC.clr}",
                                            "Rate (₹/L)" to "₹${lastC.rate}",
                                            "Total Amt" to "₹${lastC.amount}"
                                        ),
                                        size = printerSize
                                    )
                                    showShareDialogText = receipt
                                    Toast.makeText(context, "Printed last milk collection receipt!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "No collection entries found to print!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).height(34.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Print Last Collection", fontSize = 10.sp)
                        }

                        Button(
                            onClick = {
                                val lastInv = invoices.lastOrNull()
                                if (lastInv != null) {
                                    val receipt = PrinterHelper.generateReceiptText(
                                        title = "POS Sale Invoice",
                                        centerName = dpVal.dairyName,
                                        centerCode = "DH-800",
                                        fields = mapOf(
                                            "Invoice No" to lastInv.invoiceNumber,
                                            "Customer" to lastInv.customerName,
                                            "Subtotal" to "₹${lastInv.subtotal}",
                                            "GST" to "₹${lastInv.gst}",
                                            "Discount" to "₹${lastInv.discount}",
                                            "Total Paid" to "₹${lastInv.totalAmount}",
                                            "Payment Status" to lastInv.paymentStatus
                                        ),
                                        size = printerSize
                                    )
                                    showShareDialogText = receipt
                                    Toast.makeText(context, "Printed last POS sale invoice!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "No sales invoices found to print!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).height(34.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Print Last Sale", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // MODULE 3: INVOICES & RECEIPTS HUB
        item {
            AccordionWrapper(
                title = L.t("Invoices & Receipts Hub"),
                isExpanded = isInvoicesExpanded,
                onToggle = { isInvoicesExpanded = !isInvoicesExpanded },
                isDarkTheme = isDarkTheme
            ) {
                var selectedTab by remember { mutableStateOf("Collections") }
                
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    // Tabs
                    Row(modifier = Modifier.fillMaxWidth().height(36.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Collections", "Sales Invoices", "Payments").forEach { tab ->
                            val sel = selectedTab == tab
                            Button(
                                onClick = { selectedTab = tab },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (sel) Color(0xFF00A38B) else Color.Gray.copy(alpha = 0.15f))
                            ) {
                                Text(L.t(tab), fontSize = 10.sp, color = if (sel) Color.White else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Listing the records based on tab
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp).verticalScroll(rememberScrollState())) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            when (selectedTab) {
                                "Collections" -> {
                                    collections.reversed().forEach { item ->
                                        val farmer = customers.find { it.id == item.customerId }
                                        ReceiptRow(
                                            title = "${farmer?.name ?: "Farmer #${item.customerId}"} (${item.milkType})",
                                            meta = "Shift: ${item.shift} | ${item.quantity}L @ ₹${item.rate}",
                                            amount = "₹${item.amount}",
                                            onPrint = {
                                                selectedCollectionReceipt = item
                                                val fields = mapOf(
                                                    "Farmer" to (farmer?.name ?: "Unknown"),
                                                    "Shift" to item.shift,
                                                    "Milk Type" to item.milkType,
                                                    "Qty (L)" to "${item.quantity} L",
                                                    "FAT (%)" to "${item.fat}%",
                                                    "SNF (%)" to "${item.snf}%",
                                                    "CLR" to "${item.clr}",
                                                    "Rate" to "₹${item.rate}/L",
                                                    "Total Amount" to "₹${item.amount}"
                                                )
                                                showShareDialogText = PrinterHelper.generateReceiptText("Collection Receipt", dpVal.dairyName, "DH-800", fields, printerSize)
                                            }
                                        )
                                    }
                                }
                                "Sales Invoices" -> {
                                    invoices.reversed().forEach { item ->
                                        ReceiptRow(
                                            title = "${item.customerName} (${item.invoiceNumber})",
                                            meta = "Subtotal: ₹${item.subtotal} | Status: ${item.paymentStatus}",
                                            amount = "₹${item.totalAmount}",
                                            onPrint = {
                                                selectedSaleInvoice = item
                                                val fields = mapOf(
                                                    "Invoice" to item.invoiceNumber,
                                                    "Customer" to item.customerName,
                                                    "Subtotal" to "₹${item.subtotal}",
                                                    "GST" to "₹${item.gst}",
                                                    "Discount" to "₹${item.discount}",
                                                    "Total" to "₹${item.totalAmount}",
                                                    "Payment" to item.paymentStatus
                                                )
                                                showShareDialogText = PrinterHelper.generateReceiptText("POS Sale Invoice", dpVal.dairyName, "DH-800", fields, printerSize)
                                            }
                                        )
                                    }
                                }
                                "Payments" -> {
                                    payments.reversed().forEach { item ->
                                        val c = customers.find { it.id == item.customerId }
                                        ReceiptRow(
                                            title = "${c?.name ?: "Client #${item.customerId}"}",
                                            meta = "Method: ${item.paymentMethod} | Notes: ${item.notes}",
                                            amount = "₹${item.amountReceived}",
                                            onPrint = {
                                                selectedPaymentReceipt = item
                                                val fields = mapOf(
                                                    "Client" to (c?.name ?: "Unknown"),
                                                    "Payment Amount" to "₹${item.amountReceived}",
                                                    "Method" to item.paymentMethod,
                                                    "Txn ID" to item.transactionId,
                                                    "Notes" to item.notes
                                                )
                                                showShareDialogText = PrinterHelper.generateReceiptText("Payment Voucher", dpVal.dairyName, "DH-800", fields, printerSize)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // MODULE 4: FARMERS & CUSTOMERS LEDGERS DIRECTORY
        item {
            AccordionWrapper(
                title = L.t("Farmers & Customers Ledgers"),
                isExpanded = isLedgersExpanded,
                onToggle = { isLedgersExpanded = !isLedgersExpanded },
                isDarkTheme = isDarkTheme
            ) {
                var searchDirectoryQuery by remember { mutableStateOf("") }
                var selectedDirTab by remember { mutableStateOf("Farmers") }
                
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().height(36.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Farmers", "Retail Customers").forEach { tab ->
                            val sel = selectedDirTab == tab
                            Button(
                                onClick = { selectedDirTab = tab },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (sel) Color(0xFF00A38B) else Color.Gray.copy(alpha = 0.15f))
                            ) {
                                Text(L.t(tab), fontSize = 10.sp, color = if (sel) Color.White else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchDirectoryQuery,
                        onValueChange = { searchDirectoryQuery = it },
                        placeholder = { Text(L.t("Search name or code...")) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Listing matching directory cards
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp).verticalScroll(rememberScrollState())) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val filtered = customers.filter {
                                val matchesTab = if (selectedDirTab == "Farmers") it.customerType == "Farmer" else it.customerType != "Farmer"
                                val matchesSearch = it.name.contains(searchDirectoryQuery, ignoreCase = true) || it.code.contains(searchDirectoryQuery, ignoreCase = true)
                                matchesTab && matchesSearch
                            }

                            filtered.forEach { profile ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color.Black.copy(alpha = 0.2f) else Color.White),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(profile.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Code: ${profile.code} | Milk: ${profile.milkType}", fontSize = 11.sp, color = Color.Gray)
                                            Text("Ledger Balance: " + if (profile.balance >= 0) "₹${profile.balance} (Due)" else "₹${Math.abs(profile.balance)} (Credit)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (profile.balance >= 0) Color.Red else Color(0xFF00A38B))
                                        }
                                        
                                        Button(
                                            onClick = { activeLedgerProfile = profile },
                                            modifier = Modifier.height(30.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                        ) {
                                            Text(L.t("View Ledger"), fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // E. TODAY'S OPERATIONS HERO SUMMARY & ANALYTICS CHARTS
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) Color(0xFF132223) else Color(0xFFF0FDFB)
                ),
                border = BorderStroke(1.dp, Color(0xFF00A38B).copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = L.t("Today's Operations Summary"),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFF00A38B)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 2x4 Metric Grid for all Today's parameters
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                SmallMetric(title = "Today's Milk", value = "${String.format("%.1f", todayCollectionQty)} L", sub = "Cost: ₹${String.format("%.1f", todayCollectionCost)}", icon = Icons.Default.WaterDrop, Color(0xFF00A38B), isDarkTheme)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                SmallMetric(title = "Daily Sales", value = "₹${String.format("%.1f", todaySalesValue)}", sub = "${todaySales.size} Invoices", icon = Icons.Default.Category, Color(0xFF0091FF), isDarkTheme)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                SmallMetric(title = "Today's Expenses", value = "₹${String.format("%.1f", todayExpensesValue)}", sub = "Center Outflow", icon = Icons.Default.LocalShipping, Color(0xFFFF5252), isDarkTheme)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                SmallMetric(title = "Net Profit", value = "₹${String.format("%.1f", netProfit)}", sub = "Live Live Calculation", icon = Icons.Default.Analytics, Color(0xFF00D16B), isDarkTheme)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                SmallMetric(title = "Milk Rate Summary", value = "₹${String.format("%.2f", averageCollectionRate)}/L", sub = "Avg Collection Rate", icon = Icons.Default.Payments, Color(0xFF8E44AD), isDarkTheme)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                SmallMetric(title = "Pending Payments", value = "₹${String.format("%.1f", pendingFarmerPayments)}", sub = "Awaiting Payouts", icon = Icons.Default.People, Color(0xFFD35400), isDarkTheme)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                SmallMetric(title = "Collection Shift", value = "AM: ${String.format("%.1f", amCollections.sumOf { it.quantity })}L", sub = "PM: ${String.format("%.1f", pmCollections.sumOf { it.quantity })}L", icon = Icons.Default.Settings, Color(0xFF34495E), isDarkTheme)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                SmallMetric(title = "Active Rate Chart", value = "$activeRatesCount Rows", sub = "FAT/SNF Grid Active", icon = Icons.Default.GridOn, Color(0xFF16A085), isDarkTheme)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Line Canvas Chart showing recent trend
                    CollectionsLineChart(collections = collections, isDarkTheme = isDarkTheme)
                }
            }
        }
    }

    // ==============================================================
    // FLOATING CORE DIALOGS & SHEET MODAL FORMS
    // ==============================================================

    // 1. QUICK MILK COLLECTION DIALOG
    if (showQuickCollect) {
        var selectedFarmer by remember { mutableStateOf<CustomerEntity?>(null) }
        var selectedShift by remember { mutableStateOf("Morning") }
        var selectedMilkType by remember { mutableStateOf("Cow") }
        var qtyText by remember { mutableStateOf("") }
        var fatText by remember { mutableStateOf("4.0") }
        var snfText by remember { mutableStateOf("8.5") }
        var clrText by remember { mutableStateOf("28.0") }
        var searchFarmerQuery by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showQuickCollect = false },
            title = { Text(L.t("Quick Milk Collection"), fontWeight = FontWeight.Bold, color = Color(0xFF00A38B)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Search & Select Farmer dropdown
                    Text(L.t("Select Farmer") + ":", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = searchFarmerQuery,
                        onValueChange = { searchFarmerQuery = it },
                        placeholder = { Text("Search farmer name or code") },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    )

                    // Compact select list
                    val matching = customers.filter {
                        it.customerType == "Farmer" && (it.name.contains(searchFarmerQuery, ignoreCase = true) || it.code.contains(searchFarmerQuery, ignoreCase = true))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp).verticalScroll(rememberScrollState())) {
                        Column {
                            matching.forEach { farmer ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (selectedFarmer?.id == farmer.id) Color(0xFF00A38B).copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable { selectedFarmer = farmer }
                                        .padding(8.dp)
                                ) {
                                    Text("${farmer.name} (${farmer.code})", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    if (selectedFarmer != null) {
                        Text("Selected: ${selectedFarmer?.name} (${selectedFarmer?.code})", fontWeight = FontWeight.Bold, color = Color(0xFF00A38B), fontSize = 12.sp)
                    }

                    // Shift & Milk Type selectors
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("Morning", "Evening").forEach { shift ->
                            val sel = selectedShift == shift
                            Button(
                                onClick = { selectedShift = shift },
                                modifier = Modifier.weight(1f).height(32.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (sel) Color(0xFF00A38B) else Color.Gray.copy(alpha = 0.2f))
                            ) {
                                Text(L.t(shift), fontSize = 10.sp, color = if (sel) Color.White else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("Cow", "Buffalo").forEach { type ->
                            val sel = selectedMilkType == type
                            Button(
                                onClick = { selectedMilkType = type },
                                modifier = Modifier.weight(1f).height(32.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (sel) Color(0xFF00A38B) else Color.Gray.copy(alpha = 0.2f))
                            ) {
                                Text(L.t(type), fontSize = 10.sp, color = if (sel) Color.White else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    // Quantity, FAT, SNF, CLR Inputs
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = qtyText,
                            onValueChange = { qtyText = it },
                            label = { Text("Qty (L)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = fatText,
                            onValueChange = { fatText = it },
                            label = { Text("FAT (%)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = snfText,
                            onValueChange = { snfText = it },
                            label = { Text("SNF (%)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = clrText,
                            onValueChange = { clrText = it },
                            label = { Text("CLR") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    // Real-time calculation previews
                    val fVal = fatText.toDoubleOrNull() ?: 0.0
                    val sVal = snfText.toDoubleOrNull() ?: 0.0
                    val qVal = qtyText.toDoubleOrNull() ?: 0.0
                    val cVal = clrText.toDoubleOrNull() ?: 0.0
                    
                    val ratePair = viewModel.getClosestRateFromChart(selectedMilkType, fVal, sVal)
                    val calculatedAmt = qVal * ratePair.first
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF00A38B).copy(alpha = 0.06f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text("Live Preview Stats:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF00A38B))
                        Text("Rate: ₹${String.format("%.2f", ratePair.first)} / Litre", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Total Estimated Amount: ₹${String.format("%.2f", calculatedAmt)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fVal = fatText.toDoubleOrNull()
                        val sVal = snfText.toDoubleOrNull()
                        val qVal = qtyText.toDoubleOrNull()
                        val cVal = clrText.toDoubleOrNull() ?: 28.0
                        
                        if (selectedFarmer == null) {
                            Toast.makeText(context, "Error: Choose a Farmer profile first", Toast.LENGTH_SHORT).show()
                        } else if (fVal == null || sVal == null || qVal == null) {
                            Toast.makeText(context, "Error: Enter valid quantity, FAT and SNF parameters", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addMilkCollection(
                                customerId = selectedFarmer!!.id,
                                shift = selectedShift,
                                fat = fVal,
                                snf = sVal,
                                clr = cVal,
                                quantity = qVal,
                                milkType = selectedMilkType
                            )
                            Toast.makeText(context, "Recorded collection successfully!", Toast.LENGTH_SHORT).show()
                            showQuickCollect = false
                        }
                    }
                ) {
                    Text(L.t("Record Collection"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickCollect = false }) {
                    Text(L.t("Cancel"))
                }
            }
        )
    }

    // 2. QUICK POS SALE / CHECKOUT DIALOG
    if (showQuickSale) {
        var selectedCustomerForSale by remember { mutableStateOf<CustomerEntity?>(null) }
        var searchCustomerSaleQuery by remember { mutableStateOf("") }
        var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
        var saleQty by remember { mutableStateOf("1.0") }
        var discountPercent by remember { mutableStateOf("0.0") }
        var gstPercent by remember { mutableStateOf("18.0") }
        var isSalePaid by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showQuickSale = false },
            title = { Text(L.t("Quick POS Sale Billing"), fontWeight = FontWeight.Bold, color = Color(0xFF0091FF)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Choose Retail Buyer
                    Text(L.t("Select Customer") + ":", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = searchCustomerSaleQuery,
                        onValueChange = { searchCustomerSaleQuery = it },
                        placeholder = { Text("Search buyer or client") },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    )

                    val matchingBuyers = customers.filter {
                        it.customerType != "Farmer" && (it.name.contains(searchCustomerSaleQuery, ignoreCase = true) || it.code.contains(searchCustomerSaleQuery, ignoreCase = true))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp).verticalScroll(rememberScrollState())) {
                        Column {
                            matchingBuyers.forEach { b ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (selectedCustomerForSale?.id == b.id) Color(0xFF0091FF).copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable { selectedCustomerForSale = b }
                                        .padding(6.dp)
                                ) {
                                    Text("${b.name} (${b.code})", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    if (selectedCustomerForSale != null) {
                        Text("Selected Customer: ${selectedCustomerForSale?.name}", fontWeight = FontWeight.Bold, color = Color(0xFF0091FF), fontSize = 12.sp)
                    }

                    // Choose Product
                    Text(L.t("Select Product") + ":", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp).verticalScroll(rememberScrollState())) {
                        Column {
                            products.forEach { p ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (selectedProduct?.id == p.id) Color(0xFF0091FF).copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable { selectedProduct = p }
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(p.name, fontSize = 12.sp)
                                    Text("₹${p.price} / ${p.unit}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    if (selectedProduct != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = saleQty,
                                onValueChange = { saleQty = it },
                                label = { Text("Quantity") },
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    val q = saleQty.toDoubleOrNull() ?: 1.0
                                    viewModel.addProductToDraft(selectedProduct!!, q)
                                    Toast.makeText(context, "Added ${selectedProduct!!.name} to draft list", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Add")
                            }
                        }
                    }

                    // Current Cart Items List
                    Text("Draft items list:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Gray.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        if (draftItems.isEmpty()) {
                            Text("No items added yet", fontSize = 11.sp, color = Color.Gray)
                        } else {
                            draftItems.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${item.productName} x ${item.quantity}", fontSize = 11.sp)
                                    Row {
                                        Text("₹${item.amount}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "X",
                                            modifier = Modifier.clickable { viewModel.removeProductFromDraft(item) },
                                            fontSize = 11.sp,
                                            color = Color.Red,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Discount, GST, Paid Switch
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = discountPercent,
                            onValueChange = { discountPercent = it },
                            label = { Text("Discount (%)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = gstPercent,
                            onValueChange = { gstPercent = it },
                            label = { Text("GST (%)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark Invoice as Paid", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Switch(checked = isSalePaid, onCheckedChange = { isSalePaid = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dP = discountPercent.toDoubleOrNull() ?: 0.0
                        val gP = gstPercent.toDoubleOrNull() ?: 0.0
                        
                        if (selectedCustomerForSale == null) {
                            Toast.makeText(context, "Error: Choose a Buyer customer first", Toast.LENGTH_SHORT).show()
                        } else if (draftItems.isEmpty()) {
                            Toast.makeText(context, "Error: Cart draft is empty. Add products first", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.checkoutInvoice(
                                customerId = selectedCustomerForSale!!.id,
                                customerName = selectedCustomerForSale!!.name,
                                discountPercent = dP,
                                gstPercent = gP,
                                isPaid = isSalePaid
                            )
                            Toast.makeText(context, "Tax POS Invoice generated successfully!", Toast.LENGTH_SHORT).show()
                            showQuickSale = false
                        }
                    }
                ) {
                    Text("Checkout & Invoice")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickSale = false }) {
                    Text(L.t("Cancel"))
                }
            }
        )
    }

    // Call standard Customer register popup if needed
    if (showQuickAddFarmer) {
        QuickAddCustomerDialog(
            viewModel = viewModel,
            onDismiss = { showQuickAddFarmer = false },
            onSuccess = { showQuickAddFarmer = false }
        )
    }

    if (showQuickAddCustomer) {
        QuickAddCustomerDialog(
            viewModel = viewModel,
            onDismiss = { showQuickAddCustomer = false },
            onSuccess = { showQuickAddCustomer = false }
        )
    }

    // 3. RECORD CASH PAYMENT RECEIVED / MADE DIALOG
    if (showRecordPayment) {
        var selectedClientForPay by remember { mutableStateOf<CustomerEntity?>(null) }
        var searchClientPayQuery by remember { mutableStateOf("") }
        var payAmountText by remember { mutableStateOf("") }
        var payMethod by remember { mutableStateOf("Cash") }
        var payNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRecordPayment = false },
            title = { Text(L.t("Record Payout / Payment"), fontWeight = FontWeight.Bold, color = Color(0xFF8E44AD)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(L.t("Select Profile") + ":", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = searchClientPayQuery,
                        onValueChange = { searchClientPayQuery = it },
                        placeholder = { Text("Search farmer or client profile") },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    )

                    val matchingClients = customers.filter {
                        it.name.contains(searchClientPayQuery, ignoreCase = true) || it.code.contains(searchClientPayQuery, ignoreCase = true)
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp).verticalScroll(rememberScrollState())) {
                        Column {
                            matchingClients.forEach { c ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (selectedClientForPay?.id == c.id) Color(0xFF8E44AD).copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable { selectedClientForPay = c }
                                        .padding(6.dp)
                                ) {
                                    Text("${c.name} (${c.code} - ${c.customerType})", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    if (selectedClientForPay != null) {
                        Text("Selected Client: ${selectedClientForPay?.name} (Balance: ₹${selectedClientForPay?.balance})", fontWeight = FontWeight.Bold, color = Color(0xFF8E44AD), fontSize = 11.sp)
                    }

                    OutlinedTextField(
                        value = payAmountText,
                        onValueChange = { payAmountText = it },
                        label = { Text("Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Text(L.t("Payment Method") + ":", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("Cash", "UPI", "Bank Transfer").forEach { m ->
                            val selected = payMethod == m
                            Button(
                                onClick = { payMethod = m },
                                modifier = Modifier.weight(1f).height(32.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (selected) Color(0xFF8E44AD) else Color.Gray.copy(alpha = 0.2f))
                            ) {
                                Text(L.t(m), fontSize = 10.sp, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = payNotes,
                        onValueChange = { payNotes = it },
                        label = { Text("Voucher Description / Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = payAmountText.toDoubleOrNull()
                        if (selectedClientForPay == null) {
                            Toast.makeText(context, "Error: Choose a customer profile first", Toast.LENGTH_SHORT).show()
                        } else if (amt == null || amt <= 0) {
                            Toast.makeText(context, "Error: Enter a valid cash amount", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.receivePayment(
                                customerId = selectedClientForPay!!.id,
                                amount = amt,
                                method = payMethod,
                                notes = payNotes
                            )
                            Toast.makeText(context, "Payment voucher saved successfully!", Toast.LENGTH_SHORT).show()
                            showRecordPayment = false
                        }
                    }
                ) {
                    Text("Save Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecordPayment = false }) {
                    Text(L.t("Cancel"))
                }
            }
        )
    }

    // 4. RECORD EXPENSE DIALOG
    if (showRecordExpense) {
        var expenseCategory by remember { mutableStateOf("Salary") }
        var expenseAmtText by remember { mutableStateOf("") }
        var expenseDescText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRecordExpense = false },
            title = { Text(L.t("Record Center Expense"), fontWeight = FontWeight.Bold, color = Color(0xFFD35400)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Expense Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp).verticalScroll(rememberScrollState())) {
                        Column {
                            listOf("Salary", "Diesel", "Electricity", "Maintenance", "Animal Feed", "Others").forEach { cat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (expenseCategory == cat) Color(0xFFD35400).copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable { expenseCategory = cat }
                                        .padding(6.dp)
                                ) {
                                    Text(cat, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = expenseAmtText,
                        onValueChange = { expenseAmtText = it },
                        label = { Text("Expense Cost (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    OutlinedTextField(
                        value = expenseDescText,
                        onValueChange = { expenseDescText = it },
                        label = { Text("Details / Purpose") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = expenseAmtText.toDoubleOrNull()
                        if (amt == null || amt <= 0) {
                            Toast.makeText(context, "Error: Enter a valid expense cost", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addExpense(expenseCategory, amt, expenseDescText)
                            Toast.makeText(context, "Expense logged successfully!", Toast.LENGTH_SHORT).show()
                            showRecordExpense = false
                        }
                    }
                ) {
                    Text("Save Expense")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecordExpense = false }) {
                    Text(L.t("Cancel"))
                }
            }
        )
    }

    // 5. BLUETOOTH THERMAL PRINTER SIMULATOR / POPUP PREVIEW
    if (showShareDialogText != null) {
        AlertDialog(
            onDismissRequest = { showShareDialogText = null },
            title = { Text(L.t("Receipt Layout Preview"), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.Gray.copy(alpha = 0.2f))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = showShareDialogText!!,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black
                        )
                    }
                    
                    Text(L.t("Choose Share Destination") + ":", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Downloaded simulated PDF to local store successfully!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Download PDF", fontSize = 10.sp)
                        }
                        Button(
                            onClick = {
                                Toast.makeText(context, "Receipt copied. WhatsApp clipboard prefilled!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("WhatsApp", fontSize = 10.sp)
                        }
                        Button(
                            onClick = {
                                Toast.makeText(context, "Email voucher dispatch queued!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Email", fontSize = 10.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showShareDialogText = null }) {
                    Text("Close Preview")
                }
            }
        )
    }

    // 6. DETAILED HISTORICAL LEDGER / KHATA DIALOG
    if (activeLedgerProfile != null) {
        val client = activeLedgerProfile!!
        val isFarmer = client.customerType == "Farmer"
        
        val fCollections = collections.filter { it.customerId == client.id }
        val fPayments = payments.filter { it.customerId == client.id }
        val fInvoices = invoices.filter { it.customerId == client.id }
        
        AlertDialog(
            onDismissRequest = { activeLedgerProfile = null },
            title = { Text("${client.name} - Ledger Khata Statement", fontWeight = FontWeight.Bold, color = Color(0xFF00A38B)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF1E2E2F) else Color(0xFFF0FDFB))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Code: ${client.code} | Type: ${client.customerType}", fontSize = 11.sp, color = Color.Gray)
                            Text("Mobile: ${client.phone} | Address: ${client.address}", fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Outstanding Ledger Balance: ₹${client.balance}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (client.balance >= 0) Color.Red else Color(0xFF00A38B))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Historical Transactions Ledger:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (isFarmer) {
                            fCollections.reversed().forEach { coll ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Milk Dep: ${coll.quantity}L (${coll.shift})", fontSize = 11.sp)
                                    Text("- ₹${coll.amount}", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            fPayments.reversed().forEach { pay ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Paid: ${pay.paymentMethod}", fontSize = 11.sp)
                                    Text("+ ₹${pay.amountReceived}", fontSize = 11.sp, color = Color(0xFF00A38B), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        } else {
                            fInvoices.reversed().forEach { inv ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Invoice: ${inv.invoiceNumber}", fontSize = 11.sp)
                                    Text("+ ₹${inv.totalAmount}", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            fPayments.reversed().forEach { pay ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Received: ${pay.paymentMethod}", fontSize = 11.sp)
                                    Text("- ₹${pay.amountReceived}", fontSize = 11.sp, color = Color(0xFF00A38B), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val summaryText = "Ledger of ${client.name}\nBalance: ₹${client.balance}\n" +
                            if (isFarmer) "Collections: ${fCollections.size} | Payouts: ${fPayments.size}"
                            else "Sales: ${fInvoices.size} | Payments Recv: ${fPayments.size}"
                            showShareDialogText = PrinterHelper.generateReceiptText("Customer Ledger", dpVal.dairyName, "DH-800", mapOf("Client" to client.name, "Summary" to summaryText), printerSize)
                        },
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("Print Ledger Statement")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { activeLedgerProfile = null }) {
                    Text("Close Statement")
                }
            }
        )
    }
}

// ==============================================================
// CUSTOM INTERNAL COMPOSABLE UTILITIES & HELPERS
// ==============================================================

@Composable
fun SmallMetric(
    title: String,
    value: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isDarkTheme: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF1E2E2F).copy(alpha = 0.3f) else Color.White
        ),
        border = BorderStroke(1.dp, if (isDarkTheme) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(L.t(title), fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(L.t(sub), fontSize = 9.sp, color = color, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun AccordionWrapper(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    isDarkTheme: Boolean,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF1E2E2F).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.8f)
        ),
        border = BorderStroke(1.dp, if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            if (isExpanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                content()
            }
        }
    }
}

@Composable
fun ReceiptRow(
    title: String,
    meta: String,
    amount: String,
    onPrint: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(meta, fontSize = 10.sp, color = Color.Gray)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(amount, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF00A38B))
            IconButton(onClick = onPrint, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Print, contentDescription = "Print", tint = Color(0xFF00A38B), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun CollectionsLineChart(collections: List<MilkCollectionEntity>, isDarkTheme: Boolean) {
    val recentDays = collections.sortedBy { it.timestamp }.takeLast(7)
    
    if (recentDays.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(L.t("No collection records available to plot"), fontSize = 11.sp, color = Color.Gray)
        }
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(L.t("Milk Collections Trend (Last 7 Records)"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00A38B))
        Spacer(modifier = Modifier.height(8.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            val width = size.width
            val height = size.height
            val maxQty = (recentDays.maxOfOrNull { it.quantity } ?: 10.0).coerceAtLeast(5.0)
            val stepX = width / (recentDays.size - 1).coerceAtLeast(1)
            
            // Draw grid lines
            val gridColor = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f)
            for (i in 0..4) {
                val y = height * i / 4
                drawLine(
                    color = gridColor,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(width, y),
                    strokeWidth = 1f
                )
            }

            // Draw line curve
            val points = recentDays.mapIndexed { index, item ->
                val x = index * stepX
                val y = height - (item.quantity.toFloat() / maxQty.toFloat() * height * 0.8f)
                androidx.compose.ui.geometry.Offset(x, y.toFloat())
            }

            // Draw area gradient under the line
            val path = androidx.compose.ui.graphics.Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points[0].x, height)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, height)
                    close()
                }
            }
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00A38B).copy(alpha = 0.25f), Color.Transparent)
                )
            )

            // Draw actual line
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = Color(0xFF00A38B),
                    start = points[i],
                    end = points[i+1],
                    strokeWidth = 3f
                )
            }

            // Draw markers
            points.forEachIndexed { index, point ->
                drawCircle(
                    color = Color(0xFF00A38B),
                    radius = 5f,
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5f,
                    center = point
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            recentDays.forEachIndexed { index, item ->
                val calendar = Calendar.getInstance().apply { timeInMillis = item.timestamp }
                val dateStr = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}"
                Text(
                    text = "$dateStr\n${item.quantity}L",
                    fontSize = 8.sp,
                    lineHeight = 10.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtext,
                fontSize = 10.sp,
                color = color,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// ======================================================
// MODULE 3: FARMERS / CUSTOMERS MANAGEMENT
// ======================================================
@Composable
fun CustomersScreen(viewModel: DairyViewModel) {
    val context = LocalContext.current
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val collections by viewModel.allCollections.collectAsStateWithLifecycle()
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val payments by viewModel.allPayments.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<CustomerEntity?>(null) }
    var selectedCustomerForProfile by remember { mutableStateOf<CustomerEntity?>(null) }

    // Form states (Add)
    var newCode by remember { mutableStateOf("") }
    var newFirstName by remember { mutableStateOf("") }
    var newLastName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newMilkType by remember { mutableStateOf("Both") } // Cow, Buffalo, Both
    var newBankName by remember { mutableStateOf("") }
    var newBranch by remember { mutableStateOf("") }
    var newAccountNumber by remember { mutableStateOf("") }
    var newIfsc by remember { mutableStateOf("") }
    var newAddress by remember { mutableStateOf("") }
    var newOpeningBalance by remember { mutableStateOf("0.0") }
    var newCustomerType by remember { mutableStateOf("Farmer") }
    var newPaymentMode by remember { mutableStateOf("Cash") }
    var newIsActive by remember { mutableStateOf(true) }

    // Form states (Edit)
    var editCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var editCode by remember { mutableStateOf("") }
    var editFirstName by remember { mutableStateOf("") }
    var editLastName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editMilkType by remember { mutableStateOf("Both") }
    var editBankName by remember { mutableStateOf("") }
    var editBranch by remember { mutableStateOf("") }
    var editAccountNumber by remember { mutableStateOf("") }
    var editIfsc by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }
    var editOpeningBalance by remember { mutableStateOf("0.0") }
    var editCustomerType by remember { mutableStateOf("Farmer") }
    var editPaymentMode by remember { mutableStateOf("Cash") }
    var editIsActive by remember { mutableStateOf(true) }

    val filteredCustomers = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || 
        it.phone.contains(searchQuery) || 
        it.code.contains(searchQuery, ignoreCase = true)
    }

    if (selectedCustomerForProfile != null) {
        val currentProfileId = selectedCustomerForProfile!!.id
        val customer = customers.find { it.id == currentProfileId } ?: selectedCustomerForProfile!!
        val customerCollections = collections.filter { it.customerId == customer.id }
        val customerInvoices = invoices.filter { it.customerId == customer.id }
        val customerPayments = payments.filter { it.customerId == customer.id }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Back, Edit, Delete
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { selectedCustomerForProfile = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Farmer Portfolio",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = {
                            editCustomer = customer
                            editCode = customer.code
                            val names = customer.name.split(" ", limit = 2)
                            editFirstName = names.getOrNull(0) ?: ""
                            editLastName = names.getOrNull(1) ?: ""
                            editPhone = customer.phone
                            editEmail = customer.email
                            editMilkType = customer.milkType
                            editBankName = customer.bankName
                            editBranch = customer.branch
                            editAccountNumber = customer.accountNumber
                            editIfsc = customer.ifsc
                            editAddress = customer.address
                            editOpeningBalance = customer.balance.toString()
                            editCustomerType = customer.customerType
                            editPaymentMode = customer.paymentMode
                            editIsActive = customer.isActive
                            showEditDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(onClick = {
                            showDeleteConfirmDialog = customer
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Profile", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Overview details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = customer.name,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 22.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = if (customer.isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                        border = BorderStroke(1.dp, if (customer.isActive) Color(0xFF81C784) else Color(0xFFE57373))
                                    ) {
                                        Text(
                                            text = if (customer.isActive) "Active" else "Inactive",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (customer.isActive) Color(0xFF2E7D32) else Color(0xFFC62828),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Code: ${customer.code.ifEmpty { "N/A" }} | Milk Type: ${customer.milkType}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Switch(
                                checked = customer.isActive,
                                onCheckedChange = { isChecked ->
                                    val updated = customer.copy(isActive = isChecked)
                                    viewModel.updateCustomer(updated)
                                    Toast.makeText(context, "Status updated to ${if (isChecked) "Active" else "Inactive"}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("📞 Phone", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text(customer.phone, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("✉️ Email", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text(customer.email.ifEmpty { "Not set" }, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("💰 Ledger Balance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text(
                                    text = "₹${String.format("%.2f", customer.balance)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    color = if (customer.balance >= 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                                )
                                Text(
                                    text = if (customer.balance >= 0) "Collect from Buyer" else "Owed to Farmer (Credit)",
                                    fontSize = 10.sp,
                                    color = if (customer.balance >= 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("📍 Address", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(customer.address.ifEmpty { "No address registered." }, fontSize = 13.sp)
                    }
                }
            }

            // Bank settlements card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🏦 Settled Bank Account Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1.1f)) {
                                Text("Bank Name", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text(customer.bankName.ifEmpty { "Not registered" }, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                                Spacer(modifier = Modifier.height(8.dp))

                                Text("Account Number", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text(customer.accountNumber.ifEmpty { "Not registered" }, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Column(modifier = Modifier.weight(0.9f), horizontalAlignment = Alignment.End) {
                                Text("Branch", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text(customer.branch.ifEmpty { "N/A" }, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                                Spacer(modifier = Modifier.height(8.dp))

                                Text("IFSC Code", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text(customer.ifsc.ifEmpty { "N/A" }, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Recent supplies
            item {
                Text("🥛 Recent Milk Supply Log", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            if (customerCollections.isEmpty()) {
                item { Text("No supplies registered yet.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) }
            } else {
                items(customerCollections.take(5)) { col ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Shift: ${col.shift} (${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(col.timestamp))})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("Volume: ${col.quantity}L | Type: ${col.milkType} | Fat: ${col.fat}% | SNF: ${col.snf}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Text("+₹${col.amount}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }
                }
            }

            // Recent invoices
            item {
                Text("🧾 Sales Receipts & Invoices", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            if (customerInvoices.isEmpty()) {
                item { Text("No retail invoices registered.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) }
            } else {
                items(customerInvoices) { inv ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Invoice: ${inv.invoiceNumber}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text(inv.paymentStatus, fontSize = 10.sp, color = if (inv.paymentStatus == "Paid") Color(0xFF2E7D32) else MaterialTheme.colorScheme.error)
                            }
                            Text("-₹${inv.totalAmount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    } else {
        // Customer directory listing screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("👥 Farmers Directory", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_customer_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Account")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, mobile, or code...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_search_field"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredCustomers.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No farmer account fits search parameters.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers) { cust ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCustomerForProfile = cust }
                                .testTag("customer_item_${cust.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (cust.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (cust.isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cust.name.take(1).uppercase(),
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (cust.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            if (!cust.isActive) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("(Inactive)", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Text(
                                            text = "Code: ${cust.code.ifEmpty { "N/A" }} | Phone: ${cust.phone}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Ledger Balance", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    Text(
                                        text = "₹${String.format("%.2f", cust.balance)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (cust.balance >= 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // dialog for add
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(L.t("Add Customer"), fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.height(350.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newCode,
                            onValueChange = { newCode = it },
                            label = { Text(L.t("Code")) },
                            modifier = Modifier.fillMaxWidth().testTag("new_customer_code")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newFirstName,
                                onValueChange = { newFirstName = it },
                                label = { Text(L.t("First Name")) },
                                modifier = Modifier.weight(1f).testTag("new_customer_fname")
                            )
                            OutlinedTextField(
                                value = newLastName,
                                onValueChange = { newLastName = it },
                                label = { Text(L.t("Last Name")) },
                                modifier = Modifier.weight(1f).testTag("new_customer_lname")
                            )
                        }
                        OutlinedTextField(
                            value = newPhone,
                            onValueChange = { newPhone = it },
                            label = { Text(L.t("Mobile")) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth().testTag("new_customer_phone")
                        )
                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = { newEmail = it },
                            label = { Text("Email Address") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Customer Type selection
                        Text(L.t("Customer Type") + ":", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("Farmer", "Retailer", "Wholesaler", "Staff").forEach { ct ->
                                val selected = newCustomerType == ct
                                Button(
                                    onClick = { newCustomerType = ct },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(L.t(ct), fontSize = 10.sp)
                                }
                            }
                        }

                        // Payment Mode selection
                        Text(L.t("Payment Mode") + ":", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("Cash", "UPI", "Credit", "Bank Transfer").forEach { pm ->
                                val selected = newPaymentMode == pm
                                Button(
                                    onClick = { newPaymentMode = pm },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(L.t(pm), fontSize = 10.sp)
                                }
                            }
                        }

                        // Milk type selector
                        Text("Milk Type Supplied:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("Cow", "Buffalo", "Both").forEach { mt ->
                                val selected = newMilkType == mt
                                Button(
                                    onClick = { newMilkType = mt },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(mt, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("🏦 Bank Settlement Accounts", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        
                        OutlinedTextField(
                            value = newBankName,
                            onValueChange = { newBankName = it },
                            label = { Text("Bank Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newBranch,
                            onValueChange = { newBranch = it },
                            label = { Text("Branch") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newAccountNumber,
                            onValueChange = { newAccountNumber = it },
                            label = { Text("Account Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newIfsc,
                            onValueChange = { newIfsc = it },
                            label = { Text("IFSC Code") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newAddress,
                            onValueChange = { newAddress = it },
                            label = { Text(L.t("Address")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newOpeningBalance,
                            onValueChange = { newOpeningBalance = it },
                            label = { Text(L.t("Opening Balance")) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(checked = newIsActive, onCheckedChange = { newIsActive = it })
                            Text("Mark Account as Active")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fullName = "${newFirstName.trim()} ${newLastName.trim()}".trim()
                        if (fullName.isNotEmpty() && newPhone.isNotEmpty()) {
                            viewModel.addCustomer(
                                name = fullName,
                                phone = newPhone,
                                address = newAddress,
                                code = newCode,
                                email = newEmail,
                                milkType = newMilkType,
                                bankName = newBankName,
                                branch = newBranch,
                                accountNumber = newAccountNumber,
                                ifsc = newIfsc,
                                openingBalance = newOpeningBalance.toDoubleOrNull() ?: 0.0,
                                customerType = newCustomerType,
                                paymentMode = newPaymentMode,
                                isActive = newIsActive
                            )
                            newCode = ""
                            newFirstName = ""
                            newLastName = ""
                            newPhone = ""
                            newEmail = ""
                            newMilkType = "Both"
                            newBankName = ""
                            newBranch = ""
                            newAccountNumber = ""
                            newIfsc = ""
                            newAddress = ""
                            newOpeningBalance = "0.0"
                            newCustomerType = "Farmer"
                            newPaymentMode = "Cash"
                            newIsActive = true
                            showAddDialog = false
                            Toast.makeText(context, "Customer profile successfully registered", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Full Name and Phone are required fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("save_customer_button")
                ) {
                    Text(L.t("Save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text(L.t("Cancel")) }
            }
        )
    }

    // dialog for edit
    if (showEditDialog && editCustomer != null) {
        val originalCust = editCustomer!!
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(L.t("Edit Customer"), fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.height(350.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = editCode,
                            onValueChange = { editCode = it },
                            label = { Text(L.t("Code")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editFirstName,
                                onValueChange = { editFirstName = it },
                                label = { Text(L.t("First Name")) },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editLastName,
                                onValueChange = { editLastName = it },
                                label = { Text(L.t("Last Name")) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text(L.t("Mobile")) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text("Email Address") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Customer Type selection
                        Text(L.t("Customer Type") + ":", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("Farmer", "Retailer", "Wholesaler", "Staff").forEach { ct ->
                                val selected = editCustomerType == ct
                                Button(
                                    onClick = { editCustomerType = ct },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(L.t(ct), fontSize = 10.sp)
                                }
                            }
                        }

                        // Payment Mode selection
                        Text(L.t("Payment Mode") + ":", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("Cash", "UPI", "Credit", "Bank Transfer").forEach { pm ->
                                val selected = editPaymentMode == pm
                                Button(
                                    onClick = { editPaymentMode = pm },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(L.t(pm), fontSize = 10.sp)
                                }
                            }
                        }

                        // Milk type selector
                        Text("Milk Type Supplied:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("Cow", "Buffalo", "Both").forEach { mt ->
                                val selected = editMilkType == mt
                                Button(
                                    onClick = { editMilkType = mt },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(mt, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("🏦 Bank Settlement Accounts", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        OutlinedTextField(
                            value = editBankName,
                            onValueChange = { editBankName = it },
                            label = { Text("Bank Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editBranch,
                            onValueChange = { editBranch = it },
                            label = { Text("Branch") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editAccountNumber,
                            onValueChange = { editAccountNumber = it },
                            label = { Text("Account Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editIfsc,
                            onValueChange = { editIfsc = it },
                            label = { Text("IFSC Code") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editAddress,
                            onValueChange = { editAddress = it },
                            label = { Text(L.t("Address")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editOpeningBalance,
                            onValueChange = { editOpeningBalance = it },
                            label = { Text(L.t("Opening Balance")) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(checked = editIsActive, onCheckedChange = { editIsActive = it })
                            Text("Mark Account as Active")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fullName = "${editFirstName.trim()} ${editLastName.trim()}".trim()
                        if (fullName.isNotEmpty() && editPhone.isNotEmpty()) {
                            val updatedCust = originalCust.copy(
                                name = fullName,
                                phone = editPhone,
                                address = editAddress,
                                code = editCode,
                                email = editEmail,
                                milkType = editMilkType,
                                bankName = editBankName,
                                branch = editBranch,
                                accountNumber = editAccountNumber,
                                ifsc = editIfsc,
                                balance = editOpeningBalance.toDoubleOrNull() ?: originalCust.balance,
                                customerType = editCustomerType,
                                paymentMode = editPaymentMode,
                                isActive = editIsActive
                            )
                            viewModel.updateCustomer(updatedCust)
                            showEditDialog = false
                            Toast.makeText(context, "Customer details successfully updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Full Name and Phone are required", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(L.t("Save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text(L.t("Cancel")) }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog != null) {
        val targetCust = showDeleteConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete Farmer Profile?") },
            text = { Text("Are you absolutely sure you want to permanently delete the profile for ${targetCust.name}? This will purge their ledger records completely.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        viewModel.deleteCustomer(targetCust)
                        showDeleteConfirmDialog = null
                        selectedCustomerForProfile = null
                        Toast.makeText(context, "Profile successfully deleted", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) { Text("Cancel") }
            }
        )
    }
}

// ======================================================
// MODULE 4: MILK COLLECTION DESK
// ======================================================
@Composable
fun CollectionScreen(viewModel: DairyViewModel) {
    val context = LocalContext.current
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val collections by viewModel.allCollections.collectAsStateWithLifecycle()

    var selectedShift by remember { mutableStateOf("Morning") }
    var selectedMilkType by remember { mutableStateOf("Cow") } // Cow, Buffalo
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var fatText by remember { mutableStateOf("") }
    var snfText by remember { mutableStateOf("") }
    var clrText by remember { mutableStateOf("") }
    var qtyText by remember { mutableStateOf("") }

    var expandedDropdown by remember { mutableStateOf(false) }
    var showQuickAddDialog by remember { mutableStateOf(false) }
    var newPhoneToSelect by remember { mutableStateOf<String?>(null) }

    // Listen to changes in customers and auto-select newly added one
    LaunchedEffect(customers) {
        newPhoneToSelect?.let { phone ->
            val newlyAdded = customers.find { it.phone == phone }
            if (newlyAdded != null) {
                selectedCustomer = newlyAdded
                if (newlyAdded.milkType == "Cow" || newlyAdded.milkType == "Buffalo") {
                    selectedMilkType = newlyAdded.milkType
                }
                newPhoneToSelect = null
            }
        }
    }

    // Duplicate entry protection states
    var showDuplicateWarning by remember { mutableStateOf(false) }

    // Edit collection states
    var showEditDialog by remember { mutableStateOf(false) }
    var editingCollection by remember { mutableStateOf<MilkCollectionEntity?>(null) }
    var editFatText by remember { mutableStateOf("") }
    var editSnfText by remember { mutableStateOf("") }
    var editClrText by remember { mutableStateOf("") }
    var editQtyText by remember { mutableStateOf("") }
    var editMilkType by remember { mutableStateOf("Cow") }
    var showCollectionReceipt by remember { mutableStateOf<MilkCollectionEntity?>(null) }

    val fat = fatText.toDoubleOrNull() ?: 0.0
    val snf = snfText.toDoubleOrNull() ?: 0.0
    val clr = clrText.toDoubleOrNull() ?: 0.0
    val qty = qtyText.toDoubleOrNull() ?: 0.0

    val calculatedRate = viewModel.calculateRate(fat, snf)
    val calculatedAmount = String.format("%.2f", qty * calculatedRate).toDouble()

    // Filter active customers for supply dropdown
    val activeCustomers = customers.filter { it.isActive }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("collection_root"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("🥛 Rapid Milk Collection Desk", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Text("Weigh, evaluate FAT/SNF, and calculate payouts in one screen.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Shift Selection Segment
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { selectedShift = "Morning" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedShift == "Morning") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selectedShift == "Morning") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("☀️ Morning")
                        }
                        Button(
                            onClick = { selectedShift = "Evening" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedShift == "Evening") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selectedShift == "Evening") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("🌙 Evening")
                        }
                    }

                    // Cow / Buffalo Milk Type Segment Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { selectedMilkType = "Cow" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedMilkType == "Cow") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selectedMilkType == "Cow") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("🐄 Cow Milk")
                        }
                        Button(
                            onClick = { selectedMilkType = "Buffalo" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedMilkType == "Buffalo") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selectedMilkType == "Buffalo") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("🦬 Buffalo Milk")
                        }
                    }

                    // Customer Selector Box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ExposedDropdownMenuBox(
                                expanded = expandedDropdown,
                                onExpandedChange = { expandedDropdown = !expandedDropdown },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedCustomer?.name ?: "Tap to select supplying farmer...",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Farmer / Supplier") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                        .testTag("supplier_dropdown")
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedDropdown,
                                    onDismissRequest = { expandedDropdown = false }
                                ) {
                                    activeCustomers.forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text("${c.name} (${c.code}) - Bal: ₹${String.format("%.2f", c.balance)}") },
                                            onClick = {
                                                selectedCustomer = c
                                                expandedDropdown = false
                                                // Auto-select milk type of customer if registered
                                                if (c.milkType == "Cow" || c.milkType == "Buffalo") {
                                                    selectedMilkType = c.milkType
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = { showQuickAddDialog = true },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .testTag("quick_add_customer_collection_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Quick Add Farmer",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Raw Metric Fields (Grid of inputs)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = fatText,
                            onValueChange = { fatText = it },
                            label = { Text("FAT %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("fat_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = snfText,
                            onValueChange = { snfText = it },
                            label = { Text("SNF %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("snf_input"),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Quantity (Litres)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("qty_input"),
                        singleLine = true
                    )

                    // Auto Payout Metrics Display
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Calculated Rate / L", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text("₹${String.format("%.2f", calculatedRate)}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Farmer Payout Credit", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text("₹${String.format("%.2f", calculatedAmount)}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // Save Button
                    Button(
                        onClick = {
                            val cust = selectedCustomer
                            if (cust == null) {
                                Toast.makeText(context, "Please select a supplying farmer first!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (qty <= 0.0 || fat <= 0.0 || snf <= 0.0) {
                                Toast.makeText(context, "FAT %, SNF %, and Quantity must be greater than zero!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Duplicate Entry Protection check
                            val todayStart = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis

                            val isDuplicate = collections.any {
                                it.customerId == cust.id &&
                                it.shift == selectedShift &&
                                it.timestamp >= todayStart
                            }

                            if (isDuplicate) {
                                showDuplicateWarning = true
                            } else {
                                viewModel.addMilkCollection(
                                    customerId = cust.id,
                                    shift = selectedShift,
                                    fat = fat,
                                    snf = snf,
                                    clr = clr,
                                    quantity = qty,
                                    milkType = selectedMilkType
                                )
                                Toast.makeText(context, "Collected ${qty}L ${selectedMilkType} milk successfully!", Toast.LENGTH_SHORT).show()
                                // Clear fields
                                fatText = ""
                                snfText = ""
                                clrText = ""
                                qtyText = ""
                                selectedCustomer = null
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_collection_button")
                    ) {
                        Text("Register Entry & Update Ledger", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("📋 Shift Collection History", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (collections.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No milk collections logged today.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }
        } else {
            items(collections) { col ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Column: Milk Icon and Farmer Details
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (col.milkType == "Buffalo") "🦬" else "🐄",
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                val custName = customers.find { it.id == col.customerId }?.name ?: "Farmer #${col.customerId}"
                                Text(
                                    text = custName, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = L.t(col.shift), 
                                            fontSize = 9.sp, 
                                            fontWeight = FontWeight.Bold, 
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Text(
                                        text = "${col.quantity}L | F: ${col.fat}% | S: ${col.snf}%",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Right Column: Stacked Amount and Edit/Delete Actions (Compact & Crowding-proof!)
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "₹${String.format("%.2f", col.amount)}",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color(0xFF2E7D32),
                                maxLines = 1
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Receipt & Slip Button
                                IconButton(
                                    onClick = {
                                        showCollectionReceipt = col
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Receipt, 
                                        contentDescription = "Collection Receipt", 
                                        tint = MaterialTheme.colorScheme.primary, 
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Edit Button
                                IconButton(
                                    onClick = {
                                        editingCollection = col
                                        editFatText = col.fat.toString()
                                        editSnfText = col.snf.toString()
                                        editClrText = col.clr.toString()
                                        editQtyText = col.quantity.toString()
                                        editMilkType = col.milkType
                                        showEditDialog = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit, 
                                        contentDescription = "Edit Entry", 
                                        tint = MaterialTheme.colorScheme.primary, 
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Delete Button
                                IconButton(
                                    onClick = { 
                                        viewModel.deleteCollection(col)
                                        Toast.makeText(context, "Collection entry deleted, ledger reverted", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete, 
                                        contentDescription = "Delete", 
                                        tint = MaterialTheme.colorScheme.error, 
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Duplicate entry confirmation dialog
    if (showDuplicateWarning && selectedCustomer != null) {
        val cust = selectedCustomer!!
        AlertDialog(
            onDismissRequest = { showDuplicateWarning = false },
            title = { Text("⚠️ Duplicate Entry Warning") },
            text = { Text("An entry for ${cust.name} during the ${selectedShift} shift has already been logged today. Are you sure you want to register another entry anyway?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addMilkCollection(
                            customerId = cust.id,
                            shift = selectedShift,
                            fat = fat,
                            snf = snf,
                            clr = clr,
                            quantity = qty,
                            milkType = selectedMilkType
                        )
                        Toast.makeText(context, "Collected ${qty}L ${selectedMilkType} milk successfully (forced)!", Toast.LENGTH_SHORT).show()
                        // Clear fields
                        fatText = ""
                        snfText = ""
                        clrText = ""
                        qtyText = ""
                        selectedCustomer = null
                        showDuplicateWarning = false
                    }
                ) {
                    Text("Register Anyway")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDuplicateWarning = false }) { Text("Cancel") }
            }
        )
    }

    // Edit entry Dialog
    if (showEditDialog && editingCollection != null) {
        val col = editingCollection!!
        val custName = customers.find { it.id == col.customerId }?.name ?: "Farmer #${col.customerId}"
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Milk Entry: $custName", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { editMilkType = "Cow" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (editMilkType == "Cow") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (editMilkType == "Cow") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("🐄 Cow", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { editMilkType = "Buffalo" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (editMilkType == "Buffalo") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (editMilkType == "Buffalo") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("🦬 Buffalo", fontSize = 11.sp)
                        }
                    }

                    OutlinedTextField(
                        value = editFatText,
                        onValueChange = { editFatText = it },
                        label = { Text("FAT %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editSnfText,
                        onValueChange = { editSnfText = it },
                        label = { Text("SNF %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editQtyText,
                        onValueChange = { editQtyText = it },
                        label = { Text("Quantity (Litres)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newFat = editFatText.toDoubleOrNull() ?: col.fat
                        val newSnf = editSnfText.toDoubleOrNull() ?: col.snf
                        val newClr = editClrText.toDoubleOrNull() ?: col.clr
                        val newQty = editQtyText.toDoubleOrNull() ?: col.quantity

                        if (newQty <= 0.0 || newFat <= 0.0 || newSnf <= 0.0) {
                            Toast.makeText(context, "Values must be greater than zero!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val newRate = viewModel.calculateRate(newFat, newSnf)
                        val newAmt = String.format("%.2f", newQty * newRate).toDouble()

                        val updated = col.copy(
                            fat = newFat,
                            snf = newSnf,
                            clr = newClr,
                            quantity = newQty,
                            rate = newRate,
                            amount = newAmt,
                            milkType = editMilkType
                        )
                        viewModel.updateCollection(updated)
                        showEditDialog = false
                        Toast.makeText(context, "Collection entry updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save Updates")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showQuickAddDialog) {
        QuickAddCustomerDialog(
            viewModel = viewModel,
            onDismiss = { showQuickAddDialog = false },
            onSuccess = { phone ->
                newPhoneToSelect = phone
                showQuickAddDialog = false
            }
        )
    }

    // 9. Collection Receipt & 19. Farmer Payment Slip Dialog
    if (showCollectionReceipt != null) {
        val col = showCollectionReceipt!!
        val dp by viewModel.dairyProfile.collectAsStateWithLifecycle()
        val dpVal = dp ?: com.example.data.model.DairyProfileEntity()
        val farmerName = customers.find { it.id == col.customerId }?.name ?: "Farmer #${col.customerId}"
        val farmerPhone = customers.find { it.id == col.customerId }?.phone ?: "N/A"
        
        AlertDialog(
            onDismissRequest = { showCollectionReceipt = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = Color(0xFF00A38B))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Collection Receipt & Slip", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFDFD))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Header Company Info
                            Text(
                                text = dpVal.dairyName.uppercase(Locale.ROOT),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Proprietor: ${dpVal.ownerName}",
                                fontSize = 10.sp,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "📞 ${dpVal.contactNumber} | ✉️ ${dpVal.emailAddress}",
                                fontSize = 9.sp,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Address: ${dpVal.address}",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (dpVal.gstNumber.isNotEmpty()) {
                                Text(
                                    text = "GSTIN: ${dpVal.gstNumber} | PAN: ${dpVal.panNumber}",
                                    fontSize = 9.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            
                            Text(
                                text = "---------------------------------------------",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                maxLines = 1
                            )
                            
                            // Title
                            Text(
                                text = "FARMER MILK PAYMENT SLIP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Text(
                                text = "---------------------------------------------",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                maxLines = 1
                            )
                            
                            // Details
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Farmer Name:", fontSize = 10.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                                Text(farmerName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = FontFamily.Monospace)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Contact phone:", fontSize = 10.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                                Text(farmerPhone, fontSize = 10.sp, color = Color.Black, fontFamily = FontFamily.Monospace)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Receipt ID:", fontSize = 10.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                                Text("#col-${col.id}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.Black, fontFamily = FontFamily.Monospace)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Date & Time:", fontSize = 10.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                                val currentSdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                                Text(currentSdf.format(Date(col.timestamp)), fontSize = 10.sp, color = Color.Black, fontFamily = FontFamily.Monospace)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Milk shift:", fontSize = 10.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                                Text(col.shift, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = FontFamily.Monospace)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Milk breed:", fontSize = 10.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                                Text(col.milkType, fontSize = 10.sp, color = Color.Black, fontFamily = FontFamily.Monospace)
                            }
                            
                            Text(
                                text = "- - - - - - - - - - - - - - - - - - - - - - -",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                maxLines = 1
                            )
                            
                            // Metrics
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Milk volume:", fontSize = 10.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                                Text("${col.quantity} Litres", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = FontFamily.Monospace)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("FAT content:", fontSize = 10.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                                Text("${col.fat} %", fontSize = 10.sp, color = Color.Black, fontFamily = FontFamily.Monospace)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SNF level:", fontSize = 10.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                                Text("${col.snf} %", fontSize = 10.sp, color = Color.Black, fontFamily = FontFamily.Monospace)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("CLR level:", fontSize = 10.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                                Text("${col.clr} %", fontSize = 10.sp, color = Color.Black, fontFamily = FontFamily.Monospace)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Rate / Litre:", fontSize = 10.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                                Text("₹${String.format("%.2f", col.rate)}", fontSize = 10.sp, color = Color.Black, fontFamily = FontFamily.Monospace)
                            }
                            
                            Text(
                                text = "---------------------------------------------",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                maxLines = 1
                            )
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("NET PAYABLE:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = FontFamily.Monospace)
                                Text("₹${String.format("%.2f", col.amount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontFamily = FontFamily.Monospace)
                            }
                            
                            Text(
                                text = "---------------------------------------------",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                maxLines = 1
                            )
                            
                            // Signature & Logo
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                if (dpVal.logo.isNotEmpty()) {
                                    Text(text = "Seal: ${dpVal.logo}", fontSize = 8.sp, color = Color.Gray)
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = dpVal.signature, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(text = "Auth Signature", fontSize = 8.sp, color = Color.DarkGray)
                                }
                            }
                            
                            Text(
                                text = "Thank You - ${dpVal.dairyName} App",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Printed Farmer Payment Slip to Bluetooth ESC/POS printer!", Toast.LENGTH_SHORT).show()
                        showCollectionReceipt = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A38B))
                ) {
                    Text("Print Slip", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCollectionReceipt = null }) {
                    Text("Close", color = Color.Gray)
                }
            }
        )
    }
}

// ======================================================
// MODULE 5: PRODUCT & INVENTORY MANAGEMENT
// ======================================================
@Composable
fun ProductsScreen(viewModel: DairyViewModel) {
    val products by viewModel.allProducts.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }
    var newStock by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Dairy") }
    var newUnit by remember { mutableStateOf("Kg") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📦 Dairy Inventory Hub", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Product")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (products.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Catalog is empty.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(products) { prod ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Cat: ${prod.category} | Price: $${prod.price}/${prod.unit}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Spacer(modifier = Modifier.height(4.dp))
                                // Custom Stock Alert Bar
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                    ) {
                                        val fillRatio = (prod.stockQty / 500.0).coerceIn(0.0, 1.0).toFloat()
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(fillRatio)
                                                .background(if (prod.stockQty < 15.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("${prod.stockQty} ${prod.unit} available", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = if (prod.stockQty < 15.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                }
                            }

                            Row {
                                IconButton(onClick = { viewModel.restockProduct(prod.id, 50.0) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Quick Restock (+50)", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { viewModel.deleteProduct(prod) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create Catalog Entry") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newPrice, onValueChange = { newPrice = it }, label = { Text("Price per Unit ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newStock, onValueChange = { newStock = it }, label = { Text("Initial Stock Qty") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newCategory, onValueChange = { newCategory = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newUnit, onValueChange = { newUnit = it }, label = { Text("Unit (Kg / Litre / Packet)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceVal = newPrice.toDoubleOrNull() ?: 0.0
                        val stockVal = newStock.toDoubleOrNull() ?: 0.0
                        if (newName.isNotEmpty() && priceVal > 0) {
                            viewModel.addProduct(newName, newCategory, priceVal, stockVal, newUnit)
                            showAddDialog = false
                            newName = ""
                            newPrice = ""
                            newStock = ""
                        }
                    }
                ) {
                    Text("Add Entry")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ======================================================
// MODULE 6: POINT OF SALE (POS SALES & INVOICES)
// ======================================================
@Composable
fun SalesScreen(viewModel: DairyViewModel) {
    val context = LocalContext.current
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val draftItems by viewModel.draftItems.collectAsStateWithLifecycle()

    val walkInCustomer = remember {
        CustomerEntity(
            id = 0,
            name = "Walk-in Cash Customer",
            phone = "",
            address = "Counter Sale",
            code = "CASH01",
            email = "",
            milkType = "Both",
            bankName = "",
            branch = "",
            accountNumber = "",
            ifsc = "",
            isActive = true
        )
    }

    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    var activeTab by remember { mutableStateOf("Billing") } // "Billing" or "History"
    var showQuickAddDialog by remember { mutableStateOf(false) }
    var newPhoneToSelect by remember { mutableStateOf<String?>(null) }

    var expandedInvoiceId by remember { mutableStateOf<Long?>(null) }
    var expandedInvoiceItems by remember { mutableStateOf<List<InvoiceItemEntity>>(emptyList()) }
    var loadingItemsForInvoiceId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(expandedInvoiceId) {
        val invId = expandedInvoiceId
        if (invId != null) {
            loadingItemsForInvoiceId = invId
            expandedInvoiceItems = viewModel.getInvoiceItems(invId)
            loadingItemsForInvoiceId = null
        } else {
            expandedInvoiceItems = emptyList()
        }
    }

    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(walkInCustomer) }

    // Snapshot of last checkout details for high-fidelity confirmation rendering
    var lastCheckoutCust by remember { mutableStateOf<CustomerEntity?>(null) }
    var lastCheckoutItems by remember { mutableStateOf<List<com.example.data.model.InvoiceItemEntity>>(emptyList()) }
    var lastCheckoutSubtotal by remember { mutableStateOf(0.0) }
    var lastCheckoutDiscountAmt by remember { mutableStateOf(0.0) }
    var lastCheckoutGstAmt by remember { mutableStateOf(0.0) }
    var lastCheckoutTotal by remember { mutableStateOf(0.0) }
    var lastCheckoutPaid by remember { mutableStateOf(true) }

    // Listen to changes in customers and auto-select newly added one
    LaunchedEffect(customers) {
        newPhoneToSelect?.let { phone ->
            val newlyAdded = customers.find { it.phone == phone }
            if (newlyAdded != null) {
                selectedCustomer = newlyAdded
                newPhoneToSelect = null
            }
        }
    }
    var selectedProductForDraft by remember { mutableStateOf<ProductEntity?>(null) }
    var qtyText by remember { mutableStateOf("1") }
    var discountText by remember { mutableStateOf("0") }
    var gstText by remember { mutableStateOf("5") }
    var isPaidStatus by remember { mutableStateOf(true) }

    var expandedCustDropdown by remember { mutableStateOf(false) }
    var expandedProdDropdown by remember { mutableStateOf(false) }

    var invoicePreviewHtml by remember { mutableStateOf<String?>(null) }

    val subtotal = draftItems.sumOf { it.amount }
    val discPct = discountText.toDoubleOrNull() ?: 0.0
    val gstPct = gstText.toDoubleOrNull() ?: 5.0
    val discountAmt = (subtotal * discPct) / 100.0
    val gstAmt = ((subtotal - discountAmt) * gstPct) / 100.0
    val grandTotal = String.format("%.2f", subtotal - discountAmt + gstAmt).toDouble()

    // Filter active customers for the selection
    val activeCustomers = customers.filter { it.isActive }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pos_root"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Tab switcher
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { activeTab = "Billing" },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == "Billing") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (activeTab == "Billing") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Sale POS")
                }
                Button(
                    onClick = { activeTab = "History" },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == "History") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (activeTab == "History") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sales History")
                }
            }
        }

        if (activeTab == "Billing") {
            item {
                Text("🧾 SaaS Interactive POS Terminal", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Text("Deduct inventory and update farmer accounts automatically on checkout.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Customer Dropdown selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ExposedDropdownMenuBox(
                                expanded = expandedCustDropdown,
                                onExpandedChange = { expandedCustDropdown = !expandedCustDropdown },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedCustomer?.name ?: "Select buying customer...",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Buyer Portfolio") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCustDropdown) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                        .testTag("buyer_dropdown")
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedCustDropdown,
                                    onDismissRequest = { expandedCustDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Walk-in Cash Customer (Standard Counter)") },
                                        onClick = {
                                            selectedCustomer = walkInCustomer
                                            expandedCustDropdown = false
                                        }
                                    )
                                    activeCustomers.forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text("${c.name} (${c.code}) - Bal: ₹${String.format("%.2f", c.balance)}") },
                                            onClick = {
                                                selectedCustomer = c
                                                expandedCustDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = { showQuickAddDialog = true },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .testTag("quick_add_customer_sales_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Quick Add Buyer",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Product Adder selection
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1.5f)) {
                            ExposedDropdownMenuBox(
                                expanded = expandedProdDropdown,
                                onExpandedChange = { expandedProdDropdown = !expandedProdDropdown }
                            ) {
                                OutlinedTextField(
                                    value = selectedProductForDraft?.name ?: "Choose product...",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Product Catalog") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProdDropdown) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedProdDropdown,
                                    onDismissRequest = { expandedProdDropdown = false }
                                ) {
                                    products.forEach { p ->
                                        DropdownMenuItem(
                                            text = { Text("${p.name} - ₹${p.price}/${p.unit} (Stock: ${p.stockQty})") },
                                            onClick = {
                                                selectedProductForDraft = p
                                                expandedProdDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = qtyText,
                            onValueChange = { qtyText = it },
                            label = { Text("Qty") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.5f),
                            singleLine = true
                        )

                        IconButton(
                            onClick = {
                                val prod = selectedProductForDraft
                                val quantity = qtyText.toDoubleOrNull() ?: 1.0
                                if (prod == null) {
                                    Toast.makeText(context, "Select a product to sell!", Toast.LENGTH_SHORT).show()
                                    return@IconButton
                                }
                                if (quantity <= 0) {
                                    Toast.makeText(context, "Quantity must be greater than zero!", Toast.LENGTH_SHORT).show()
                                    return@IconButton
                                }
                                if (quantity > prod.stockQty) {
                                    Toast.makeText(context, "Alert: Insufficient stock! Available: ${prod.stockQty} ${prod.unit}", Toast.LENGTH_SHORT).show()
                                }
                                viewModel.addProductToDraft(prod, quantity)
                                selectedProductForDraft = null
                                qtyText = "1"
                                Toast.makeText(context, "Added ${prod.name} to order basket", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .testTag("add_to_cart_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add to basket", tint = Color.White)
                        }
                    }
                }
            }
        }

        // Cart listing
        if (draftItems.isNotEmpty()) {
            item { Text("🛒 Draft Order Basket", fontWeight = FontWeight.Bold, fontSize = 14.sp) }

            items(draftItems) { di ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(di.productName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${di.quantity} x ₹${di.price}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("₹${di.amount}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(onClick = { 
                                viewModel.removeProductFromDraft(di)
                                Toast.makeText(context, "Removed item from draft", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Calculation Sheet
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = discountText,
                                onValueChange = { discountText = it },
                                label = { Text("Discount %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = gstText,
                                onValueChange = { gstText = it },
                                label = { Text("GST %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            CalculationRow(label = "Basket Subtotal", value = "₹${String.format("%.2f", subtotal)}")
                            CalculationRow(label = "Promotional Discount", value = "-₹${String.format("%.2f", discountAmt)}", color = MaterialTheme.colorScheme.primary)
                            CalculationRow(label = "Government GST Value", value = "+₹${String.format("%.2f", gstAmt)}")
                            CalculationRow(label = "Grand Total Invoice", value = "₹${String.format("%.2f", grandTotal)}", isBold = true)
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        // Payment Status Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mark Instant Payment Cash Cleared", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Switch(checked = isPaidStatus, onCheckedChange = { isPaidStatus = it })
                        }

                        // Checkout Submit Button
                        Button(
                            onClick = {
                                val cust = selectedCustomer
                                if (cust != null) {
                                    // Snapshot draft details before they are cleared inside checkoutInvoice
                                    lastCheckoutCust = cust
                                    lastCheckoutItems = draftItems.toList()
                                    lastCheckoutSubtotal = draftItems.sumOf { it.amount }
                                    lastCheckoutDiscountAmt = (lastCheckoutSubtotal * discPct) / 100.0
                                    lastCheckoutGstAmt = ((lastCheckoutSubtotal - lastCheckoutDiscountAmt) * gstPct) / 100.0
                                    lastCheckoutTotal = lastCheckoutSubtotal - lastCheckoutDiscountAmt + lastCheckoutGstAmt
                                    lastCheckoutPaid = isPaidStatus

                                    viewModel.checkoutInvoice(
                                        customerId = cust.id,
                                        customerName = cust.name,
                                        discountPercent = discPct,
                                        gstPercent = gstPct,
                                        isPaid = isPaidStatus
                                    )
                                    invoicePreviewHtml = "INV-${System.currentTimeMillis().toString().takeLast(6)}"
                                    selectedCustomer = walkInCustomer
                                    Toast.makeText(context, "Invoice created successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("checkout_button")
                        ) {
                            Text("Generate Invoice & Deduct Inventory", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Draft Order Basket is empty.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        Text("Choose products from the catalog above to make a sale.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    }
                }
            }
        }
    } else {
        // activeTab == "History"
        if (invoices.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No Sales History recorded yet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                        Text("Complete a POS sale to see it listed here.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    }
                }
            }
        } else {
            item {
                Text("📜 Past Sales Log (${invoices.size} Invoices)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            items(invoices) { inv ->
                val isExpanded = expandedInvoiceId == inv.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedInvoiceId = if (isExpanded) null else inv.id
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Inv: ${inv.invoiceNumber}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (inv.paymentStatus == "Paid") Color(0xFFE8F5E9) else Color(0xFFFFFDE7),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = inv.paymentStatus,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (inv.paymentStatus == "Paid") Color(0xFF2E7D32) else Color(0xFFF57F17)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = inv.customerName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(inv.timestamp)),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${String.format("%.2f", inv.totalAmount)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isExpanded) "Show Less" else "Show Details",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(8.dp))

                            if (loadingItemsForInvoiceId == inv.id) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                }
                            } else {
                                Text("Products Sold:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Spacer(modifier = Modifier.height(4.dp))
                                expandedInvoiceItems.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("${item.quantity} x ₹${String.format("%.2f", item.price)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                        }
                                        Text("₹${String.format("%.2f", item.amount)}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                Spacer(modifier = Modifier.height(8.dp))

                                // Invoice Calculations Summary
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Subtotal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                        Text("₹${String.format("%.2f", inv.subtotal)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }
                                    if (inv.discount > 0) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Discount", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                            Text("-₹${String.format("%.2f", inv.discount)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    if (inv.gst > 0) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("GST", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                            Text("+₹${String.format("%.2f", inv.gst)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                        }
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Grand Total", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("₹${String.format("%.2f", inv.totalAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { invoicePreviewHtml = inv.invoiceNumber },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("View & Print Digital Receipt")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

    if (showQuickAddDialog) {
        QuickAddCustomerDialog(
            viewModel = viewModel,
            onDismiss = { showQuickAddDialog = false },
            onSuccess = { phone ->
                newPhoneToSelect = phone
                showQuickAddDialog = false
            }
        )
    }

    // Invoice Receipt Visual PDF/Print Simulation Dialog
    if (invoicePreviewHtml != null) {
        val invNum = invoicePreviewHtml!!
        val dp by viewModel.dairyProfile.collectAsStateWithLifecycle()

        AlertDialog(
            onDismissRequest = { invoicePreviewHtml = null },
            title = { 
                Text(
                    text = L.t("Tax Invoice Generated"), 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                ) 
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "✅ " + L.t("Sales Invoice has been successfully committed to offline Room schema."),
                        fontSize = 11.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    // POS Tape Visual Design (Glassmorphic / White paper container)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFDFD)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Header Info
                            val dpVal = dp ?: com.example.data.model.DairyProfileEntity()
                            Text(
                                text = dpVal.dairyName.uppercase(Locale.ROOT),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Proprietor: ${dpVal.ownerName} | UPI ID: ${dpVal.upiId}",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "📞 ${dpVal.contactNumber} | ✉️ ${dpVal.emailAddress}",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Address: ${dpVal.address}",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (dpVal.gstNumber.isNotEmpty()) {
                                Text(
                                    text = "GSTIN: ${dpVal.gstNumber} | PAN: ${dpVal.panNumber}",
                                    fontSize = 9.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (dpVal.website.isNotEmpty()) {
                                Text(
                                    text = "Web: ${dpVal.website}",
                                    fontSize = 9.sp,
                                    color = Color.Blue,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Text(
                                text = "------------------------------------------------",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                maxLines = 1
                            )

                            // Meta details
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Invoice No:", fontSize = 11.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                                Text(invNum, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = FontFamily.Monospace)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Date / Time:", fontSize = 11.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                                val currentSdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                                Text(currentSdf.format(Date()), fontSize = 11.sp, color = Color.Black, fontFamily = FontFamily.Monospace)
                            }

                            lastCheckoutCust?.let { cust ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Customer:", fontSize = 11.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                                    Text(cust.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = FontFamily.Monospace)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Mobile:", fontSize = 11.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                                    Text(cust.phone, fontSize = 11.sp, color = Color.Black, fontFamily = FontFamily.Monospace)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Cust Type:", fontSize = 11.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                                    Text(L.t(cust.customerType), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Black, fontFamily = FontFamily.Monospace)
                                }
                            }

                            Text(
                                text = "------------------------------------------------",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                maxLines = 1
                            )

                            // Items Header
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Item", modifier = Modifier.weight(1.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = FontFamily.Monospace)
                                Text("Qty", modifier = Modifier.weight(0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.End, fontFamily = FontFamily.Monospace)
                                Text("Rate", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.End, fontFamily = FontFamily.Monospace)
                                Text("Amt", modifier = Modifier.weight(1.2f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.End, fontFamily = FontFamily.Monospace)
                            }

                            Text(
                                text = "- - - - - - - - - - - - - - - - - - - - - - - -",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                maxLines = 1
                            )

                            // Items List
                            lastCheckoutItems.forEach { item ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(item.productName, modifier = Modifier.weight(1.8f), fontSize = 11.sp, color = Color.Black, fontFamily = FontFamily.Monospace)
                                    Text("${item.quantity}", modifier = Modifier.weight(0.8f), fontSize = 11.sp, color = Color.Black, textAlign = TextAlign.End, fontFamily = FontFamily.Monospace)
                                    Text("₹${String.format("%.1f", item.price)}", modifier = Modifier.weight(1f), fontSize = 11.sp, color = Color.Black, textAlign = TextAlign.End, fontFamily = FontFamily.Monospace)
                                    Text("₹${String.format("%.1f", item.amount)}", modifier = Modifier.weight(1.2f), fontSize = 11.sp, color = Color.Black, textAlign = TextAlign.End, fontFamily = FontFamily.Monospace)
                                }
                            }

                            Text(
                                text = "------------------------------------------------",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                maxLines = 1
                            )

                            // Calculations Breakdown
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal:", fontSize = 11.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                                Text("₹${String.format("%.2f", lastCheckoutSubtotal)}", fontSize = 11.sp, color = Color.Black, fontFamily = FontFamily.Monospace)
                            }
                            if (lastCheckoutDiscountAmt > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Discount:", fontSize = 11.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                                    Text("-₹${String.format("%.2f", lastCheckoutDiscountAmt)}", fontSize = 11.sp, color = Color(0xFF2E7D32), fontFamily = FontFamily.Monospace)
                                }
                            }
                            if (lastCheckoutGstAmt > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("GST:", fontSize = 11.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
                                    Text("+₹${String.format("%.2f", lastCheckoutGstAmt)}", fontSize = 11.sp, color = Color.Black, fontFamily = FontFamily.Monospace)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("GRAND TOTAL:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = FontFamily.Monospace)
                                Text("₹${String.format("%.2f", lastCheckoutTotal)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = FontFamily.Monospace)
                            }

                            Text(
                                text = "------------------------------------------------",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                maxLines = 1
                            )

                            // Payment Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val statusText = if (lastCheckoutPaid) "PAID IN CASH" else "PENDING CREDIT DEBT"
                                val statusColor = if (lastCheckoutPaid) Color(0xFF2E7D32) else Color(0xFFC62828)
                                Box(
                                    modifier = Modifier
                                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = statusText,
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    // Draw a mock visual barcode graphic using Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(50.dp)
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .border(BorderStroke(1.dp, Color.LightGray)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 6.dp)) {
                            val w = size.width
                            val h = size.height
                            var currentX = 0f
                            val random = Random(invNum.hashCode().toLong())
                            while (currentX < w) {
                                val barWidth = random.nextFloat() * 8f + 2f
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(currentX, 0f),
                                    size = Size(barWidth, h)
                                )
                                currentX += barWidth + (random.nextFloat() * 6f + 2f)
                            }
                        }
                    }

                    // Scannable UPI QR Graphic Drawing
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(BorderStroke(2.dp, Color.Black)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                            val w = size.width
                            val h = size.height
                            val rows = 8
                            val cols = 8
                            val cellW = w / cols
                            val cellH = h / rows
                            val random = Random(invNum.hashCode().toLong())
                            for (r in 0 until rows) {
                                val rowIsCorner = r < 2 || r >= rows - 2
                                for (c in 0 until cols) {
                                    // Make corners square visual anchors
                                    val colIsCorner = c < 2 || c >= cols - 2
                                    val isCorner = rowIsCorner && colIsCorner
                                    if (isCorner || random.nextBoolean()) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(c * cellW, r * cellH),
                                            size = Size(cellW, cellH)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = L.t("Digital receipt contains dynamic signature. Compatible with Android printers."), 
                        fontSize = 10.sp, 
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), 
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        Toast.makeText(context, "Receipt sent to Bluetooth Thermal Printer! (ESC/POS 58mm)", Toast.LENGTH_LONG).show()
                        invoicePreviewHtml = null 
                    }
                ) {
                    Text(L.t("Print Receipt"))
                }
            },
            dismissButton = {
                TextButton(onClick = { invoicePreviewHtml = null }) {
                    Text(L.t("Close"))
                }
            }
        )
    }
}

@Composable
fun CalculationRow(label: String, value: String, isBold: Boolean = false, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = color)
        Text(text = value, fontSize = 12.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = color)
    }
}

// ======================================================
// MODULE 7: PAYMENTS & FINANCIALS
// ======================================================
@Composable
fun PaymentsScreen(viewModel: DairyViewModel) {
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val payments by viewModel.allPayments.collectAsStateWithLifecycle()

    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var payAmountText by remember { mutableStateOf("") }
    var payMethod by remember { mutableStateOf("UPI") }
    var payNotes by remember { mutableStateOf("") }

    var expandedDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("💳 Farmer Payment & Ledger Office", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Text("Receive payments for retail supplies or payout farmers with instant adjustments.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Customer selection
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCustomer?.name ?: "Select farmer / buyer...",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Account Profile") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        customers.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.name} (Balance Ledger: $${c.balance})") },
                                onClick = {
                                    selectedCustomer = c
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = payAmountText,
                    onValueChange = { payAmountText = it },
                    label = { Text("Amount Received ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = payNotes,
                    onValueChange = { payNotes = it },
                    label = { Text("Transaction Notes (GTR, receipt references...)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Save
                Button(
                    onClick = {
                        val cust = selectedCustomer
                        val amt = payAmountText.toDoubleOrNull() ?: 0.0
                        if (cust != null && amt > 0) {
                            viewModel.receivePayment(cust.id, amt, payMethod, payNotes)
                            selectedCustomer = null
                            payAmountText = ""
                            payNotes = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Post Ledger Payment Receipt", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("📜 Payment Journal Log", fontWeight = FontWeight.Bold, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(10.dp))

        if (payments.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No logged payments in this journal.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(payments) { p ->
                    val farmerName = customers.find { it.id == p.customerId }?.name ?: "Supplier #${p.customerId}"
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(farmerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Ref: ${p.transactionId} | Notes: ${p.notes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Text("+$${p.amountReceived}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

// ======================================================
// MODULE 8: EXPENSE MANAGEMENT
// ======================================================
@Composable
fun ExpensesScreen(viewModel: DairyViewModel) {
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()

    var category by remember { mutableStateOf("Animal Feed") }
    var amountText by remember { mutableStateOf("") }
    var descText by remember { mutableStateOf("") }

    val categories = listOf("Animal Feed", "Diesel", "Salary", "Electricity", "Maintenance", "Others")
    var expandedDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("💸 Business Expenditure Ledger", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Text("Record operations costs (Salary, diesel, storage repair) to calculate business profit.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expense Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount Spent ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = descText,
                    onValueChange = { descText = it },
                    label = { Text("Description / Vendor") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.addExpense(category, amt, descText)
                            amountText = ""
                            descText = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Commit Business Expense", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("📋 Business Expense Journal", fontWeight = FontWeight.Bold, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(10.dp))

        if (expenses.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No expenses logged in this cycle.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(expenses) { exp ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(exp.category, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(exp.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("-$${exp.amount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(onClick = { viewModel.deleteExpense(exp) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ======================================================
// MODULE 9: COMPREHENSIVE REPORTS VIEW
// ======================================================
@Composable
fun ReportsScreen(viewModel: DairyViewModel) {
    val collections by viewModel.allCollections.collectAsStateWithLifecycle()
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val dp by viewModel.dairyProfile.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("Profit/Loss") }
    var showPdfReportPreview by remember { mutableStateOf(false) }
    var showEmailTemplatePreview by remember { mutableStateOf(false) }
    var showWhatsappSharePreview by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("📊 Commercial Report & Ledger statements", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Text("Export financial performance directly or generate Excel layouts.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Profit/Loss", "Supplies", "Retail sales").forEach { tab ->
                Button(
                    onClick = { activeTab = tab },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == tab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (activeTab == tab) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(tab, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card displaying cumulative totals
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (activeTab) {
                    "Profit/Loss" -> {
                        val sales = invoices.sumOf { it.totalAmount }
                        val supplyCost = collections.sumOf { it.amount }
                        val opsCosts = expenses.sumOf { it.amount }
                        val net = sales - (supplyCost + opsCosts)

                        Text("💸 Annual Profit / Loss Statement", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        HorizontalDivider()
                        CalculationRow(label = "Total retail sales generated:", value = "$${String.format("%.2f", sales)}")
                        CalculationRow(label = "Total farmer milk supply payouts:", value = "-$${String.format("%.2f", supplyCost)}", color = MaterialTheme.colorScheme.error)
                        CalculationRow(label = "Total operational expenditures:", value = "-$${String.format("%.2f", opsCosts)}", color = MaterialTheme.colorScheme.error)
                        HorizontalDivider()
                        CalculationRow(label = "Net Net Enterprise Margin:", value = "$${String.format("%.2f", net)}", isBold = true, color = if (net >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    }
                    "Supplies" -> {
                        val totalVolume = collections.sumOf { it.quantity }
                        val avgFat = if (collections.isEmpty()) 0.0 else collections.map { it.fat }.average()
                        val avgSnf = if (collections.isEmpty()) 0.0 else collections.map { it.snf }.average()

                        Text("🥛 Direct Farmer Milk Inflow Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        HorizontalDivider()
                        CalculationRow(label = "Total quantity of milk supplied:", value = "${String.format("%.1f", totalVolume)} Litres")
                        CalculationRow(label = "Average FAT level calculated:", value = "${String.format("%.2f", avgFat)} %")
                        CalculationRow(label = "Average SNF level calculated:", value = "${String.format("%.2f", avgSnf)} %")
                    }
                    "Retail sales" -> {
                        val invoiceCount = invoices.size
                        val totalSales = invoices.sumOf { it.totalAmount }
                        val avgTicket = if (invoices.isEmpty()) 0.0 else totalSales / invoiceCount

                        Text("🧾 Retail Sales KPI metrics", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        HorizontalDivider()
                        CalculationRow(label = "Committed Sales Receipts:", value = "$invoiceCount Invoices")
                        CalculationRow(label = "Average invoice basket value:", value = "$${String.format("%.2f", avgTicket)}")
                        CalculationRow(label = "Gross sales margin value:", value = "$${String.format("%.2f", totalSales)}", isBold = true)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "📤 DISPATCH & EXPORT DIGITAL SCHEMES",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showPdfReportPreview = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF Report", fontSize = 10.sp, color = Color.White)
                    }
                    Button(
                        onClick = { showEmailTemplatePreview = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Email Template", fontSize = 10.sp, color = Color.White)
                    }
                    Button(
                        onClick = { showWhatsappSharePreview = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }
    }

    // PDF Reports Dialog Preview
    if (showPdfReportPreview) {
        val dpVal = dp ?: com.example.data.model.DairyProfileEntity()
        AlertDialog(
            onDismissRequest = { showPdfReportPreview = false },
            title = { Text("PDF Document Generator") },
            text = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(4.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text(
                            text = dpVal.dairyName.uppercase(Locale.ROOT),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "Proprietor: ${dpVal.ownerName} | GSTIN: ${dpVal.gstNumber}",
                            fontSize = 10.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Email: ${dpVal.emailAddress} | Mob: ${dpVal.contactNumber}",
                            fontSize = 10.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Address: ${dpVal.address}",
                            fontSize = 10.sp,
                            color = Color.DarkGray
                        )
                        if (dpVal.website.isNotEmpty()) {
                            Text(
                                text = "Website: ${dpVal.website}",
                                fontSize = 10.sp,
                                color = Color.Blue
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Black)
                        
                        Text(
                            text = "COMMERCIAL LEDGER STATEMENT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text("Generated on: ${SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())}", fontSize = 10.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Milk Purchases Total: ₹${String.format("%.2f", collections.sumOf { it.amount })}", fontSize = 11.sp, color = Color.Black)
                        Text("Sales Total Invoice: ₹${String.format("%.2f", invoices.sumOf { it.totalAmount })}", fontSize = 11.sp, color = Color.Black)
                        Text("Expenses Outflow: ₹${String.format("%.2f", expenses.sumOf { it.amount })}", fontSize = 11.sp, color = Color.Black)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Authorized Signature: ${dpVal.signature}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.End)
                        )
                        if (dpVal.logo.isNotEmpty()) {
                            Text(
                                text = "[Seal: ${dpVal.logo}]",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showPdfReportPreview = false
                    Toast.makeText(context, "PDF Report successfully saved to /downloads!", Toast.LENGTH_LONG).show()
                }) {
                    Text("Download PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPdfReportPreview = false }) { Text("Close") }
            }
        )
    }

    // Email Templates Dialog Preview
    if (showEmailTemplatePreview) {
        val dpVal = dp ?: com.example.data.model.DairyProfileEntity()
        AlertDialog(
            onDismissRequest = { showEmailTemplatePreview = false },
            title = { Text("Email Template Outbox") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Subject: Daily Performance Report - ${dpVal.dairyName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(4.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Dear Valued Partner,\n\nPlease find the active daily performance statistics of ${dpVal.dairyName} below:\n", fontSize = 11.sp)
                            Text("• Total Farmer Collections: ${collections.size} records", fontSize = 11.sp)
                            Text("• Active Ledger balance: ₹${String.format("%.2f", collections.sumOf { it.amount })}", fontSize = 11.sp)
                            Text("• Operational cost: ₹${String.format("%.2f", expenses.sumOf { it.amount })}", fontSize = 11.sp)
                            Text("\nFor any invoice queries or updates, reply to this mail or call ${dpVal.contactNumber}.\n\nBest Regards,\n${dpVal.ownerName}\nAdmin, ${dpVal.dairyName}\nEmail: ${dpVal.emailAddress}\nWebsite: ${dpVal.website}", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showEmailTemplatePreview = false
                    Toast.makeText(context, "Email dispatched via DairyHub SMTP Gateway!", Toast.LENGTH_LONG).show()
                }) {
                    Text("Send Email")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmailTemplatePreview = false }) { Text("Close") }
            }
        )
    }

    // WhatsApp Share Templates Dialog Preview
    if (showWhatsappSharePreview) {
        val dpVal = dp ?: com.example.data.model.DairyProfileEntity()
        AlertDialog(
            onDismissRequest = { showWhatsappSharePreview = false },
            title = { Text("WhatsApp Share Template") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Format: SMS & WhatsApp Business API", fontSize = 12.sp, color = Color.Gray)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, Color(0xFFB9F6CA)), RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "*${dpVal.dairyName.uppercase(Locale.ROOT)}*\n" +
                                        "━━━━━━━━━━━━━━━━━━━\n" +
                                        "Dear Farmer, your daily milk receipt ledger has been updated.\n" +
                                        "• Supply Amount: ₹${String.format("%.2f", collections.sumOf { it.amount })}\n" +
                                        "• UPI Gateway: ${dpVal.upiId}\n" +
                                        "• Owner helpline: ${dpVal.contactNumber}\n" +
                                        "• Support: ${dpVal.emailAddress}\n" +
                                        "• Online Portal: ${dpVal.website}\n" +
                                        "━━━━━━━━━━━━━━━━━━━\n" +
                                        "Thank you for partnering with us!",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.Black
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showWhatsappSharePreview = false
                    Toast.makeText(context, "Template shared to WhatsApp Business contacts!", Toast.LENGTH_LONG).show()
                }) {
                    Text("Share Template")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWhatsappSharePreview = false }) { Text("Close") }
            }
        )
    }
}

// ======================================================
// MODULE 10: ENTERPRISE SETTINGS & UTILITY HUB
// ======================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(viewModel: DairyViewModel) {
    val context = LocalContext.current
    val activeUser by viewModel.activeUser.collectAsStateWithLifecycle()

    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val appLockEnabled by viewModel.appLockEnabled.collectAsStateWithLifecycle()
    val printerType by viewModel.printerType.collectAsStateWithLifecycle()
    val smsApp by viewModel.smsApp.collectAsStateWithLifecycle()
    val whatsappSetting by viewModel.whatsappSetting.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val centerName by viewModel.centerName.collectAsStateWithLifecycle()
    val centerCode by viewModel.centerCode.collectAsStateWithLifecycle()
    val dairyProfile by viewModel.dairyProfile.collectAsStateWithLifecycle()

    val connectedPrinterName by viewModel.connectedPrinterName.collectAsStateWithLifecycle()
    val connectedPrinterAddress by viewModel.connectedPrinterAddress.collectAsStateWithLifecycle()
    val printerSize by viewModel.printerSize.collectAsStateWithLifecycle()
    var showTestPrintPreview by remember { mutableStateOf<String?>(null) }

    // Social Media & Support Info flows
    var showSocialDialog by remember { mutableStateOf(false) }
    val facebookLink by viewModel.facebookLink.collectAsStateWithLifecycle()
    val instagramLink by viewModel.instagramLink.collectAsStateWithLifecycle()
    val youtubeLink by viewModel.youtubeLink.collectAsStateWithLifecycle()
    val linkedinLink by viewModel.linkedinLink.collectAsStateWithLifecycle()
    val telegramLink by viewModel.telegramLink.collectAsStateWithLifecycle()
    val supportTime by viewModel.supportTime.collectAsStateWithLifecycle()

    // Dialog control states
    var showProfileDialog by remember { mutableStateOf(false) }
    var showCenterDialog by remember { mutableStateOf(false) }
    var showComputerDialog by remember { mutableStateOf(false) }
    var showPrinterDialog by remember { mutableStateOf(false) }
    var showSmsDialog by remember { mutableStateOf(false) }
    var showWhatsappDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showFooterDialog by remember { mutableStateOf(false) }
    var footerDialogTitle by remember { mutableStateOf("") }
    var footerDialogContent by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar matching Image 2
        Surface(
            color = Color(0xFF00A38B),
            contentColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        // Scrollable List of Settings
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Profile
            SettingItemRow(
                icon = Icons.Default.Person,
                title = L.t("Profile"),
                onClick = { showProfileDialog = true }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // 2. Dairy Profile Settings
            SettingItemRow(
                icon = Icons.Default.Store,
                title = "Dairy Profile Settings",
                onClick = { showCenterDialog = true }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // 3. Computer Login
            SettingItemRow(
                icon = Icons.Default.Laptop,
                title = L.t("Computer Login"),
                onClick = { showComputerDialog = true }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // 4. App Lock [Off/On]
            SettingItemRow(
                icon = Icons.Default.Lock,
                title = L.t("App Lock"),
                statusText = if (appLockEnabled) L.t("On") else L.t("Off"),
                onClick = {
                    viewModel.setAppLockEnabled(!appLockEnabled)
                    ToastUtil.show(context, L.t("App Lock turned") + " " + (if (!appLockEnabled) L.t("On") else L.t("Off")))
                }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // 5. Printer [Laser/Inject]
            SettingItemRow(
                icon = Icons.Default.Print,
                title = L.t("Printer Settings"),
                statusText = if (printerType == "Thermal / POS Bluetooth") "Thermal ($printerSize)" else printerType,
                onClick = { showPrinterDialog = true }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // 6. SMS Application [Phone Message App]
            SettingItemRow(
                icon = Icons.Default.Sms,
                title = L.t("SMS Settings"),
                statusText = smsApp,
                onClick = { showSmsDialog = true }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // 7. WhatsApp [WhatsApp]
            SettingItemRow(
                icon = Icons.Default.Chat,
                title = L.t("WhatsApp Settings"),
                statusText = whatsappSetting,
                onClick = { showWhatsappDialog = true }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // 8. Language [English]
            SettingItemRow(
                icon = Icons.Default.Language,
                title = L.t("Language Preference"),
                statusText = language,
                onClick = { showLanguageDialog = true }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // NEW: Dark Theme Toggle row (aur dark theme light theme bhi add Krna baki hai)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(
                                width = 1.dp,
                                color = Color(0xFF00A38B).copy(alpha = 0.5f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = Color(0xFF00A38B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Dark Theme",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.setDarkTheme(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00A38B),
                        checkedTrackColor = Color(0xFF00A38B).copy(alpha = 0.4f)
                    )
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // Social Media Settings
            SettingItemRow(
                icon = Icons.Default.Share,
                title = "Social Media Settings",
                onClick = { showSocialDialog = true }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // About DairyHub Screen
            SettingItemRow(
                icon = Icons.Default.Info,
                title = "About DairyHub",
                onClick = { viewModel.navigateTo(Screen.AboutUs) }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // 9. Sign out
            SettingItemRow(
                icon = Icons.Default.ExitToApp,
                title = "Sign out",
                onClick = { viewModel.logout() }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // Gray spacer block and version label (matching Image 2)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "App Version: v_4.1.1",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            // Footer links (About Us, Privacy policy, Terms & Conditions, Refund & Cancellation policy)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                val footers = listOf(
                    "About Us" to "DairyHub Enterprise v_4.1.1\n\nDeveloped to empower small, medium and large-scale dairy collection centers. We offer direct digital scales, thermal bluetooth printers integration, and local database persistence to secure milk collections, payments and customer statements without internet dependencies.",
                    "Privacy policy" to "At DairyHub, we value your privacy. All dairy farmer database records, transaction sheets, and weight receipts are stored fully locally inside your device's secured database. We do not transmit nor sell your commercial dairy registers to any third-party networks without your explicit consent.",
                    "Terms & Conditions" to "Usage of DairyHub implies complete acceptance of offline-safety guidelines. Ensure local database backups are frequently exported to external physical drives via the Settings export menu to avoid data loss from hardware defects. We do not assume liability for manual bookkeeping mistakes.",
                    "Refund & Cancellation policy" to "All direct software upgrades, cloud backups synchronization services, and premium thermal roll printers integrations are covered under our 30-day trial policy. If you wish to cancel or seek technical assistance, contact support at support@dairyhub.net."
                )

                footers.forEach { (title, detail) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (title == "About Us") {
                                    viewModel.navigateTo(Screen.AboutUs)
                                } else {
                                    footerDialogTitle = title
                                    footerDialogContent = detail
                                    showFooterDialog = true
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                }
            }
        }
    }

    // ========================================================
    // DIALOG IMPLEMENTATIONS FOR EACH SETTING
    // ========================================================

    // Social Media Settings Dialog
    if (showSocialDialog) {
        var tempFacebook by remember { mutableStateOf(facebookLink) }
        var tempInstagram by remember { mutableStateOf(instagramLink) }
        var tempYoutube by remember { mutableStateOf(youtubeLink) }
        var tempLinkedin by remember { mutableStateOf(linkedinLink) }
        var tempTelegram by remember { mutableStateOf(telegramLink) }
        var tempSupport by remember { mutableStateOf(supportTime) }

        AlertDialog(
            onDismissRequest = { showSocialDialog = false },
            title = { Text("Update Social & Support Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = tempFacebook,
                        onValueChange = { tempFacebook = it },
                        label = { Text("Facebook URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempInstagram,
                        onValueChange = { tempInstagram = it },
                        label = { Text("Instagram URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempYoutube,
                        onValueChange = { tempYoutube = it },
                        label = { Text("YouTube Channel URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempLinkedin,
                        onValueChange = { tempLinkedin = it },
                        label = { Text("LinkedIn Company URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempTelegram,
                        onValueChange = { tempTelegram = it },
                        label = { Text("Telegram Group/Contact URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempSupport,
                        onValueChange = { tempSupport = it },
                        label = { Text("Support Operating Time") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSocialLinksAndSupportTime(
                            facebook = tempFacebook,
                            instagram = tempInstagram,
                            youtube = tempYoutube,
                            linkedin = tempLinkedin,
                            telegram = tempTelegram,
                            support = tempSupport
                        )
                        showSocialDialog = false
                        ToastUtil.show(context, "Social media and support profiles updated!")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A38B))
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSocialDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // 1. Profile Dialog
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("User Profile") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .align(Alignment.CenterHorizontally)
                            .background(Color(0xFF00A38B).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF00A38B),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Full Name: ${activeUser?.fullName ?: "N/A"}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Username: ${activeUser?.username ?: "N/A"}", fontSize = 14.sp)
                    Text("Access Role: ${activeUser?.role ?: "N/A"}", fontSize = 14.sp)
                    Text("Phone: ${activeUser?.phone ?: "N/A"}", fontSize = 14.sp)
                    Text("Email: ${activeUser?.email ?: "N/A"}", fontSize = 14.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Close", color = Color(0xFF00A38B))
                }
            }
        )
    }

    // 2. Dairy Profile Dialog
    if (showCenterDialog) {
        val dpVal = dairyProfile ?: com.example.data.model.DairyProfileEntity()
        var tempName by remember { mutableStateOf(dpVal.dairyName) }
        var tempOwner by remember { mutableStateOf(dpVal.ownerName) }
        var tempContact by remember { mutableStateOf(dpVal.contactNumber) }
        var tempEmail by remember { mutableStateOf(dpVal.emailAddress) }
        var tempAddress by remember { mutableStateOf(dpVal.address) }
        var tempGst by remember { mutableStateOf(dpVal.gstNumber) }
        var tempPan by remember { mutableStateOf(dpVal.panNumber) }
        var tempLogo by remember { mutableStateOf(dpVal.logo) }
        var tempSignature by remember { mutableStateOf(dpVal.signature) }
        var tempWebsite by remember { mutableStateOf(dpVal.website) }
        var tempUpi by remember { mutableStateOf(dpVal.upiId) }

        AlertDialog(
            onDismissRequest = { showCenterDialog = false },
            title = { Text("Update Dairy Profile Settings") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Dairy Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempOwner,
                        onValueChange = { tempOwner = it },
                        label = { Text("Owner Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempContact,
                        onValueChange = { tempContact = it },
                        label = { Text("Contact Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempEmail,
                        onValueChange = { tempEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempAddress,
                        onValueChange = { tempAddress = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempGst,
                        onValueChange = { tempGst = it },
                        label = { Text("GST Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempPan,
                        onValueChange = { tempPan = it },
                        label = { Text("PAN Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempLogo,
                        onValueChange = { tempLogo = it },
                        label = { Text("Logo URL / Symbol Key") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempSignature,
                        onValueChange = { tempSignature = it },
                        label = { Text("Signature Key") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempWebsite,
                        onValueChange = { tempWebsite = it },
                        label = { Text("Website Domain") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempUpi,
                        onValueChange = { tempUpi = it },
                        label = { Text("UPI Merchant ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateDairyProfile(
                            dairyName = tempName,
                            ownerName = tempOwner,
                            contactNumber = tempContact,
                            emailAddress = tempEmail,
                            address = tempAddress,
                            gstNumber = tempGst,
                            panNumber = tempPan,
                            logo = tempLogo,
                            signature = tempSignature,
                            website = tempWebsite,
                            upiId = tempUpi
                        )
                        showCenterDialog = false
                        ToastUtil.show(context, "Dairy profile successfully updated and synced!")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A38B))
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCenterDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // 3. Computer Login Dialog
    if (showComputerDialog) {
        AlertDialog(
            onDismissRequest = { showComputerDialog = false },
            title = { Text("Computer Login Sync") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Laptop,
                        contentDescription = null,
                        tint = Color(0xFF00A38B),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "To login on your desktop client or sync with central server, scan the QR code below or click 'Authorize Device'.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(6) { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    repeat(6) { col ->
                                        val isFilled = (row + col) % 2 == 0 || (row == 0 || row == 5 || col == 0 || col == 5)
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(if (isFilled) Color.DarkGray else Color.White)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showComputerDialog = false
                        ToastUtil.show(context, "Computer Authorization Token Generated!")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A38B))
                ) {
                    Text("Authorize Device", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showComputerDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // 4. Printer Dialog (Bluetooth configuration panel)
    if (showPrinterDialog) {
        val printerOptions = listOf("Laser/Inject", "Thermal / POS Bluetooth", "Dot Matrix", "Disabled")
        AlertDialog(
            onDismissRequest = { showPrinterDialog = false },
            title = { Text(L.t("Configure Printer"), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(L.t("Printer Type"), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    printerOptions.forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setPrinterType(opt)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = printerType == opt,
                                onClick = {
                                    viewModel.setPrinterType(opt)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00A38B))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(opt, fontSize = 14.sp)
                        }
                    }

                    if (printerType == "Thermal / POS Bluetooth") {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Text(L.t("Printer Size"), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            listOf("58mm", "80mm").forEach { sz ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { viewModel.setPrinterSize(sz) }
                                ) {
                                    RadioButton(
                                        selected = printerSize == sz,
                                        onClick = { viewModel.setPrinterSize(sz) },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00A38B))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(sz, fontSize = 14.sp)
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(L.t("Bluetooth Printer"), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            if (connectedPrinterAddress.isNotEmpty()) {
                                Text(
                                    text = L.t("Connected"),
                                    color = Color(0xFF00A38B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            } else {
                                Text(
                                    text = L.t("Not Connected"),
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (connectedPrinterAddress.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF00A38B).copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(connectedPrinterName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(connectedPrinterAddress, fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.disconnectPrinter()
                                            ToastUtil.show(context, L.t("Printer Disconnected"))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(L.t("Disconnect"), fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(L.t("Paired Devices"), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                        val paired = PrinterHelper.getPairedPrinters(context)
                        paired.forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        viewModel.connectPrinter(p.first, p.second)
                                        ToastUtil.show(context, "${L.t("Connected")}: ${p.first}")
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(p.first, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text(p.second, fontSize = 11.sp, color = Color.Gray)
                                }
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (connectedPrinterAddress == p.second) Color(0xFF00A38B) else Color.Transparent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Button(
                            onClick = {
                                if (connectedPrinterAddress.isEmpty()) {
                                    ToastUtil.show(context, L.t("Error: Connect a Bluetooth Printer first!"))
                                } else {
                                    val testFields = mapOf(
                                        "Receipt Type" to "Test Print",
                                        "Status" to "Online / Active",
                                        "Baud Rate" to "9600 bps",
                                        "ESC/POS Command" to "ASCII standard",
                                        "App Version" to "v_4.1.1"
                                    )
                                    val receipt = PrinterHelper.generateReceiptText(
                                        title = "DairyHub Test Print",
                                        centerName = centerName,
                                        centerCode = centerCode,
                                        fields = testFields,
                                        size = printerSize
                                    )
                                    showTestPrintPreview = receipt
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A38B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(L.t("Test Print"), color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrinterDialog = false }) {
                    Text(L.t("Close"), color = Color(0xFF00A38B))
                }
            }
        )
    }

    // Interactive Test Print Preview popup dialog
    if (showTestPrintPreview != null) {
        AlertDialog(
            onDismissRequest = { showTestPrintPreview = null },
            title = { Text(L.t("Simulated Printed Receipt"), fontWeight = FontWeight.Bold) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = showTestPrintPreview ?: "",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.Black,
                        lineHeight = 16.sp,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTestPrintPreview = null
                        ToastUtil.show(context, L.t("Printed Successfully!"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A38B))
                ) {
                    Text(L.t("Got it"), color = Color.White)
                }
            }
        )
    }

    // 5. SMS Dialog
    if (showSmsDialog) {
        val smsOptions = listOf("Phone Message App", "Twilio API Gateway", "TextLocal SMS Router", "Disabled")
        AlertDialog(
            onDismissRequest = { showSmsDialog = false },
            title = { Text("Configure SMS Gateway") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    smsOptions.forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setSmsApp(opt)
                                    showSmsDialog = false
                                    ToastUtil.show(context, "SMS routing set: $opt")
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = smsApp == opt,
                                onClick = {
                                    viewModel.setSmsApp(opt)
                                    showSmsDialog = false
                                    ToastUtil.show(context, "SMS routing set: $opt")
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00A38B))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(opt, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // 6. WhatsApp Dialog
    if (showWhatsappDialog) {
        val waOptions = listOf("WhatsApp", "WhatsApp Business", "Cloud API Gateway", "Disabled")
        AlertDialog(
            onDismissRequest = { showWhatsappDialog = false },
            title = { Text("Configure WhatsApp Share") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    waOptions.forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setWhatsappSetting(opt)
                                    showWhatsappDialog = false
                                    ToastUtil.show(context, "WhatsApp client set: $opt")
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = whatsappSetting == opt,
                                onClick = {
                                    viewModel.setWhatsappSetting(opt)
                                    showWhatsappDialog = false
                                    ToastUtil.show(context, "WhatsApp client set: $opt")
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00A38B))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(opt, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // 7. Language Dialog
    if (showLanguageDialog) {
        val langOptions = listOf("English", "Hindi (हिन्दी)", "Punjabi (ਪੰਜਾਬੀ)", "Marathi (मਰਾઠી)")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    langOptions.forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguage(opt)
                                    showLanguageDialog = false
                                    ToastUtil.show(context, "Language switched to $opt")
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = language == opt,
                                onClick = {
                                    viewModel.setLanguage(opt)
                                    showLanguageDialog = false
                                    ToastUtil.show(context, "Language switched to $opt")
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00A38B))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(opt, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // 8. Footer Info Dialog
    if (showFooterDialog) {
        AlertDialog(
            onDismissRequest = { showFooterDialog = false },
            title = { Text(footerDialogTitle, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = footerDialogContent,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(onClick = { showFooterDialog = false }) {
                    Text("Got it", color = Color(0xFF00A38B))
                }
            }
        )
    }
}

@Composable
fun SettingItemRow(
    icon: ImageVector,
    title: String,
    statusText: String = "",
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon in thin outline circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(
                    width = 1.dp,
                    color = Color(0xFF00A38B).copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF00A38B),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Title text and status text if applicable
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (statusText.isNotEmpty()) {
                Text(
                    text = "[$statusText]",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00A38B),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        // Chevron Right Icon
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}

// Utility class for native Android Toast messages inside Compose click logs
object ToastUtil {
    fun show(context: android.content.Context, msg: String) {
        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun NotificationsDialog(
    viewModel: DairyViewModel,
    onDismiss: () -> Unit
) {
    val notifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val unreadNotifications by viewModel.unreadNotifications.collectAsStateWithLifecycle()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔔 Notification Desk (${unreadNotifications.size})")
                TextButton(onClick = { viewModel.markAllNotificationsRead() }) {
                    Text("Clear All")
                }
            }
        },
        text = {
            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Inbox is empty.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notifications) { n ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (n.isRead) Color.Transparent else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    viewModel.markNotificationRead(n.id)
                                    ToastUtil.show(context, "Marked as read: ${n.title}")
                                }
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!n.isRead) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(n.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(n.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Dismiss Desk")
            }
        }
    )
}

@Composable
fun QuickAddCustomerDialog(
    viewModel: DairyViewModel,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    var code by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var milkType by remember { mutableStateOf("Both") }
    var address by remember { mutableStateOf("") }
    var openingBalance by remember { mutableStateOf("0.0") }
    var customerType by remember { mutableStateOf("Farmer") }
    var paymentMode by remember { mutableStateOf("Cash") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(L.t("Add Customer"), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text(L.t("Code")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text(L.t("First Name")) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text(L.t("Last Name")) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(L.t("Mobile")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Customer Type selection
                Text(L.t("Customer Type") + ":", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("Farmer", "Retailer", "Wholesaler", "Staff").forEach { ct ->
                        val selected = customerType == ct
                        Button(
                            onClick = { customerType = ct },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(L.t(ct), fontSize = 10.sp)
                        }
                    }
                }

                // Payment Mode selection
                Text(L.t("Payment Mode") + ":", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("Cash", "UPI", "Credit", "Bank Transfer").forEach { pm ->
                        val selected = paymentMode == pm
                        Button(
                            onClick = { paymentMode = pm },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(L.t(pm), fontSize = 10.sp)
                        }
                    }
                }
                
                Text("Milk Type Supplied:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("Cow", "Buffalo", "Both").forEach { mt ->
                        val selected = milkType == mt
                        Button(
                            onClick = { milkType = mt },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(mt, fontSize = 11.sp)
                        }
                    }
                }
                
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(L.t("Address")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = openingBalance,
                    onValueChange = { openingBalance = it },
                    label = { Text(L.t("Opening Balance")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fullName = "${firstName.trim()} ${lastName.trim()}".trim()
                    if (fullName.isNotEmpty() && phone.isNotEmpty()) {
                        val finalCode = code.ifEmpty { "C-${System.currentTimeMillis().toString().takeLast(4)}" }
                        val bal = openingBalance.toDoubleOrNull() ?: 0.0
                        viewModel.addCustomer(
                            name = fullName,
                            phone = phone,
                            address = address,
                            code = finalCode,
                            milkType = milkType,
                            openingBalance = bal,
                            customerType = customerType,
                            paymentMode = paymentMode,
                            isActive = true
                        )
                        Toast.makeText(context, "Customer profile successfully registered", Toast.LENGTH_SHORT).show()
                        onSuccess(phone)
                    } else {
                        Toast.makeText(context, "Full Name and Phone are required fields", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(L.t("Save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(L.t("Cancel"))
            }
        }
    )
}

@Composable
fun GlassyCard(
    modifier: Modifier = Modifier,
    isDark: Boolean,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val containerColor = if (isDark) {
        Color(0x2B211F26)
    } else {
        Color(0x9EFEF7FF)
    }
    
    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color(0xFF6750A4).copy(alpha = 0.15f)
    }

    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            content()
        }
    }
}

@Composable
fun AboutUsScreen(viewModel: com.example.ui.viewmodel.DairyViewModel) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    
    // Dynamic company data from settings
    val dairyProfile by viewModel.dairyProfile.collectAsStateWithLifecycle()
    val supportTime by viewModel.supportTime.collectAsStateWithLifecycle()
    val facebookLink by viewModel.facebookLink.collectAsStateWithLifecycle()
    val instagramLink by viewModel.instagramLink.collectAsStateWithLifecycle()
    val youtubeLink by viewModel.youtubeLink.collectAsStateWithLifecycle()
    val linkedinLink by viewModel.linkedinLink.collectAsStateWithLifecycle()
    val telegramLink by viewModel.telegramLink.collectAsStateWithLifecycle()

    val dpVal = dairyProfile ?: com.example.data.model.DairyProfileEntity()
    val dairyName = dpVal.dairyName
    val ownerName = dpVal.ownerName
    val contactNumber = dpVal.contactNumber
    val emailAddress = dpVal.emailAddress
    val address = dpVal.address
    val website = dpVal.website
    val gstNumber = dpVal.gstNumber
    val panNumber = dpVal.panNumber
    val upiId = dpVal.upiId

    // Interactive states
    var selectedFeature by remember { mutableStateOf<String?>(null) }
    var selectedLegalDoc by remember { mutableStateOf<String?>(null) }

    // Colors
    val primaryColor = Color(0xFF00A38B)
    val textPrimary = if (isDark) Color.White else Color(0xFF1D1B20)
    val textSecondary = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF1D1B20).copy(alpha = 0.7f)

    // Full screen gradient background matching theme
    val backgroundBrush = if (isDark) {
        Brush.linearGradient(listOf(Color(0xFF0F0E13), Color(0xFF1C1A22)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFF7F5FC), Color(0xFFE2D7F3)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // Decorative iOS Orbs in background
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.TopStart)
                .offset(x = (-60).dp, y = 80.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x1F6750A4), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 80.dp, y = 180.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x1F00A38B), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        // Main content column
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Elegant top bar with back button and glass header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0x1A121212) else Color(0x26FFFFFF))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = primaryColor
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "About DairyHub Platform",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            // Scrollable sections
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // SECTION 1: HERO CONTAINER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            if (isDark) Color(0x33FFFFFF) else Color(0x73FFFFFF)
                        )
                        .border(
                            1.dp,
                            if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFF6750A4).copy(alpha = 0.12f),
                            RoundedCornerShape(28.dp)
                        )
                        .padding(vertical = 32.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Dynamic Logo Graphic
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .background(
                                    Brush.linearGradient(listOf(primaryColor, Color(0xFF6750A4))),
                                    RoundedCornerShape(24.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Text(
                            text = "DairyHub Enterprise",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryColor
                        )

                        Text(
                            text = "\"Smart Dairy, Strong Future\"",
                            fontSize = 15.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            color = textSecondary,
                            textAlign = TextAlign.Center
                        )

                        // Info chips (Version, Build, Status)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF00A38B).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("v_4.1.1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF6750A4).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Build #20260705", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6750A4))
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF4CAF50).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Stable Release", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                            }
                        }
                    }
                }

                // SECTION 2: COMPANY INTRODUCTION
                GlassyCard(isDark = isDark) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(primaryColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = primaryColor)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Introduction", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "DairyHub is a premium, enterprise-grade Dairy Management Platform crafted to completely digitize, streamline, and secure daily operations for modern milk collection centers, local dairy farms, milk cooperatives, and diverse milk retail businesses. Running with advanced local SQLite offline database engines, DairyHub empowers business managers to confidently record milk weights, FAT/SNF fractions, calculate fair payout rates, generate invoices, and print receipts without depending on active internet connections.",
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = textSecondary
                    )
                }

                // SECTION 3 & 4: MISSION & VISION
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isDark) Color(0x2B211F26) else Color(0x9EFEF7FF))
                            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFF6750A4).copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF6750A4).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF6750A4), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Our Mission", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Digitize milk collection center mechanics, optimize producer payout transparency, fully eliminate physical bookkeeping, and reduce farmer wait-times.",
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = textSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isDark) Color(0x2B211F26) else Color(0x9EFEF7FF))
                            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFF6750A4).copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(primaryColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Our Vision", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "To become the global cornerstone for local agricultural digitization, powering small cooperatives with enterprise-ready smart tools.",
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = textSecondary
                            )
                        }
                    }
                }

                // SECTION 5: CORE FEATURES (Interactive grid cards!)
                GlassyCard(isDark = isDark) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(primaryColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Stars, contentDescription = null, tint = primaryColor)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Core Functional Modules", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Interactive: Click any module to view details", fontSize = 12.sp, color = textSecondary)
                    Spacer(modifier = Modifier.height(14.dp))

                    val features = listOf(
                        "Milk Collection" to Icons.Default.WaterDrop,
                        "Farmer Management" to Icons.Default.People,
                        "Customer Management" to Icons.Default.PersonOutline,
                        "Milk Sale POS" to Icons.Default.Storefront,
                        "Ledger Management" to Icons.Default.AccountBalance,
                        "Farmer Payments" to Icons.Default.Payment,
                        "Customer Payments" to Icons.Default.Receipt,
                        "Advance Management" to Icons.Default.LocalAtm,
                        "Deduction Management" to Icons.Default.PriceCheck,
                        "Expense Management" to Icons.Default.MoneyOff,
                        "Rate Chart" to Icons.Default.GridOn,
                        "Collection Reports" to Icons.Default.BarChart,
                        "Sales Reports" to Icons.Default.PieChart,
                        "Analytics Dashboard" to Icons.Default.Dashboard,
                        "Invoice Generation" to Icons.Default.Description,
                        "PDF Reports" to Icons.Default.PictureAsPdf,
                        "Excel Import/Export" to Icons.Default.ImportExport,
                        "Bluetooth Thermal Printing" to Icons.Default.Print,
                        "WhatsApp Sharing" to Icons.Default.ChatBubbleOutline,
                        "SMS Notification" to Icons.Default.Message,
                        "Multi Language" to Icons.Default.Translate,
                        "Dark Mode" to Icons.Default.DarkMode,
                        "Cloud Backup" to Icons.Default.CloudUpload,
                        "Secure Authentication" to Icons.Default.VpnKey
                    )

                    // Staggered flowing layout of cards in a chunked 2-column safe layout
                    val chunkedFeatures = features.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        chunkedFeatures.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { (name, icon) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFF6750A4).copy(alpha = 0.04f)
                                            )
                                            .border(
                                                1.dp,
                                                if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFF6750A4).copy(alpha = 0.1f),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .clickable { selectedFeature = name }
                                            .padding(horizontal = 10.dp, vertical = 10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                                if (rowItems.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // SECTION 6: WHY CHOOSE DAIRYHUB
                GlassyCard(isDark = isDark) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(primaryColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = primaryColor)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Why Choose DairyHub", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    val highlights = listOf(
                        Triple(Icons.Default.FlashOn, "Fast Performance", "Optimized offline architecture ensures sub-millisecond calculation speeds."),
                        Triple(Icons.Default.Security, "Secure System", "Enterprise-grade local SQLite encryption protects all client and farmer logs."),
                        Triple(Icons.Default.ThumbUp, "Easy To Use", "Intuitive layouts designed with oversized buttons and simple workflows."),
                        Triple(Icons.Default.Calculate, "Automatic Calculations", "Auto-computes FAT, SNF, CLR, weight, deductions, and total rates."),
                        Triple(Icons.Default.Assessment, "Real-time Reports", "Instantly generates P&L sheets, payout lists, and inventory statements."),
                        Triple(Icons.Default.CloudOff, "Offline Ready", "100% functional without internet connectivity, perfect for rural milk routes."),
                        Triple(Icons.Default.Phonelink, "Mobile Friendly", "Fully fluid responsive design adjusting to smartphones, tablets, or DeX."),
                        Triple(Icons.Default.ReceiptLong, "Professional Billing", "Customizable receipt generation with GST, UPI QR, and signature fields."),
                        Triple(Icons.Default.Group, "Role Based Access", "Separate user clearances for Admins, Managers, and operators."),
                        Triple(Icons.Default.Storage, "Cloud Database", "Optional secure cloud synchronization to access statements remotely.")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        highlights.forEach { (icon, title, desc) ->
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(primaryColor.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    Text(desc, fontSize = 12.sp, color = textSecondary, lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }

                // SECTION 7: COMPANY INFORMATION
                GlassyCard(isDark = isDark) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(primaryColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Store, contentDescription = null, tint = primaryColor)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Company Information", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        }
                        
                        Text(
                            text = "LIVE SYNCED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier
                                .background(Color(0xFF4CAF50).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    val companyFields = listOf(
                        "Company Name" to dairyName,
                        "Owner Name" to ownerName,
                        "Contact Number" to contactNumber,
                        "Email Address" to emailAddress,
                        "Address" to address,
                        "Website" to website,
                        "GST Number" to gstNumber,
                        "PAN Number" to panNumber,
                        "UPI ID" to upiId,
                        "Support Time" to supportTime
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        companyFields.forEach { (label, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textSecondary, modifier = Modifier.weight(1f))
                                Text(
                                    text = value.ifEmpty { "Not Configured" },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1.5f)
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                        }
                    }
                }

                // SECTION 8: DEVELOPER INFORMATION
                GlassyCard(isDark = isDark) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF6750A4).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF6750A4))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Developer Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    val developerFields = listOf(
                        "Developed By" to "DairyHub Development Team",
                        "Support Email" to "ashutoshsingh1363@gmail.com",
                        "Support Phone" to "+91 6207159208",
                        "App Version" to "v_4.1.1",
                        "Release Date" to "July 2026"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        developerFields.forEach { (label, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textSecondary)
                                Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                        }
                    }
                }

                // SECTION 9: TECHNOLOGIES USED
                GlassyCard(isDark = isDark) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(primaryColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = primaryColor)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Technologies Leveraged", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    val technologies = listOf(
                        "React / Next.js" to Color(0xFF00D8FF),
                        "Node.js" to Color(0xFF43853D),
                        "Express.js" to Color(0xFF828282),
                        "PostgreSQL" to Color(0xFF336791),
                        "Prisma ORM" to Color(0xFF0C344B),
                        "Tailwind CSS" to Color(0xFF38BDF8),
                        "JWT Auth" to Color(0xFFD63AFF),
                        "REST API" to Color(0xFFFF5722),
                        "Bluetooth Print" to Color(0xFF2979FF),
                        "Supabase" to Color(0xFF3ECF8E)
                    )

                    val chunkedTech = technologies.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        chunkedTech.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { (tech, color) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(color.copy(alpha = 0.12f))
                                            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = tech,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color.White else color,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                if (rowItems.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // SECTION 10: SECURITY SECTION
                GlassyCard(isDark = isDark) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(primaryColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = primaryColor)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Platform Security", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF4CAF50).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("ENCRYPTED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    val securityItems = listOf(
                        "JWT Authentication" to "Secure JSON Web Token standard protecting managers' web sessions.",
                        "Password Encryption" to "Cryptographic SHA-256 salted hashes preventing raw credential exposure.",
                        "Secure Database" to "Encrypted Android room database engines guarding files locally.",
                        "Role Based Access Control" to "Separation of admin, managers, and operator operational scopes.",
                        "Daily Backup" to "One-tap file persistence exporting encrypted logs locally or to the cloud.",
                        "Secure API Communication" to "All backup integrations leverage SSL/TLS encrypted rest tunnels."
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        securityItems.forEach { (title, desc) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    Text(desc, fontSize = 11.sp, color = textSecondary)
                                }
                            }
                        }
                    }
                }

                // SECTION 11: CONTACT & SUPPORT ACTION BUTTONS
                GlassyCard(isDark = isDark) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(primaryColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, tint = primaryColor)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Connect with Support", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    // Column of buttons trigger actual intents
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                try {
                                    val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contactNumber.ifEmpty { "+916207159208" }}"))
                                    context.startActivity(callIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not launch dialer", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Call Support Desk", fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                try {
                                    val cleanedPhone = contactNumber.ifEmpty { "+916207159208" }.replace("+", "").replace(" ", "").trim()
                                    val url = "https://api.whatsapp.com/send?phone=$cleanedPhone&text=Hello%20DairyHub%20Support%2C%20I%20need%20assistance."
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "WhatsApp support launch failed", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("WhatsApp Live Support", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${emailAddress.ifEmpty { "ashutoshsingh1363@gmail.com" }}")).apply {
                                        putExtra(Intent.EXTRA_SUBJECT, "DairyHub App Support Request")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open email app", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA4335)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = null)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Email Developer Support", fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                try {
                                    val targetWeb = website.ifEmpty { "https://www.dairyhub.com" }
                                    val parsedUrl = if (targetWeb.startsWith("http://") || targetWeb.startsWith("https://")) targetWeb else "https://$targetWeb"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(parsedUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Visit Official Website", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // SECTION 12: SOCIAL MEDIA CHANNELS
                GlassyCard(isDark = isDark) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(primaryColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = primaryColor)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Follow Our Channels", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    val socialList = listOf(
                        "Facebook" to facebookLink,
                        "Instagram" to instagramLink,
                        "YouTube" to youtubeLink,
                        "LinkedIn" to linkedinLink,
                        "Telegram" to telegramLink
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        socialList.forEach { (name, link) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        if (link.isNotEmpty()) {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Link unavailable", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "No link set by Admin", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(6.dp)
                            ) {
                                val ic = when (name) {
                                    "Facebook" -> Icons.Default.Facebook
                                    "Instagram" -> Icons.Default.CameraAlt
                                    "YouTube" -> Icons.Default.PlayArrow
                                    "LinkedIn" -> Icons.Default.WorkOutline
                                    else -> Icons.Default.Send
                                }
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(primaryColor.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(ic, contentDescription = name, tint = primaryColor)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            }
                        }
                    }
                }

                // SECTION 13: LEGAL SECTION
                GlassyCard(isDark = isDark) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(primaryColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Gavel, contentDescription = null, tint = primaryColor)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Legal Agreements & Policy", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    val legalList = listOf(
                        "Privacy Policy",
                        "Terms & Conditions",
                        "Refund Policy",
                        "Support Policy",
                        "Disclaimer",
                        "Open Source Licenses"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        legalList.forEach { doc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedLegalDoc = doc }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(doc, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                }
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = primaryColor.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        }
                    }
                }

                // SECTION 14: COPYRIGHT FOOTER
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "© 2026 DairyHub",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "All Rights Reserved.",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Designed & Developed with ❤️ by DairyHub Team.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryColor
                    )
                }
            }
        }

        // Feature Dialog Details Popup
        selectedFeature?.let { feat ->
            val featureDetails = mapOf(
                "Milk Collection" to "Allows real-time entry of milk weight, fat content, SNF, and CLR with auto-calculated total pricing, custom rate charts, and instant thermal receipt printing.",
                "Farmer Management" to "Digitally register farmers, store bank account details, assign specific milk categories, and keep track of daily deposits, payments, advances, and ledger files.",
                "Customer Management" to "Manage milk purchasers, wholesalers, and retail buyers, track their credits, configure special bulk rates, and generate monthly ledger sheets.",
                "Milk Sale POS" to "Point-of-Sale terminal to directly sell raw milk, butter, ghee, cheese, or pasteurized milk packets to retail or walk-in customers with billing.",
                "Ledger Management" to "Double-entry bookkeeping system capturing all debits, credits, collection deposits, POS sales, and expenditures in real-time, matching standard dairy audits.",
                "Farmer Payments" to "Automate invoice cycles, calculate net milk dues after advance and feed deductions, and process batch payments with complete payment slips.",
                "Customer Payments" to "Track outstanding client balances, record payments received via cash, UPI, or bank transfer, and generate custom client ledger balance statements.",
                "Advance Management" to "Provide cash advances, veterinary loans, or feed credit to registered farmers, with automatic partial repayments deducted from milk payouts.",
                "Deduction Management" to "Configure standard deduction profiles (e.g., local cooperative funds, transport charges, medical advances, custom feed fees) for each farmer.",
                "Expense Management" to "Record dairy operational costs, cooperative staff salaries, electricity bills, machinery maintenance, and transport charges, categorized for P&L tracking.",
                "Rate Chart" to "Create complex, fully customized milk rate matrices based on FAT only, SNF only, FAT + SNF grid, or premium standard cooperative quality grids.",
                "Collection Reports" to "Access granular daily, weekly, or monthly collection registers, sorting by milk source (Cow/Buffalo) and tracking total solids percentage.",
                "Sales Reports" to "Monitor POS milk and product sales volume, track daily cash flow, and review profit margins on value-added dairy products.",
                "Analytics Dashboard" to "Visual analytics displaying peak collection times, total milk flow metrics, monthly revenue streams, customer distribution, and active farmer growth rates.",
                "Invoice Generation" to "Instantly generate professional invoices for retail product buyers, bulk commercial distributors, and local cooperative associations.",
                "PDF Reports" to "Render beautiful, comprehensive digital statements, payment receipts, ledgers, and collection histories into high-quality PDF files ready for distribution.",
                "Excel Import/Export" to "Export complete farmer records, collection logs, and payment sheets to MS Excel format, or import farmer databases for rapid initial onboarding.",
                "Bluetooth Thermal Printing" to "Integrated support for 58mm & 80mm ESC/POS thermal printers to instantly print collection weight slips, billing receipts, and statements.",
                "WhatsApp Sharing" to "Share direct digital receipts, payment details, and ledger statements to customers or farmers on WhatsApp, completely eliminating paper printing.",
                "SMS Notification" to "Automated SMS notification alerts dispatched directly to farmers immediately upon weighing milk or processing standard monthly bank payouts.",
                "Multi Language" to "Complete support for local languages (Hindi, Marathi, Punjabi, Tamil, Telugu, etc.) ensuring effortless usage for farmers and operators alike.",
                "Dark Mode" to "Beautifully crafted night-friendly dark theme that reduces screen glare during early morning (4:00 AM) and late evening milk collection hours.",
                "Cloud Backup" to "Secure, real-time or periodic cloud database synchronization, protecting your critical dairy ledger data from device hardware failures.",
                "Secure Authentication" to "Role-based user authentication (Admin, Manager, Operator) ensuring strict data protection, preventing unauthorized tampering of milk records."
            )

            AlertDialog(
                onDismissRequest = { selectedFeature = null },
                title = { Text(feat, fontWeight = FontWeight.Bold, color = primaryColor) },
                text = {
                    Text(
                        text = featureDetails[feat] ?: "Detailed features description coming soon.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = textPrimary
                    )
                },
                confirmButton = {
                    TextButton(onClick = { selectedFeature = null }) {
                        Text("Close", color = primaryColor, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Legal document details popup
        selectedLegalDoc?.let { docTitle ->
            val legalDocuments = mapOf(
                "Privacy Policy" to """
                    At DairyHub, we take your operational privacy very seriously.
                    
                    1. LOCAL DATA SOVEREIGNTY
                    All farmer names, weight tickets, financial records, milk pricing scales, and customer ledgers are stored strictly in your device's secure local SQLite database using industry-grade Room persistence.
                    
                    2. DATA TRANSMISSION
                    No commercial transactions, personal farmer details, or sales data are transmitted to any external networks or third parties without your explicit administrative authorization.
                    
                    3. DEVICE PERMISSIONS
                    - Bluetooth: Used solely to discover and print receipts on ESC/POS thermal printing devices.
                    - Storage: Used to write/read exported PDF statements and MS Excel ledger sheets.
                    - Internet: Required only if cloud backup synchronization is explicitly enabled.
                """.trimIndent(),
                "Terms & Conditions" to """
                    Welcome to DairyHub. By utilizing this software, you agree to these terms:
                    
                    1. OPERATIONAL RESPONSIBILITY
                    The operator of the application is solely responsible for ensuring the correctness of milk fat, SNF, CLR, and pricing weight figures entered during daily transactions.
                    
                    2. DATABASE MAINTENANCE
                    As DairyHub operates offline, you must perform regular database backups using the export system. DairyHub is not responsible for data loss due to device hardware malfunction, loss, or unauthorized factory resets.
                    
                    3. SOFTWARE USE
                    The software is provided "as is" with no warranty for specific commercial yields. You agree to utilize it in accordance with local dairy cooperative and farming trade laws.
                """.trimIndent(),
                "Refund Policy" to """
                    Thank you for choosing DairyHub.
                    
                    1. PREMIUM UPGRADES
                    All purchases of premium licenses, Bluetooth thermal printer add-ons, or custom cloud backup packages include a 30-day fully functional trial period.
                    
                    2. SUBSCRIPTION REFUNDS
                    If you are unsatisfied with our digital platform, you may request a full refund within 30 days of purchase. No refunds are issued after the 30-day window.
                    
                    3. CUSTOM SUPPORT
                    If you experience technical setup difficulties with weight scale integrations, contact support@dairyhub.net for immediate assistance.
                """.trimIndent(),
                "Support Policy" to """
                    DairyHub is committed to keeping your milk collections running 24/7.
                    
                    1. SUPPORT TICKETS
                    Registered dairy administrators can reach our dedicated technical desk via email or phone.
                    
                    2. RESPONSE SLA
                    - Critical P0 issues (e.g. database corruption or print failures during collections): Within 2 hours.
                    - Normal P1 issues (e.g. adding new staff profiles, custom rate charts): Within 24 hours.
                    
                    3. WORKING HOURS
                    Our technical staff is available from 9:00 AM to 6:00 PM (Monday to Saturday) to ensure uninterrupted operations.
                """.trimIndent(),
                "Disclaimer" to """
                    DairyHub is an independent software tool designed for dairy operations.
                    
                    1. NO GOVERNMENT AFFILIATION
                    DairyHub is not officially affiliated with any government department, National Dairy Development Board (NDDB), or state cooperative federations unless explicitly contracted.
                    
                    2. CALCULATION ACCURACY
                    While our mathematical formulas follow standard SNF/FAT cooperative grids, you must verify calculations against your local cooperative's official dairy manuals before disbursing high-volume payouts.
                """.trimIndent(),
                "Open Source Licenses" to """
                    DairyHub is powered by exceptional open source projects:
                    
                    1. KOTLIN & COROUTINES
                    Copyright JetBrains s.r.o. Licensed under Apache 2.0.
                    
                    2. JETPACK COMPOSE
                    Copyright The Android Open Source Project. Licensed under Apache 2.0.
                    
                    3. ROOM PERSISTENCE
                    Copyright The Android Open Source Project. Licensed under Apache 2.0.
                    
                    4. KOTLINX SERIALIZATION
                    Copyright JetBrains s.r.o. Licensed under Apache 2.0.
                """.trimIndent()
            )

            AlertDialog(
                onDismissRequest = { selectedLegalDoc = null },
                title = { Text(docTitle, fontWeight = FontWeight.Bold, color = primaryColor) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = legalDocuments[docTitle] ?: "Legal content coming soon.",
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = textPrimary
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedLegalDoc = null }) {
                        Text("Accept & Close", color = primaryColor, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}


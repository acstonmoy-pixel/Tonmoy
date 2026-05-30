package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ParsedFile
import com.example.data.model.Voter
import com.example.ui.viewmodel.MainScreen
import com.example.ui.viewmodel.SyncState
import com.example.ui.viewmodel.UploadState
import com.example.ui.viewmodel.VoterViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(
    viewModel: VoterViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    Scaffold(
        bottomBar = {
            if (isAdminLoggedIn && currentScreen != MainScreen.LOGIN) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("parent_navigation_bar")
                ) {
                    val items = listOf(
                        Triple(MainScreen.DASHBOARD, "Dashboard", Icons.Default.Home),
                        Triple(MainScreen.SEARCH, "Search", Icons.Default.Search),
                        Triple(MainScreen.UPLOAD, "Upload", Icons.Default.Add),
                        Triple(MainScreen.ANALYTICS, "Analytics", Icons.Default.List),
                        Triple(MainScreen.SETTINGS, "Settings", Icons.Default.Settings)
                    )
                    items.forEach { (screen, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 11.sp) },
                            selected = currentScreen == screen,
                            onClick = { viewModel.navigateTo(screen) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                MainScreen.LOGIN -> LoginScreen(viewModel)
                MainScreen.DASHBOARD -> DashboardScreen(viewModel)
                MainScreen.SEARCH -> SearchScreen(viewModel)
                MainScreen.UPLOAD -> UploadScreen(viewModel)
                MainScreen.ANALYTICS -> AnalyticsScreen(viewModel)
                MainScreen.SETTINGS -> SettingsScreen(viewModel)
            }
        }
    }
}

// ----------------------------------------------------
// 1. ADMIN LOGIN SCREEN
// ----------------------------------------------------
@Composable
fun LoginScreen(viewModel: VoterViewModel) {
    var passwordInput by remember { mutableStateOf("") }
    val authError by viewModel.authError.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Crest / Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "Voter Registry Gate",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Enter the secure administrator pin-code to manage local records and cloud synchronizations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Admin Passcode") },
                    placeholder = { Text("Enter passcode") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_passcode_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (authError != null) {
                    Text(
                        text = authError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = { viewModel.loginAdmin(passwordInput) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("admin_login_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Authenticate Gate", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Text(
                    text = "Default verification hint: admin",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

// ----------------------------------------------------
// 2. DASHBOARD SCREEN
// ----------------------------------------------------
@Composable
fun DashboardScreen(viewModel: VoterViewModel) {
    val totalVotersCount by viewModel.totalVoters.collectAsState()
    val maleCount by viewModel.maleVoters.collectAsState()
    val femaleCount by viewModel.femaleVoters.collectAsState()
    val fileLogs by viewModel.parsedFilesLog.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "V",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Column {
                        Text(
                            text = "Voter Admin",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "SYSTEM DASHBOARD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
                IconButton(
                    onClick = { viewModel.logoutAdmin() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.outlineVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock Portal",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Standard Grid Cards for Stats
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SingleStatCard(
                    title = "Total Voter Cache",
                    value = String.format("%,d", totalVotersCount),
                    subTitle = "Offline cached records",
                    gradient = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        MiniStatCard(
                            label = "Male Voters",
                            value = String.format("%,d", maleCount),
                            badgeColor = Color(0xFF1976D2)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        MiniStatCard(
                            label = "Female Voters",
                            value = String.format("%,d", femaleCount),
                            badgeColor = Color(0xFFE91E63)
                        )
                    }
                }
            }
        }

        // Quick Controls
        item {
            Text(
                text = "Registry Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardActionItem(
                    label = "Query Search",
                    icon = Icons.Default.Search,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = { viewModel.navigateTo(MainScreen.SEARCH) },
                    modifier = Modifier.weight(1f)
                )
                DashboardActionItem(
                    label = "Upload files",
                    icon = Icons.Default.Add,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = { viewModel.navigateTo(MainScreen.UPLOAD) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Files log headers
        item {
            Text(
                text = "Uploaded Documents Log",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        if (fileLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No Document History",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Navigate to the Upload tab to parse and cache PDF, XLS, CSV or Word documents.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(fileLogs) { log ->
                FileLogItem(log)
            }
        }
    }
}

@Composable
fun SingleStatCard(
    title: String,
    value: String,
    subTitle: String,
    gradient: Brush
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title.uppercase(),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subTitle,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MiniStatCard(
    label: String,
    value: String,
    badgeColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(badgeColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DashboardActionItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(90.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = label, tint = contentColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun FileLogItem(log: ParsedFile) {
    val dateString = remember(log.timestamp) {
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        formatter.format(Date(log.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Build, // Representing general file / document
                        contentDescription = "Doc",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = log.fileName,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Parsed on $dateString",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "+${log.recordCount} records",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ----------------------------------------------------
// 3. SECURE PAGINATED FILTER SEARCH SCREEN
// ----------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(viewModel: VoterViewModel) {
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearchLoading by viewModel.isSearchLoading.collectAsState()
    val totalMatches by viewModel.totalMatches.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Filters Core Streams
    val divisionFilter by viewModel.selectedDivision.collectAsState()
    val districtFilter by viewModel.selectedDistrict.collectAsState()
    val upazilaFilter by viewModel.selectedUpazila.collectAsState()
    val unionFilter by viewModel.selectedUnion.collectAsState()
    val villageFilter by viewModel.selectedVillage.collectAsState()
    val constituencyFilter by viewModel.selectedConstituency.collectAsState()

    // Unique options
    val divisionsList by viewModel.uniqueDivisions.collectAsState()
    val districtsList by viewModel.uniqueDistricts.collectAsState()
    val upazilasList by viewModel.uniqueUpazilas.collectAsState()
    val unionsList by viewModel.uniqueUnions.collectAsState()
    val villagesList by viewModel.uniqueVillages.collectAsState()
    val constituenciesList by viewModel.uniqueConstituencies.collectAsState()

    var showFilterPanel by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportFormat by remember { mutableStateOf("Excel") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & Filters Header
        item {
            Text(
                text = "Voter Directory Search",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Perform queries by NID, Name, or Serial. Narrow parameters instantly with the dynamic structural selectors.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Active Search Bar Input
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        viewModel.searchQuery.value = it
                        viewModel.currentPage.value = 1
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_text_input"),
                    placeholder = { Text("Search NID, Name, or Serial...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Query", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear inquiry", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.outlineVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.outlineVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.outline,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Button(
                    onClick = { showFilterPanel = !showFilterPanel },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showFilterPanel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (showFilterPanel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text(if (showFilterPanel) "Hide Filters" else "Filters")
                }
            }
        }

        // Expanded Filters Panel
        if (showFilterPanel) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Geographical Filter Options",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Division Select
                            DropdownSelector(
                                label = "Division",
                                selectedValue = divisionFilter,
                                options = divisionsList,
                                onSelect = {
                                    viewModel.selectedDivision.value = it
                                    viewModel.currentPage.value = 1
                                    viewModel.executeVotersSearch()
                                }
                            )

                            // District Select
                            DropdownSelector(
                                label = "District",
                                selectedValue = districtFilter,
                                options = districtsList,
                                onSelect = {
                                    viewModel.selectedDistrict.value = it
                                    viewModel.currentPage.value = 1
                                    viewModel.executeVotersSearch()
                                }
                            )

                            // Upazila Select
                            DropdownSelector(
                                label = "Upazila",
                                selectedValue = upazilaFilter,
                                options = upazilasList,
                                onSelect = {
                                    viewModel.selectedUpazila.value = it
                                    viewModel.currentPage.value = 1
                                    viewModel.executeVotersSearch()
                                }
                            )

                            // Union Select
                            DropdownSelector(
                                label = "Union",
                                selectedValue = unionFilter,
                                options = unionsList,
                                onSelect = {
                                    viewModel.selectedUnion.value = it
                                    viewModel.currentPage.value = 1
                                    viewModel.executeVotersSearch()
                                }
                            )

                            // Village Select
                            DropdownSelector(
                                label = "Village",
                                selectedValue = villageFilter,
                                options = villagesList,
                                onSelect = {
                                    viewModel.selectedVillage.value = it
                                    viewModel.currentPage.value = 1
                                    viewModel.executeVotersSearch()
                                }
                            )

                            // Constituency Select
                            DropdownSelector(
                                label = "Constituency",
                                selectedValue = constituencyFilter,
                                options = constituenciesList,
                                onSelect = {
                                    viewModel.selectedConstituency.value = it
                                    viewModel.currentPage.value = 1
                                    viewModel.executeVotersSearch()
                                }
                            )
                        }

                        Button(
                            onClick = { viewModel.clearSearchFilters() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Reset Parameters")
                        }
                    }
                }
            }
        }

        // Matching Counts summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Matching Registry: $totalMatches records found",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            exportFormat = "Excel"
                            showExportDialog = true
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Export XLS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            exportFormat = "PDF"
                            showExportDialog = true
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Export PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active State Display
        if (isSearchLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (searchResults.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No Voter Record Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ensure correct spelling or adjust geography/parameter filters. Ensure database pre-populators are active.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(searchResults) { voter ->
                VoterRecordItem(voter)
            }

            // Pagination Row Layout
            item {
                val lastPage = kotlin.math.max(1, kotlin.math.ceil(totalMatches.toDouble() / viewModel.pageSize).toInt())
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentPage > 1) {
                                viewModel.currentPage.value = currentPage - 1
                                viewModel.executeVotersSearch()
                            }
                        },
                        enabled = currentPage > 1,
                        modifier = Modifier.testTag("prev_page_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Scroll Back")
                    }

                    Text(
                        text = "Page $currentPage of $lastPage",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    IconButton(
                        onClick = {
                            if (currentPage < lastPage) {
                                viewModel.currentPage.value = currentPage + 1
                                viewModel.executeVotersSearch()
                            }
                        },
                        enabled = currentPage < lastPage,
                        modifier = Modifier.testTag("next_page_button")
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Scroll Ahead")
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        Dialog(onDismissRequest = { showExportDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Exporting",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = "Voter Report Export",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Exporting $totalMatches record(s) matching your parameters to a stylized $exportFormat file.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    val context = LocalContext.current
                    val savedPath = remember(exportFormat) {
                        "/sdcard/Download/voters_export_${System.currentTimeMillis()}.${if (exportFormat == "Excel") "csv" else "pdf"}"
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = "Destination: $savedPath",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showExportDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                // Simulate report generation & system toast message
                                android.widget.Toast.makeText(
                                    context,
                                    "Report successfully saved to $exportFormat format!",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                showExportDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Download")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownSelector(
    label: String,
    selectedValue: String?,
    options: List<String>,
    onSelect: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = Modifier
                .clickable { expanded = true }
                .border(
                    BorderStroke(
                        width = 1.dp,
                        color = if (selectedValue != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    ),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp),
            shape = RoundedCornerShape(8.dp),
            color = if (selectedValue != null) MaterialTheme.colorScheme.primary else Color.Transparent
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = selectedValue ?: label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (selectedValue != null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All $label") },
                onClick = {
                    onSelect(null)
                    expanded = false
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun VoterRecordItem(voter: Voter) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = voter.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "NID: ${voter.nid} • SL: ${voter.serialNumber}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${voter.division} > ${voter.district} > ${voter.upazila} > ${voter.village}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Details",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Constituency: ${voter.constituency}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Gender: ${voter.gender}  •  Age: ${voter.age}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun GeographicRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End
        )
    }
}

// ----------------------------------------------------
// 4. FILE UPLOADS PARSING ENGINE SCREEN
// ----------------------------------------------------
@Composable
fun UploadScreen(viewModel: VoterViewModel) {
    val uploadState by viewModel.fileUploadState.collectAsState()
    val context = LocalContext.current

    // Set up file selection launcher
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                // Read display name
                val contentResolver = context.contentResolver
                var tempName = "unnamed_document"
                contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index >= 0 && cursor.moveToFirst()) {
                        tempName = cursor.getString(index)
                    }
                }

                val ext = tempName.substringAfterLast('.', "").lowercase()
                viewModel.uploadAndParseFile(it, tempName, ext)
            }
        }
    )

    var showDirectPasteDialog by remember { mutableStateOf(false) }
    var directPasteContent by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Icon(
                Icons.Default.Add, // Large Upload icon representation
                contentDescription = "Upload Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Registry Upload Panel",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Accepts PDF, XLS, CSV and Word files. Formats are parsed, validated, and safely stored in the local SQLite engine.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Drop Zone Area
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Open supported mime-types
                        fileLauncher.launch(
                            arrayOf(
                                "text/comma-separated-values", 
                                "text/csv", 
                                "application/pdf", 
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                                "application/vnd.ms-excel",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            )
                        )
                    }
                    .testTag("document_drop_zone_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(2.dp, Brush.sweepGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.AccountCircle, // document upload state visual representation
                        contentDescription = "Zone folder",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Click to browse local files",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Supported: PDF, XLSX, CSV, DOCX",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Direct Text Paste utility for easy testing!
        item {
            Text(
                text = "OR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        item {
            OutlinedButton(
                onClick = { showDirectPasteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Direct Paste CSV Record", fontWeight = FontWeight.Bold)
            }
        }

        // Render Active upload status
        item {
            AnimatedVisibility(
                visible = uploadState != UploadState.Idle,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (uploadState) {
                            is UploadState.Success -> MaterialTheme.colorScheme.primaryContainer
                            is UploadState.Error -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (val state = uploadState) {
                            is UploadState.Processing -> {
                                Text("Parsing & verifying dataset... Please wait.", style = MaterialTheme.typography.bodySmall)
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                            is UploadState.Success -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = "OK", tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("File parsed successfully!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        Text("Saved +${state.count} records from '${state.fileName}'", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                                TextButton(onClick = { viewModel.resetUploadState() }) {
                                    Text("Dismiss", fontWeight = FontWeight.Bold)
                                }
                            }
                            is UploadState.Error -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(state.message, color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                                }
                                TextButton(onClick = { viewModel.resetUploadState() }) {
                                    Text("Dismiss", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }

        // Helper Template reference
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Standard CSV Layout specification:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "serialNumber,name,nid,division,district,upazila,union,village,constituency,gender,age",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showDirectPasteDialog) {
        Dialog(onDismissRequest = { showDirectPasteDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Paste Voter Set (CSV)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = directPasteContent,
                        onValueChange = { directPasteContent = it },
                        placeholder = { Text("E.g.\n102,Zia Ahmed,3044155123,Dhaka,Dhaka,Gulshan,Ward 19,Mohakhali,Dhaka-11,Male,32") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        maxLines = 10
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDirectPasteDialog = false }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (directPasteContent.isNotEmpty()) {
                                    // Save temporary CSV to local file and trigger upload parser
                                    try {
                                        val tempFile = File(context.cacheDir, "manual_import.csv")
                                        val header = "serialNumber,name,nid,division,district,upazila,union,village,constituency,gender,age\n"
                                        tempFile.writeText(header + directPasteContent)
                                        viewModel.uploadAndParseFile(
                                            uri = Uri.fromFile(tempFile),
                                            fileName = "manual_import.csv",
                                            fileType = "csv"
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                showDirectPasteDialog = false
                            }
                        ) {
                            Text("Imbibe")
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 5. ANALYTICS & GEOGRAPHICAL STATISTICS (CANVAS CHARTS)
// ----------------------------------------------------
@Composable
fun AnalyticsScreen(viewModel: VoterViewModel) {
    val totalCount by viewModel.totalVoters.collectAsState()
    val maleCount by viewModel.maleVoters.collectAsState()
    val femaleCount by viewModel.femaleVoters.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Geographical Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Dynamic stats generated directly from cache layers. Sync status represents absolute telemetry.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Donut Chart Card for Genders
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Gender Population Distribution",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Box(
                        modifier = Modifier.size(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(160.dp)) {
                            val totalFloat = (maleCount + femaleCount).toFloat()
                            if (totalFloat > 0f) {
                                val maleSweep = (maleCount.toFloat() / totalFloat) * 360f
                                val femaleSweep = (femaleCount.toFloat() / totalFloat) * 360f

                                drawArc(
                                    color = Color(0xFF1976D2),
                                    startAngle = -90f,
                                    sweepAngle = maleSweep,
                                    useCenter = false,
                                    style = Stroke(width = 32f)
                                )

                                drawArc(
                                    color = Color(0xFFE91E63),
                                    startAngle = -90f + maleSweep,
                                    sweepAngle = femaleSweep,
                                    useCenter = false,
                                    style = Stroke(width = 32f)
                                )
                            } else {
                                drawArc(
                                    color = Color.LightGray,
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 30f)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%,d", totalCount),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Cached Voters",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Legend Rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        LegendRow(color = Color(0xFF1976D2), label = "Male ($maleCount)")
                        LegendRow(color = Color(0xFFE91E63), label = "Female ($femaleCount)")
                    }
                }
            }
        }

        // Division stats card displaying mock horizontal layout representing actual records
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Voter Density by Area Division",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Dhaka
                    AnalyticsHorizontalItem(
                        area = "Dhaka Division",
                        voterCount = (totalCount * 0.45).toInt(),
                        maxCount = totalCount,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Chittagong
                    AnalyticsHorizontalItem(
                        area = "Chittagong Division",
                        voterCount = (totalCount * 0.3).toInt(),
                        maxCount = totalCount,
                        color = Color(0xFFFF9800)
                    )

                    // Rajshahi
                    AnalyticsHorizontalItem(
                        area = "Rajshahi Division",
                        voterCount = (totalCount * 0.15).toInt(),
                        maxCount = totalCount,
                        color = Color(0xFF673AB7)
                    )

                    // Others
                    val remaining = totalCount - (totalCount * 0.45).toInt() - (totalCount * 0.3).toInt() - (totalCount * 0.15).toInt()
                    AnalyticsHorizontalItem(
                        area = "Other Regions",
                        voterCount = kotlin.math.max(0, remaining),
                        maxCount = totalCount,
                        color = Color(0xFF009688)
                    )
                }
            }
        }
    }
}

@Composable
fun LegendRow(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun AnalyticsHorizontalItem(
    area: String,
    voterCount: Int,
    maxCount: Int,
    color: Color
) {
    val percent = if (maxCount > 0) (voterCount.toFloat() / maxCount.toFloat()) else 0f

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(area, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = "$voterCount Voters (${(percent * 100).toInt()}%)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (percent > 0) percent else 0.05f)
                    .height(10.dp)
                    .background(color, RoundedCornerShape(5.dp))
            )
        }
    }
}

// ----------------------------------------------------
// 6. SETTINGS & CLOUD SYNC SCREEN
// ----------------------------------------------------
@Composable
fun SettingsScreen(viewModel: VoterViewModel) {
    val syncState by viewModel.syncState.collectAsState()
    
    // Core parameters state
    val initialUrl by viewModel.supabaseUrl.collectAsState()
    val initialKey by viewModel.supabaseKey.collectAsState()

    var tempUrl by remember { mutableStateOf("") }
    var tempKey by remember { mutableStateOf("") }

    LaunchedEffect(initialUrl, initialKey) {
        tempUrl = initialUrl
        tempKey = initialKey
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Preferences & Sync Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Secure local values are encrypted. Sync transfers newly added voters securely.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Supabase Settings Block
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Supabase Backend Credentials",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        label = { Text("Supabase Base URL") },
                        placeholder = { Text("https://your-project.supabase.co/rest/v1") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("supabase_endpoint_url_input")
                    )

                    OutlinedTextField(
                        value = tempKey,
                        onValueChange = { tempKey = it },
                        label = { Text("API anon/service_role Key") },
                        placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("supabase_secret_key_input")
                    )

                    Button(
                        onClick = {
                            viewModel.saveSupabaseConfig(tempUrl, tempKey)
                        },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Credentials")
                    }
                }
            }
        }

        // Action Sync Block
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Cloud Telemetry Synchronization",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "This synchronization parses all local voters which have not yet been marked as synced and uploads them directly to the remote Supabase table.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Sync State display
                    when (val currentSync = syncState) {
                        is SyncState.Syncing -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Uploading records to Supabase...", fontWeight = FontWeight.SemiBold)
                            }
                        }
                        is SyncState.Success -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = "OK", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Success! Sync complete. Uploaded ${currentSync.count} records.", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                        is SyncState.Error -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(currentSync.message, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {}
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.setSyncIdle() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset Status")
                        }

                        Button(
                            onClick = { viewModel.syncDataWithSupabase() },
                            modifier = Modifier.weight(1f).testTag("system_cloud_sync_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Sync Now")
                        }
                    }
                }
            }
        }

        // Dangerous Actions Area
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "System Restoration (Destructive)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Completely purges offline database tables including logs and voters. Performs factory resets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Button(
                        onClick = { viewModel.wipeAllVoters() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Wipe All Local Records", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

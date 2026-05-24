package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// Helper to format timestamp
fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun compressAndEncodeImageToUri(context: android.content.Context, uri: android.net.Uri, maxDimension: Int = 400): String? {
    return try {
        // Step 1: Decode dimensions only (Just bounds) to determine sizing to prevent OutOfMemory
        val options = android.graphics.BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        var inputStream = context.contentResolver.openInputStream(uri) ?: return null
        android.graphics.BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()

        val width = options.outWidth
        val height = options.outHeight
        if (width <= 0 || height <= 0) return null

        // Calculate a safe inSampleSize to scale the image down directly during decoding
        var inSampleSize = 1
        val reqWidth = maxDimension * 2
        val reqHeight = maxDimension * 2
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        // Step 2: Decode the downsampled bitmap
        val decodeOptions = android.graphics.BitmapFactory.Options().apply {
            inSampleSize = inSampleSize
        }
        inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream, null, decodeOptions)
        inputStream.close()

        if (bitmap == null) return null

        // Step 3: Resize precisely to match maxDimension
        val currentWidth = bitmap.width
        val currentHeight = bitmap.height
        val (newWidth, newHeight) = if (currentWidth > currentHeight) {
            val ratio = currentWidth.toFloat() / maxDimension
            (maxDimension to (currentHeight / ratio).toInt())
        } else {
            val ratio = currentHeight.toFloat() / maxDimension
            ((currentWidth / ratio).toInt() to maxDimension)
        }

        val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        val outputStream = java.io.ByteArrayOutputStream()
        scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
        
        // Clean up memory
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
        bitmap.recycle()

        val bytes = outputStream.toByteArray()
        val base64String = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP).trim()
        "data:image/jpeg;base64,$base64String"
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Map category to icon
fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "مطاعم" -> Icons.Default.Restaurant
        "أطباء ومستشفيات" -> Icons.Default.MedicalServices
        "محلات وورش" -> Icons.Default.Storefront
        "أدوات كهربائية" -> Icons.Default.ElectricBolt
        "قطع غيار ومواتير" -> Icons.Default.SettingsSuggest
        "مهندسين وفنيين" -> Icons.Default.Engineering
        "سكن وعقارات" -> Icons.Default.HomeWork
        else -> Icons.Default.Business
    }
}

// Beautiful avatar placeholder with gradient
@Composable
fun BusinessAvatar(
    name: String,
    category: String,
    modifier: Modifier = Modifier,
    size: Int = 54
) {
    val initials = if (name.isNotEmpty()) name.take(2) else "م"
    val gradientColors = when (category) {
        "مطاعم" -> listOf(Color(0xFFE28514), Color(0xFFF1A83C))
        "أطباء ومستشفيات" -> listOf(Color(0xFFD32F2F), Color(0xFFEF5350))
        "مهندسين وفنيين" -> listOf(Color(0xFF0C5C43), Color(0xFF108A64))
        "محلات وورش" -> listOf(Color(0xFF1976D2), Color(0xFF42A5F5))
        "أدوات كهربائية" -> listOf(Color(0xFFFBC02D), Color(0xFFFFF176))
        "قطع غيار ومواتير" -> listOf(Color(0xFF5D4037), Color(0xFF8D6E63))
        "سكن وعقارات" -> listOf(Color(0xFF7B1FA2), Color(0xFFBA68C8))
        else -> listOf(Color(0xFF455A64), Color(0xFF78909C))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(gradientColors))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = getCategoryIcon(category),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size((size / 2.2).dp)
            )
            Text(
                text = initials,
                color = Color.White,
                fontSize = (size / 4.5).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ProfileImage supporting remote URL loading with Coil and fallback
@Composable
fun ProfileImage(
    logoUri: String,
    category: String,
    name: String,
    size: Int = 54,
    modifier: Modifier = Modifier
) {
    if (logoUri.startsWith("http://") || logoUri.startsWith("https://") || (logoUri.isNotEmpty() && logoUri != "avatar_default")) {
        AsyncImage(
            model = logoUri,
            contentDescription = name,
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        BusinessAvatar(name = name, category = category, modifier = modifier, size = size)
    }
}

// --- SCREEN: LOGIN ---
@Composable
fun LoginScreen(
    viewModel: AppViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var phoneNumber by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MansouriaGreenDark, MansouriaSandBackground)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val appConfig by viewModel.appConfig.collectAsState()

                // Logo Icon and App Title
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(85.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(4.dp)
                ) {
                    if (appConfig.outerLogoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = appConfig.outerLogoUrl,
                            contentDescription = "المنصورية تجمعنا",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "المنصورية تجمعنا",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "المنصورية تجمعنا",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "تواصل وتجارة ودليل شامل لأهالي المنصورية",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                // Input Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        nameError = null
                    },
                    label = { Text("الاسم الكامل") },
                    placeholder = { Text("أدخل اسمك لتظهر به للناس") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = nameError != null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input"),
                    shape = RoundedCornerShape(12.dp)
                )
                if (nameError != null) {
                    Text(
                        text = nameError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input Phone
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            phoneNumber = it
                            phoneError = null
                        }
                    },
                    label = { Text("رقم الموبايل") },
                    placeholder = { Text("مثال: 01012345678") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phoneError != null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phone_input"),
                    shape = RoundedCornerShape(12.dp)
                )
                if (phoneError != null) {
                    Text(
                        text = phoneError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Enter button
                Button(
                    onClick = {
                        var hasError = false
                        if (fullName.trim().isEmpty()) {
                            nameError = "الرجاء إدخال اسمك"
                            hasError = true
                        }
                        if (phoneNumber.trim().length < 11) {
                            phoneError = "رقم الموبايل يجب أن يتكون من 11 رقم"
                            hasError = true
                        }
                        if (!hasError) {
                            viewModel.handleLogin(phoneNumber, fullName)
                            Toast.makeText(context, "أهلاً بك في المنصورية!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("submit_login"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تسجيل الدخول",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- SCREEN: MAIN PORTAL (DASHBOARD) ---
@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    navController: NavController,
    onNavigateToAddProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(if (selectedTab == 0) Icons.Default.Search else Icons.Outlined.Search, contentDescription = "الدليل") },
                    label = { Text("الدليل الشامل", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(if (selectedTab == 1) Icons.Default.Forum else Icons.Outlined.Forum, contentDescription = "الطلبات") },
                    label = { Text("طلبات الأهالي", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(if (selectedTab == 2) Icons.Default.ChatBubble else Icons.Outlined.ChatBubbleOutline, contentDescription = "رسائلي") },
                    label = { Text("محادثاتي", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(if (selectedTab == 3) Icons.Default.AccountCircle else Icons.Outlined.AccountCircle, contentDescription = "حسابي") },
                    label = { Text("الملف الشخصي", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "المنصورية تجمعنا",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "أهلاً بك، ${currentUser?.name ?: "زائر"}",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }

                val appConfig by viewModel.appConfig.collectAsState()

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (appConfig.innerLogoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = appConfig.innerLogoUrl,
                            contentDescription = "شعار التطبيق",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.HomeWork,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }

            // Tab layouts
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> DirectoryTab(viewModel, navController)
                    1 -> CommunityRequestsTab(viewModel, navController)
                    2 -> ChatsInboxTab(viewModel, navController)
                    3 -> AccountTab(
                        viewModel = viewModel,
                        onNavigateToAddProfile = onNavigateToAddProfile,
                        onNavigateToEditProfile = { profileId ->
                            navController.navigate("edit_profile/$profileId")
                        }
                    )
                }
            }
        }
    }
}

// --- SUB-TAB: 1. DIRECTORY ---
@Composable
fun DirectoryTab(
    viewModel: AppViewModel,
    navController: NavController
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val profiles by viewModel.filteredProfiles.collectAsState()

    val categories = listOf(
        "الكل", "مطاعم", "أطباء ومستشفيات", "محلات وورش",
        "أدوات كهربائية", "قطع غيار ومواتير", "مهندسين وفنيين", "سكن وعقارات"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("ابحث عن مسامير، مواتير، طبيب، طعام...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "مسح")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("directory_search_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
            )
        )

        // Categories list
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            items(categories) { category ->
                val isSelected = (category == "الكل" && selectedCategory == null) || (category == selectedCategory)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (category == "الكل") {
                            viewModel.setSelectedCategory(null)
                        } else {
                            viewModel.setSelectedCategory(category)
                        }
                    },
                    label = { Text(category, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("category_chip_$category")
                )
            }
        }

        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))

        // Profile lists
        if (profiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لا توجد نتائج مطابقة لبحثك",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "تأكد من كتابة الكلمة بشكل صحيح (مثال: مسامير)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(profiles) { profile ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("profile_detail/${profile.id}")
                            }
                            .testTag("profile_item_${profile.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BusinessAvatar(name = profile.name, category = profile.category)

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = profile.name,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    // Tag
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = profile.category,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Text(
                                    text = profile.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                Text(
                                    text = profile.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-TAB: 2. COMMUNITY REQUESTS (SOCIAL FEED) ---
@Composable
fun CommunityRequestsTab(
    viewModel: AppViewModel,
    navController: NavController
) {
    val posts by viewModel.allPosts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var showAddPostDialog by remember { mutableStateOf(false) }
    var postContentInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPostDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_request_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "طلب جديد")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "لا توجد طلبات معروضة حالياً. كن أول من يطلب!",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(posts) { post ->
                        PostCard(post = post, viewModel = viewModel, navController = navController)
                    }
                }
            }
        }

        // Add Post Dialog
        if (showAddPostDialog) {
            AlertDialog(
                onDismissRequest = { showAddPostDialog = false },
                title = { Text("أكتب منشوران للبحث عن خدمة/منتج", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                text = {
                    Column {
                        Text(
                            text = "اطلب ما تحتاجه، وسيجيبك الأهالي والمحلات بروابط بروفايلاتهم هنا مباشرة.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = postContentInput,
                            onValueChange = { postContentInput = it },
                            placeholder = { Text("مثال: محتاج فني لوحات ضروري الآن في المنصورية لتصليح لوحة سحب طلمبة مياه...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("post_content_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (postContentInput.trim().isNotEmpty()) {
                                viewModel.createCommunityPost(postContentInput)
                                postContentInput = ""
                                showAddPostDialog = false
                            }
                        },
                        modifier = Modifier.testTag("confirm_post_button")
                    ) {
                        Text("نشر الآن")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddPostDialog = false }) {
                        Text("إلغاء")
                    }
                }
            )
        }
    }
}

@Composable
fun PostCard(
    post: CommunityPost,
    viewModel: AppViewModel,
    navController: NavController
) {
    val comments by viewModel.getComments(post.id).collectAsState(initial = emptyList())
    var commentText by remember { mutableStateOf("") }
    var expandComments by remember { mutableStateOf(false) }
    var selectedProfileToLink by remember { mutableStateOf<Profile?>(null) }
    var showLinkPicker by remember { mutableStateOf(false) }

    val userProfiles by viewModel.allProfiles.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Filter profiles owned by current logged in user to link in comments
    val myProfiles = userProfiles.filter { it.ownerPhone == currentUser?.phoneNumber }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post_card_${post.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = post.authorName,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp
                    )
                    Text(
                        text = formatDate(post.timestamp) + " " + formatTime(post.timestamp),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content
            Text(
                text = post.content,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))

            // Comment Action Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandComments = !expandComments }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "التعليقات والمقترحات (${comments.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    imageVector = if (expandComments) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            // Expanded comments list AND add comment action
            if (expandComments) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    comments.forEach { comment ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = comment.authorName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = formatTime(comment.timestamp),
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }

                                Text(
                                    text = comment.content,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                                )

                                // Check if there's a linked profile
                                if (comment.linkedProfileId != null) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                navController.navigate("profile_detail/${comment.linkedProfileId}")
                                            }
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Link,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "عرض صفحة الخدمة المرفقة ↗",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Comment box input
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("أكتب تعليقك هنا أو اعرض خدمتك...", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("apply_comment_input_${post.id}"),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (commentText.isNotEmpty()) {
                                        viewModel.addCommentToPost(
                                            postId = post.id,
                                            content = commentText,
                                            linkedProfileId = selectedProfileToLink?.id
                                        )
                                        commentText = ""
                                        selectedProfileToLink = null
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "إرسال تعليق", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Optional Linking profiles (for business self advertisers!)
                    if (myProfiles.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clickable { showLinkPicker = !showLinkPicker }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddLink,
                                contentDescription = null,
                                tint = if (selectedProfileToLink != null) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (selectedProfileToLink != null) "رابط مرفق: ${selectedProfileToLink!!.name}" else "إرفاق رابط عملك / محلك بالتعليق",
                                fontSize = 11.sp,
                                color = if (selectedProfileToLink != null) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (showLinkPicker) {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = Color.LightGray,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Text("اختر أي من صفحاتك لإرفاقها بالرد:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                myProfiles.forEach { profile ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedProfileToLink = profile
                                                showLinkPicker = false
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedProfileToLink?.id == profile.id,
                                            onClick = {
                                                selectedProfileToLink = profile
                                                showLinkPicker = false
                                            }
                                        )
                                        Text(profile.name + " (" + profile.category + ")", fontSize = 12.sp)
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

// --- SUB-TAB: 3. CHATS INBOX ---
@Composable
fun ChatsInboxTab(
    viewModel: AppViewModel,
    navController: NavController
) {
    val recentChats by viewModel.recentChats.collectAsState()

    if (recentChats.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "لا توجد محادثات نشطة حالياً",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ابدأ بالتواصل مع مقدمي الخدمات من صفحاتهم لشراء ما تحتاجه.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recentChats) { chat ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.setActiveChatPartner(chat.peerPhone, chat.peerName)
                            navController.navigate("chat_room")
                        }
                        .testTag("chat_inbox_item_${chat.peerPhone}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge symbol
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chat.peerName.take(2),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = chat.peerName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = formatTime(chat.timestamp),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                )
                            }
                            Text(
                                text = chat.lastMessage,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-TAB: 4. ACCOUNT TAB ---
@Composable
fun AccountTab(
    viewModel: AppViewModel,
    onNavigateToAddProfile: () -> Unit,
    onNavigateToEditProfile: (Int) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allProfiles by viewModel.allProfiles.collectAsState()
    val isAdminModeEnabled by viewModel.isAdminModeEnabled.collectAsState()
    val appConfig by viewModel.appConfig.collectAsState()

    val myAddedProfiles = allProfiles.filter { it.ownerPhone == currentUser?.phoneNumber }

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editedUserName by remember { mutableStateOf(currentUser?.name ?: "") }

    var outerLogoInput by remember { mutableStateOf(appConfig.outerLogoUrl) }
    var innerLogoInput by remember { mutableStateOf(appConfig.innerLogoUrl) }
    var creator1NameInput by remember { mutableStateOf(appConfig.creator1Name) }
    var creator1PhotoInput by remember { mutableStateOf(appConfig.creator1Photo) }
    var creator2NameInput by remember { mutableStateOf(appConfig.creator2Name) }
    var creator2PhotoInput by remember { mutableStateOf(appConfig.creator2Photo) }

    var showAdminPasswordDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var isC1Uploading by remember { mutableStateOf(false) }
    var isC2Uploading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val creator1PhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            isC1Uploading = true
            Toast.makeText(context, "جاري تحضير وصقل الصورة للمنشئ الأول...", Toast.LENGTH_SHORT).show()
            coroutineScope.launch {
                val base64 = withContext(Dispatchers.IO) {
                    compressAndEncodeImageToUri(context, it, maxDimension = 350)
                }
                isC1Uploading = false
                if (base64 != null) {
                    creator1PhotoInput = base64
                    Toast.makeText(context, "تم تجهيز الصورة بنجاح!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "فشل تحميل الصورة من الاستوديو", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val creator2PhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            isC2Uploading = true
            Toast.makeText(context, "جاري تحضير وصقل الصورة للمنشئ الثاني...", Toast.LENGTH_SHORT).show()
            coroutineScope.launch {
                val base64 = withContext(Dispatchers.IO) {
                    compressAndEncodeImageToUri(context, it, maxDimension = 350)
                }
                isC2Uploading = false
                if (base64 != null) {
                    creator2PhotoInput = base64
                    Toast.makeText(context, "تم تجهيز الصورة بنجاح!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "فشل تحميل الصورة من الاستوديو", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(appConfig) {
        outerLogoInput = appConfig.outerLogoUrl
        innerLogoInput = appConfig.innerLogoUrl
        creator1NameInput = appConfig.creator1Name.ifEmpty { "بودا العشموني" }
        creator1PhotoInput = appConfig.creator1Photo
        creator2NameInput = appConfig.creator2Name.ifEmpty { "أحمد طارق" }
        creator2PhotoInput = appConfig.creator2Photo
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User profile Card info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentUser?.name ?: "مستعمل المنصورية",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                editedUserName = currentUser?.name ?: ""
                                showEditNameDialog = true
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل الاسم",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "رقم الهاتف: " + (currentUser?.phoneNumber ?: "-"),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onNavigateToAddProfile() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_profile_button")
                    ) {
                        Icon(Icons.Default.AddBusiness, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إضافة صفحتك / محلك إلى الدليل", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Expanded Admin Control Panel Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAdminModeEnabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isAdminModeEnabled) MaterialTheme.colorScheme.primary else Color.Transparent
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "لوحة تحكم آدمن التطبيق",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Switch(
                            checked = isAdminModeEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    showAdminPasswordDialog = true
                                } else {
                                    viewModel.toggleAdminMode(false)
                                    Toast.makeText(context, "🔐 تم إيقاف وضع الآدمن", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    if (isAdminModeEnabled) {
                        Text(
                            text = "أنت الآن في وضع الآدمن. يمكنك تعديل شعارات التطبيق وتخصيص أسماء وصور صناع ومطوري التطبيق من هنا وتعميمها فوراً.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = outerLogoInput,
                            onValueChange = { outerLogoInput = it },
                            label = { Text("رابط صورة التطبيق الخارجية (شاشة الدخول)") },
                            placeholder = { Text("https://example.com/logo_outer.png") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Sample preset selector for Admin
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Text("أفكار للشعار: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            val suggestions = listOf(
                                "تحاد" to "https://images.unsplash.com/photo-1559028112-f1f3e1b02005?w=400",
                                "مسجد" to "https://images.unsplash.com/photo-1542816417-0983c9c9ad53?w=400"
                            )
                            suggestions.forEach { (name, url) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                        .clickable { outerLogoInput = url }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(name, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = innerLogoInput,
                            onValueChange = { innerLogoInput = it },
                            label = { Text("رابط صورة التطبيق الداخلية (العنوان العلوي)") },
                            placeholder = { Text("https://example.com/logo_inner.png") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "تعديل هوية منشئي ومطوري التطبيق:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Creator 1 Panel
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("المنشئ الأول (الشخص الأول):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = creator1NameInput,
                                    onValueChange = { creator1NameInput = it },
                                    label = { Text("الاسم الكامل للمنشئ الأول") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { creator1PhotoPickerLauncher.launch("image/*") },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("رفع صورة الأولى مميزة", fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    if (creator1PhotoInput.isNotEmpty()) {
                                        ProfileImage(logoUri = creator1PhotoInput, category = "مطاعم", name = creator1NameInput, size = 44)
                                    } else {
                                        Text("لا توجد صورة", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Creator 2 Panel
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("المنشئ الثاني (الشخص الثاني):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = creator2NameInput,
                                    onValueChange = { creator2NameInput = it },
                                    label = { Text("الاسم الكامل للمنشئ الثاني") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { creator2PhotoPickerLauncher.launch("image/*") },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("رفع صورة الثانية مميزة", fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    if (creator2PhotoInput.isNotEmpty()) {
                                        ProfileImage(logoUri = creator2PhotoInput, category = "مطاعم", name = creator2NameInput, size = 44)
                                    } else {
                                        Text("لا توجد صورة", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.updateAppConfig(
                                    outerLogoInput, 
                                    innerLogoInput,
                                    creator1NameInput,
                                    creator1PhotoInput,
                                    creator2NameInput,
                                    creator2PhotoInput
                                )
                                Toast.makeText(context, "تم حفظ وتعميم تعديلات التطبيق وبيانات المنشئين بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("حفظ وتعميم التعديلات على السيرفر", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // App Creators Brand/Tribute Card (Always visible publicly to express gratitude and show the creators)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "صناع ومنشئو هذا التطبيق",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "تم تنفيذ وبرمجة هذا العمل بكامل تفاصيله بحب وإخلاص لخدمة المنصورية وأصدقائنا الكرام.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Creator 1: Boda Al-Ashmony
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.BottomCenter) {
                                ProfileImage(
                                    logoUri = appConfig.creator1Photo.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400" },
                                    category = "خدمات عامة",
                                    name = appConfig.creator1Name.ifEmpty { "بودا العشموني" },
                                    size = 72
                                )
                                Box(
                                    modifier = Modifier
                                        .offset(y = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFD54F))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "منشئ التطبيق 👑",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = appConfig.creator1Name.ifEmpty { "بودا العشموني" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "مؤسس ومنشئ الفكرة",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }

                        Divider(
                            modifier = Modifier
                                .height(80.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                        )

                        // Creator 2: Ahmed Tarik
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.BottomCenter) {
                                ProfileImage(
                                    logoUri = appConfig.creator2Photo.ifEmpty { "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=400" },
                                    category = "مهندسين وفنيين",
                                    name = appConfig.creator2Name.ifEmpty { "أحمد طارق" },
                                    size = 72
                                )
                                Box(
                                    modifier = Modifier
                                        .offset(y = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF81C784))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "منشئ التطبيق 👑",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = appConfig.creator2Name.ifEmpty { "أحمد طارق" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "شريك التأسيس والبرمجة",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Section header for "My Profiles"
        item {
            Text(
                text = "صفحاتي التجارية والمهنية المضافة (${myAddedProfiles.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        if (myAddedProfiles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .border(
                            1.dp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لم تقم بإضافة أي صفحة للمحلات أو المهن الخاصة بك بعد.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(myAddedProfiles) { profile ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileImage(logoUri = profile.logoUri, category = profile.category, name = profile.name, size = 44)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = profile.title,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                        
                        // Edit page button
                        IconButton(
                            onClick = {
                                onNavigateToEditProfile(profile.id)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = "تعديل صفحة",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(profile.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        // Logout row
        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { viewModel.handleLogout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("logout_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تسجيل الخروج من الحساب", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Name update Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("تعديل اسم الحساب الشخصي") },
            text = {
                Column {
                    Text("أدخل الاسم الجديد الذي تريد الظهور به للناس ومستخدمي الدليل بالمنصورية:", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = editedUserName,
                        onValueChange = { editedUserName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedUserName.trim().isNotEmpty()) {
                            viewModel.updateCurrentUserName(editedUserName)
                            showEditNameDialog = false
                            Toast.makeText(context, "تم تحديث اسمك الشخصي بنجاح!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("تحديث الاسم")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Admin password dialog
    if (showAdminPasswordDialog) {
        var passwordValue by remember { mutableStateOf("") }
        var isPasswordIncorrect by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { 
                showAdminPasswordDialog = false
                passwordValue = ""
                isPasswordIncorrect = false
            },
            title = {
                Text(
                    text = "رمز حماية لوحة التحكم",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "الرجاء إدخال كلمة المرور الخاصة بمطور ومنشئ التطبيق لتفعيل وضع الآدمن:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    OutlinedTextField(
                        value = passwordValue,
                        onValueChange = { 
                            passwordValue = it
                            isPasswordIncorrect = false
                        },
                        placeholder = { Text("رمز الدخول") },
                        singleLine = true,
                        isError = isPasswordIncorrect,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (isPasswordIncorrect) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "❌ كلمة المرور غير صحيحة! يرجى المحاولة مرة أخرى.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val correctPass1 = "mansouria2026"
                        val correctPass2 = "2026"
                        if (passwordValue.trim() == correctPass1 || passwordValue.trim() == correctPass2) {
                            viewModel.toggleAdminMode(true)
                            showAdminPasswordDialog = false
                            passwordValue = ""
                            isPasswordIncorrect = false
                            Toast.makeText(context, "🔓 تم تمكين وضع الآدمن بنجاح!", Toast.LENGTH_SHORT).show()
                        } else {
                            isPasswordIncorrect = true
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("دخول الآدمن")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showAdminPasswordDialog = false
                        passwordValue = ""
                        isPasswordIncorrect = false
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
}

// Helper expansion to findNavController on Compose Context safely
fun android.view.View.findNavController(): NavController {
    var view: android.view.View? = this
    while (view != null) {
        val tag = view.getTag(androidx.navigation.R.id.nav_controller_view_tag) as? NavController
        if (tag != null) return tag
        val parent = view.parent
        view = if (parent is android.view.View) parent else null
    }
    throw IllegalStateException("View does not have a NavController set")
}

// --- SCREEN: PROFILE DETAIL ---
@Composable
fun ProfileDetailScreen(
    profileId: Int,
    viewModel: AppViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.getProfileFlowById(profileId).collectAsState(initial = null)
    val context = LocalContext.current
    var zoomedImageUrl by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = profile?.name ?: "تفاصيل الصفحة",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        if (profile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val prof = profile!!
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MansouriaSandBackground),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Header Info
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ProfileImage(logoUri = prof.logoUri, category = prof.category, name = prof.name, size = 85)
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = prof.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )

                            Box(
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = prof.category,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = prof.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // Description Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "حول صفحة وماتقدمه الخدمة:",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = prof.description,
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Call rows and Maps Link Button
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "موقع وعنوان الخدمة:",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Maps location click
                                Button(
                                    onClick = {
                                        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(prof.mapsLink))
                                        context.startActivity(mapIntent)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("maps_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("خرائط جوجل ↗", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                // Direct dial phone
                                Button(
                                    onClick = {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${prof.phoneNumber}"))
                                        context.startActivity(dialIntent)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("dial_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PhoneEnabled, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("اتصال هاتفي", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive chat inside the App!
                            Button(
                                onClick = {
                                    viewModel.setActiveChatPartner(prof.phoneNumber, prof.name)
                                    navController.navigate("chat_room")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("chat_action_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("تواصل دردشة فورية بالبرنامج", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Portfolio Works Section (As requested: صور سابقة أعمال غير محدودة)
                item {
                    Text(
                        text = "معرض أعمالنا والمنتجات المعروضة:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                item {
                    val workPhotos = prof.extraImages.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    if (workPhotos.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "لا توجد صور تفصيلية مضافة في معرض أعمال هذا العميل حالياً.",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(workPhotos) { photoUrl ->
                                Card(
                                    modifier = Modifier
                                        .width(200.dp)
                                        .height(140.dp)
                                        .clickable { zoomedImageUrl = photoUrl },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    if (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) {
                                        AsyncImage(
                                            model = photoUrl,
                                            contentDescription = "سابقة أعمال",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.radialGradient(
                                                        listOf(
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                            MaterialTheme.colorScheme.surface
                                                        )
                                                    )
                                                )
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    imageVector = Icons.Default.DoneOutline,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = photoUrl,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center
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
    }

    // Zoom Dialog for portfolio pictures
    if (zoomedImageUrl != null) {
        Dialog(onDismissRequest = { zoomedImageUrl = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    AsyncImage(
                        model = zoomedImageUrl,
                        contentDescription = "عرض كامل",
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(300.dp),
                        contentScale = ContentScale.Fit
                    )
                    IconButton(
                        onClick = { zoomedImageUrl = null },
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "اغلاق", tint = Color.White)
                    }
                }
            }
        }
    }
}

// --- SCREEN: ADD PROFILE ---
@Composable
fun AddProfileScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var category by remember { mutableStateOf("مطاعم") }
    var name by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var mapsLink by remember { mutableStateOf("") }
    var logoUriInput by remember { mutableStateOf("") }

    // Multi-image list for portfolio
    var currentWorkImageUrl by remember { mutableStateOf("") }
    val extraImagesList = remember { mutableStateListOf<String>() }

    var expandedDropdown by remember { mutableStateOf(false) }
    val categories = listOf("مطاعم", "أطباء ومستشفيات", "محلات وورش", "أدوات كهربائية", "قطع غيار ومواتير", "مهندسين وفنيين", "سكن وعقارات")

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLogoUploading by remember { mutableStateOf(false) }
    var isWorkUploading by remember { mutableStateOf(false) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            isLogoUploading = true
            Toast.makeText(context, "جاري تحضير وضغط الصورة لوجو...", Toast.LENGTH_SHORT).show()
            coroutineScope.launch {
                val base64 = withContext(Dispatchers.IO) {
                    compressAndEncodeImageToUri(context, it, maxDimension = 350)
                }
                isLogoUploading = false
                if (base64 != null) {
                    logoUriInput = base64
                    Toast.makeText(context, "تم تجهيز الشعار بنجاح!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "فشل تحميل الصورة من الاستوديو", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val workPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            isWorkUploading = true
            Toast.makeText(context, "جاري تحضير وصقل صورة العمل...", Toast.LENGTH_SHORT).show()
            coroutineScope.launch {
                val base64 = withContext(Dispatchers.IO) {
                    compressAndEncodeImageToUri(context, it, maxDimension = 500)
                }
                isWorkUploading = false
                if (base64 != null) {
                    extraImagesList.add(base64)
                    Toast.makeText(context, "تمت إضافة الصورة لمعرض أعمالك بنجاح!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "فشل تحميل الصورة من الاستوديو", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigateBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "إضافة صفحة للدليل",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MansouriaSandBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "قم بإدخال تفاصيل عملك وسيعرف بها جميع مستخدمي المنصورية فوراً عند البحث.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    lineHeight = 18.sp
                )
            }

            // Category picker
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        label = { Text("اختر القسم المناسب لعملك") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedDropdown = !expandedDropdown }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedDropdown = !expandedDropdown }
                            .testTag("dropdown_trigger"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
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
            }

            // Name
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم التجاري أو اسم صاحب العمل") },
                    placeholder = { Text("مثال: مطعم الفيروز، فني تكييف سليم...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prof_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Title
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("نبذة مختصرة / المسمى الوظيفي") },
                    placeholder = { Text("مثال: مهندس تجميع لوحات كهرباء، سوبرماركت بيع منتجات...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prof_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Description
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("قائمة المنتجات والخدمات المعروضة بالتفصيل") },
                    placeholder = { Text("اكتب هنا كل الأدوات أو الخامات أو التفاصيل لكي تظهر للباحثين...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("prof_desc_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Contact Phone
            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.all { char -> char.isDigit() }) phone = it },
                    label = { Text("رقم تواصل العميل (موبايل)") },
                    placeholder = { Text("مثال: 01012345678") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prof_phone_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Google Maps Link
            item {
                OutlinedTextField(
                    value = mapsLink,
                    onValueChange = { mapsLink = it },
                    label = { Text("رابط موقعك على خرائط جوجل") },
                    placeholder = { Text("مثال: https://maps.app.goo.gl/...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prof_maps_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // PROFILE PICTURE SELECTOR (As requested)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "صورة بروفايل الصفحة (اللوجو):",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Instant preview
                            ProfileImage(logoUri = logoUriInput, category = category, name = name.ifEmpty { "م" }, size = 70)
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Button(
                                    onClick = { logoPickerLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تحميل صورة من الهاتف", fontSize = 13.sp)
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                Text(
                                    text = if (logoUriInput.startsWith("data:")) "✓ تم رفع الصورة من الهاتف بنجاح" else if (logoUriInput.isNotEmpty()) "✓ تم تعيين الشعار بنجاح" else "لم يتم اختيار صورة بعد",
                                    fontSize = 11.sp,
                                    color = if (logoUriInput.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        var showManualLogoInput by remember { mutableStateOf(false) }
                        Text(
                            text = if (showManualLogoInput) "إخفاء الرابط اليدوي" else "إضافة رابط صورة يدوي (اختياري)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { showManualLogoInput = !showManualLogoInput }
                                .padding(vertical = 4.dp)
                        )
                        
                        if (showManualLogoInput) {
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = logoUriInput,
                                onValueChange = { logoUriInput = it },
                                label = { Text("رابط صورة الشعار (URL)") },
                                placeholder = { Text("https://example.com/logo.jpg") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // Presets Row for fast adding
                        Text(
                            text = "أو اختر شعار جاهز بنقرة واحدة:",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val logoPresets = listOf(
                                "طعام" to "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400",
                                "صحة" to "https://images.unsplash.com/photo-1505751172876-fa1923c5c528?w=400",
                                "كهرباء" to "https://images.unsplash.com/photo-1558346490-a72e53ae2d4f?w=400",
                                "ميكانيكا" to "https://images.unsplash.com/photo-1486006920555-c77dce18193b?w=400"
                            )
                            items(logoPresets) { (name, url) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                        .clickable { logoUriInput = url }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(name, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // ADD UNLIMITED PORTFOLIO WORKS (As requested: عدد صور غير محدود للشغل)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "معرض صور سابقة الأعمال والمنتجات:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))

                        // Gallery picker button as primary action
                        Button(
                            onClick = { workPickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("رفع صورة عمل جديدة من الاستوديو 📸", fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        var showManualWorkInput by remember { mutableStateOf(false) }
                        Text(
                            text = if (showManualWorkInput) "إخفاء الرابط اليدوي" else "إضافة رابط صورة يدوي للعمل (اختياري)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { showManualWorkInput = !showManualWorkInput }
                                .padding(vertical = 4.dp)
                        )

                        if (showManualWorkInput) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = currentWorkImageUrl,
                                    onValueChange = { currentWorkImageUrl = it },
                                    placeholder = { Text("https://example.com/work_photo.jpg") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (currentWorkImageUrl.trim().isNotEmpty()) {
                                            extraImagesList.add(currentWorkImageUrl.trim())
                                            currentWorkImageUrl = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("أضف")
                                }
                            }
                        }

                        // Fast visual templates to enrich profile
                        Text(
                            text = "أو أضف صور جاهزة لأعمال ممتازة تناسب تخصصك:",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            val genericWorkSuggestions = listOf(
                                "تجهيز لوحة" to "https://images.unsplash.com/photo-1558346490-a72e53ae2d4f?w=400",
                                "تورتة طعام" to "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400",
                                "معدات ومفكات" to "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=400"
                            )
                            items(genericWorkSuggestions) { (title, url) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))
                                        .clickable { extraImagesList.add(url) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // List of added works
                        if (extraImagesList.isNotEmpty()) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            Text("الصور المضافة الآن للمعرض (${extraImagesList.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            LazyRow(
                                modifier = Modifier.padding(top = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(extraImagesList) { imgUrl ->
                                    Box(modifier = Modifier.size(70.dp)) {
                                        AsyncImage(
                                            model = imgUrl,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(2.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                                .clickable { extraImagesList.remove(imgUrl) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "حذف",
                                                tint = Color.Red,
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

            // Add Business Button
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (name.isNotEmpty() && title.isNotEmpty() && description.isNotEmpty() && phone.isNotEmpty()) {
                            val extraImagesString = extraImagesList.filter { it.isNotEmpty() }.joinToString(",")
                            viewModel.createProfile(
                                category = category,
                                name = name,
                                title = title,
                                description = description,
                                phoneNumber = phone,
                                mapsLink = mapsLink.ifEmpty { "https://maps.google.com" },
                                logoUri = logoUriInput,
                                extraImages = extraImagesString
                            )
                            Toast.makeText(context, "تم إرسال ونشر صفحتك الإعلانية بالدليل السحابي!", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        } else {
                            Toast.makeText(context, "الرجاء تعبئة المعلومات الأساسية لتتمكن من النشر!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("prof_submit_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Publish, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("انشر ووثق الآن بالدليل", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- SCREEN: EDIT PROFILE ---
@Composable
fun EditProfileScreen(
    profileId: Int,
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profileFlow = remember(profileId) { viewModel.getProfileFlowById(profileId) }
    val existingProfile by profileFlow.collectAsState(initial = null)

    var category by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var mapsLink by remember { mutableStateOf("") }
    var logoUriInput by remember { mutableStateOf("") }

    val extraImagesList = remember { mutableStateListOf<String>() }
    var currentWorkImageUrl by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }
    val categories = listOf("مطاعم", "أطباء ومستشفيات", "محلات وورش", "أدوات كهربائية", "قطع غيار ومواتير", "مهندسين وفنيين", "سكن وعقارات")

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLogoUploading by remember { mutableStateOf(false) }
    var isWorkUploading by remember { mutableStateOf(false) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            isLogoUploading = true
            Toast.makeText(context, "جاري تحضير وضغط الصورة لوجو...", Toast.LENGTH_SHORT).show()
            coroutineScope.launch {
                val base64 = withContext(Dispatchers.IO) {
                    compressAndEncodeImageToUri(context, it, maxDimension = 350)
                }
                isLogoUploading = false
                if (base64 != null) {
                    logoUriInput = base64
                    Toast.makeText(context, "تم تعديل وتجهيز الشعار بنجاح!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "فشل تحميل الصورة من الاستوديو", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val workPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            isWorkUploading = true
            Toast.makeText(context, "جاري تحضير وصقل صورة العمل الجديدة...", Toast.LENGTH_SHORT).show()
            coroutineScope.launch {
                val base64 = withContext(Dispatchers.IO) {
                    compressAndEncodeImageToUri(context, it, maxDimension = 500)
                }
                isWorkUploading = false
                if (base64 != null) {
                    extraImagesList.add(base64)
                    Toast.makeText(context, "تمت إضافة الصورة لمعرض أعمالك بنجاح!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "فشل تحميل الصورة من الاستوديو", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Set initial values when profile is fetched
    LaunchedEffect(existingProfile) {
        existingProfile?.let { prof ->
            if (category.isEmpty()) category = prof.category
            if (name.isEmpty()) name = prof.name
            if (title.isEmpty()) title = prof.title
            if (description.isEmpty()) description = prof.description
            if (phone.isEmpty()) phone = prof.phoneNumber
            if (mapsLink.isEmpty()) mapsLink = prof.mapsLink
            if (logoUriInput.isEmpty()) logoUriInput = prof.logoUri
            
            if (extraImagesList.isEmpty() && prof.extraImages.isNotEmpty()) {
                extraImagesList.addAll(prof.extraImages.split(",").map { it.trim() }.filter { it.isNotEmpty() })
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigateBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تعديل صفحة الدليل الشامل",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        if (existingProfile == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MansouriaSandBackground)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dropdown Category
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            label = { Text("القسم المختار") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expandedDropdown = !expandedDropdown }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedDropdown = !expandedDropdown },
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
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
                }

                // Name
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم التجاري أو اسم صاحب العمل") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Title
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("نبذة مختصرة / المسمى الوظيفي") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Description
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("قائمة السلع والخدمات بالتفصيل") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Phone
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.all { char -> char.isDigit() }) phone = it },
                        label = { Text("رقم تواصل العميل (موبايل)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Google Maps Link
                item {
                    OutlinedTextField(
                        value = mapsLink,
                        onValueChange = { mapsLink = it },
                        label = { Text("رابط موقعك على خرائط جوجل") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // PROFILE PICTURE SELECTOR
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "صورة بروفايل الصفحة (اللوجو):",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Instant preview
                                ProfileImage(logoUri = logoUriInput, category = category, name = name.ifEmpty { "م" }, size = 70)
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Button(
                                        onClick = { logoPickerLauncher.launch("image/*") },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("تحميل صورة من الهاتف", fontSize = 13.sp)
                                    }
                                    
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    Text(
                                        text = if (logoUriInput.startsWith("data:")) "✓ تم رفع الصورة من الهاتف بنجاح" else if (logoUriInput.isNotEmpty()) "✓ تم تعيين الشعار بنجاح" else "لم يتم اختيار صورة بعد",
                                        fontSize = 11.sp,
                                        color = if (logoUriInput.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            var showManualLogoInput by remember { mutableStateOf(false) }
                            Text(
                                    text = if (showManualLogoInput) "إخفاء الرابط اليدوي" else "تعديل رابط الصورة يدوي (اختياري)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { showManualLogoInput = !showManualLogoInput }
                                    .padding(vertical = 4.dp)
                            )
                            
                            if (showManualLogoInput) {
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = logoUriInput,
                                    onValueChange = { logoUriInput = it },
                                    label = { Text("رابط صورة الشعار (URL)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }

                // PORTFOLIO MANAGER
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "تعديل صور سابقة الأعمال والمعرض (عدد غير محدود):",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Gallery picker button as primary action
                            Button(
                                onClick = { workPickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("رفع صورة عمل جديدة من الاستوديو 📸", fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            var showManualWorkInput by remember { mutableStateOf(false) }
                            Text(
                                text = if (showManualWorkInput) "إخفاء الرابط اليدوي" else "إضافة رابط صورة يدوي للعمل (اختياري)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { showManualWorkInput = !showManualWorkInput }
                                    .padding(vertical = 4.dp)
                            )

                            if (showManualWorkInput) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = currentWorkImageUrl,
                                        onValueChange = { currentWorkImageUrl = it },
                                        placeholder = { Text("https://example.com/work_photo.jpg") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (currentWorkImageUrl.trim().isNotEmpty()) {
                                                extraImagesList.add(currentWorkImageUrl.trim())
                                                currentWorkImageUrl = ""
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("أضف")
                                    }
                                }
                            }

                            // Added Images Display
                            if (extraImagesList.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("معرض الصور الحالي (${extraImagesList.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                LazyRow(
                                    modifier = Modifier.padding(top = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(extraImagesList) { imgUrl ->
                                        Box(modifier = Modifier.size(70.dp)) {
                                            AsyncImage(
                                                model = imgUrl,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(2.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                                    .clickable { extraImagesList.remove(imgUrl) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "حذف",
                                                    tint = Color.Red,
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

                // Submit edits Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && title.isNotEmpty() && description.isNotEmpty() && phone.isNotEmpty()) {
                                val extraImagesString = extraImagesList.filter { it.isNotEmpty() }.joinToString(",")
                                viewModel.updateProfile(
                                    id = profileId,
                                    category = category,
                                    name = name,
                                    title = title,
                                    description = description,
                                    phoneNumber = phone,
                                    mapsLink = mapsLink.ifEmpty { "https://maps.google.com" },
                                    logoUri = logoUriInput,
                                    extraImages = extraImagesString
                                )
                                Toast.makeText(context, "تم حفظ وتحديث صفحتك على السيرفر بنجاح!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            } else {
                                Toast.makeText(context, "الرجاء تعبئة البيانات الأساسية أولاً!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ وتوثيق التغييرات بالدليل", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- SCREEN: ACTIVE CHAT ROOM ---
@Composable
fun ChatRoomScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val partnerPhone by viewModel.activeChatPartnerPhone.collectAsState()
    val partnerName by viewModel.activeChatPartnerName.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var messageText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigateBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Initials Circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = partnerName?.take(2) ?: "م",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = partnerName ?: "مستلم غير معروف",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = partnerPhone ?: "-",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MansouriaSandBackground)
        ) {
            // Messages bubble list
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubble,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "ابدأ المحادثة الفورية!",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 15.sp
                            )
                            Text(
                                "استفسر عن توفر السلع أو جدول المواعيد أو الاتفاقات مباشرة هنا.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { message ->
                        val isMine = message.senderPhone == (currentUser?.phoneNumber ?: "")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isMine) 16.dp else 4.dp,
                                            bottomEnd = if (isMine) 4.dp else 16.dp
                                        )
                                    )
                                    .background(
                                        if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .widthIn(max = 280.dp)
                             ) {
                                Column {
                                    Text(
                                        text = message.text,
                                        color = if (isMine) Color.White else MaterialTheme.colorScheme.onBackground,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = formatTime(message.timestamp),
                                        color = if (isMine) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                        fontSize = 9.sp,
                                        modifier = Modifier
                                            .align(Alignment.End)
                                            .padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))

            // Bottom Sending Bar inside safe drawing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("أكتب رسالتك لـ ${partnerName ?: "الصفحة"}...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_text")
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
                    )
                )

                IconButton(
                    onClick = {
                        if (messageText.trim().isNotEmpty()) {
                            viewModel.sendMessage(messageText)
                            messageText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .testTag("submit_message_button"),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "إرسال",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

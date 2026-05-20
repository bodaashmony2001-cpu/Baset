package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import kotlinx.coroutines.launch
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
                // Logo Icon and App Title
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "المنصورية تجمعنا",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
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

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HomeWork,
                        contentDescription = null,
                        tint = Color.White
                    )
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
                    3 -> AccountTab(viewModel, onNavigateToAddProfile)
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
    onNavigateToAddProfile: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allProfiles by viewModel.allProfiles.collectAsState()

    val myAddedProfiles = allProfiles.filter { it.ownerPhone == currentUser?.phoneNumber }

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
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
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

                    Text(
                        text = currentUser?.name ?: "مستخدم المنصورية",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

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
                        BusinessAvatar(name = profile.name, category = profile.category, size = 44)
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
                            BusinessAvatar(name = prof.name, category = prof.category, size = 80)
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
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("تواصل دردشة فورية بالبرنامج", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Portfolio Works Section (As requested: صور وفيديوهات لسابقة الأعمال وعرض السلع)
                item {
                    Text(
                        text = "صور من أعمالنا أو السلع والمنتجات المتاحة المعروضة:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                item {
                    val workPhotos = when (prof.category) {
                        "مطاعم" -> listOf("طواجن فريدة ومشاوي عل فحم", "شاورما دبل ميكس سوري", "كريب شاورما ولحم")
                        "أدوات كهربائية" -> listOf("لوحات مفاتيح وتلامس شنايدر", "كابلات السويدي معتمدة", "لمبات ديكور وليد إنارة")
                        "قطع غيار ومواتير" -> listOf("مواتير مياه إيطالي نص حصان وثلاثة أرباع", "قطع غيار طلمبات ري", "جوانات وسيور أصلية")
                        "مهندسين وفنيين" -> listOf("تصميم لوحة سحب كلاسيك كنترول بمحطة مياه", "تأسيس شبكة إضاءة شقة مساحة واسعة", "إنارة حدائق ومسابح ليلية")
                        "محلات وورش" -> listOf("مجموعة مسامير سن صاج بجميع المقاسات والأقطار", "براغي صلب و تيل", "مفكات وعدد يدوية ألمانية")
                        "أطباء ومستشفيات" -> listOf("قسم رعاية الأطفال والحضانات المجهزة", "غرفة استقبال الطوارئ صيدلية تخصصية")
                        else -> listOf("صورة من مقر المحل أو الفندق", "كتالوج قائمة السلع والخدمات")
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(workPhotos) { photoTitle ->
                            Card(
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(140.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    // Custom beautifully stylized vector representation / photo placeholder card (Avoiding blank spaces elegantly)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.radialGradient(
                                                    listOf(
                                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
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
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = photoTitle,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 16.sp,
                                                maxLines = 3,
                                                overflow = TextOverflow.Ellipsis
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

    var expandedDropdown by remember { mutableStateOf(false) }
    val categories = listOf("مطاعم", "أطباء ومستشفيات", "محلات وورش", "أدوات كهربائية", "قطع غيار ومواتير", "مهندسين وفنيين", "سكن وعقارات")

    val context = LocalContext.current

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
                    text = "قم بإدخال تفاصيل محل عملك أو مهنتك وسيعرف بها جميع مستخدمي المنصورية فوراً عند البحث وسيتلقون طلباتهم.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    lineHeight = 18.sp
                )
            }

            // Dropdown picker
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

            // Business/Owner Name
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
                    placeholder = { Text("مثال: مهندس برمجيات وتطبيقات موبايل، سوبرماركت بيع منتجات غذائية...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prof_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Description of products/services
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("اكتب بالتفصيل قائمة المنتجات أو الخدمات المتاحة") },
                    placeholder = { Text("مثال: نوفر البراغي والري وتصميم اللوحات الكنترول مع كابلات كذا وكذا. محتاجين للورود...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
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

            // Add Business Button
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (name.isNotEmpty() && title.isNotEmpty() && description.isNotEmpty() && phone.isNotEmpty()) {
                            viewModel.createProfile(
                                category = category,
                                name = name,
                                title = title,
                                description = description,
                                phoneNumber = phone,
                                mapsLink = mapsLink.ifEmpty { "https://maps.google.com" }
                            )
                            Toast.makeText(context, "تم إرسال ونشر صفحتك الإعلانية بالدليل!", Toast.LENGTH_SHORT).show()
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

            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))

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

package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
import com.example.data.repository.ChatPreview
import com.example.data.api.AppApi
import com.example.data.api.AppConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = AppRepository(
        database.userDao(),
        database.profileDao(),
        database.postDao(),
        database.commentDao(),
        database.messageDao()
    )

    // User State
    val currentUser: StateFlow<User?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // All available profiles
    val allProfiles: StateFlow<List<Profile>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search & Category Filter UI States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    // Screen dynamic profiles: derived from category selection and search query
    val filteredProfiles: StateFlow<List<Profile>> = combine(
        allProfiles,
        _searchQuery,
        _selectedCategory
    ) { profiles, query, category ->
        var list = profiles
        if (category != null) {
            list = list.filter { it.category == category }
        }
        if (query.isNotEmpty()) {
            list = list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.title.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Community Posts
    val allPosts: StateFlow<List<CommunityPost>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Chat Management
    private val _activeChatPartnerPhone = MutableStateFlow<String?>(null)
    val activeChatPartnerPhone = _activeChatPartnerPhone.asStateFlow()

    private val _activeChatPartnerName = MutableStateFlow<String?>(null)
    val activeChatPartnerName = _activeChatPartnerName.asStateFlow()

    val chatMessages: StateFlow<List<Message>> = combine(
        currentUser,
        _activeChatPartnerPhone
    ) { user, partnerPhone ->
        Pair(user?.phoneNumber ?: "", partnerPhone ?: "")
    }.flatMapLatest { (myPhone, peerPhone) ->
        if (myPhone.isEmpty() || peerPhone.isEmpty()) {
            flowOf(emptyList())
        } else {
            repository.getMessagesWith(myPhone, peerPhone)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chats history list
    val recentChats: StateFlow<List<ChatPreview>> = currentUser
        .flatMapLatest { user ->
            val myPhone = user?.phoneNumber ?: ""
            if (myPhone.isEmpty()) {
                flowOf(emptyList())
            } else {
                repository.getRecentChats(myPhone)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Application Config State (Customizable App Images by Admin)
    private val _appConfig = MutableStateFlow(AppConfig())
    val appConfig = _appConfig.asStateFlow()

    // Flag for Admin Mode
    private val _isAdminModeEnabled = MutableStateFlow(false)
    val isAdminModeEnabled = _isAdminModeEnabled.asStateFlow()

    init {
        // Run database prepopulate (will be empty)
        viewModelScope.launch {
            repository.prepopulateIfNeeded()
        }

        // Active background server synchronization loop (delivers real-time collaborative updates)
        viewModelScope.launch {
            while (true) {
                try {
                    repository.syncWithServer()
                    
                    // Pull updated App Logos config from server if available
                    val config = AppApi.instance.getAppConfig()
                    if (config != null) {
                        _appConfig.value = config
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(4000) // Sync every 4 seconds
            }
        }
    }

    // AUTH
    fun handleLogin(phone: String, name: String) {
        viewModelScope.launch {
            repository.login(phone, name)
            // If the user logs in as Admin phone or starts Admin
            if (phone == "01000000000") {
                _isAdminModeEnabled.value = true
            }
        }
    }

    fun handleLogout() {
        viewModelScope.launch {
            repository.logout()
            _isAdminModeEnabled.value = false
        }
    }

    fun toggleAdminMode(enabled: Boolean) {
        _isAdminModeEnabled.value = enabled
    }

    // UPDATE CURRENT USER DETAILS
    fun updateCurrentUserName(newName: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val updatedUser = user.copy(name = newName)
            repository.login(updatedUser.phoneNumber, updatedUser.name)
        }
    }

    // UPDATE APP CONFIG (ADMIN ACTIONS)
    fun updateAppConfig(
        outerUrl: String,
        innerUrl: String,
        c1Name: String,
        c1Photo: String,
        c2Name: String,
        c2Photo: String
    ) {
        viewModelScope.launch {
            val updated = AppConfig(
                outerLogoUrl = outerUrl,
                innerLogoUrl = innerUrl,
                creator1Name = c1Name,
                creator1Photo = c1Photo,
                creator2Name = c2Name,
                creator2Photo = c2Photo
            )
            _appConfig.value = updated
            try {
                AppApi.instance.uploadAppConfig(updated)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // SEARCH & FILTER ACTIONS
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    // ADD / UPDATE PROFILE
    fun createProfile(
        category: String,
        name: String,
        title: String,
        description: String,
        phoneNumber: String,
        mapsLink: String,
        logoUri: String = "",
        extraImages: String = ""
    ) {
        viewModelScope.launch {
            val creatorPhone = currentUser.value?.phoneNumber ?: ""
            val newProfile = Profile(
                category = category,
                name = name,
                title = title,
                description = description,
                phoneNumber = phoneNumber,
                mapsLink = mapsLink,
                logoUri = logoUri.ifEmpty { "avatar_default" },
                extraImages = extraImages,
                ownerPhone = creatorPhone
            )
            repository.insertProfile(newProfile)
        }
    }

    fun updateProfile(
        id: Int,
        category: String,
        name: String,
        title: String,
        description: String,
        phoneNumber: String,
        mapsLink: String,
        logoUri: String,
        extraImages: String
    ) {
        viewModelScope.launch {
            val creatorPhone = currentUser.value?.phoneNumber ?: ""
            val updatedProfile = Profile(
                id = id,
                category = category,
                name = name,
                title = title,
                description = description,
                phoneNumber = phoneNumber,
                mapsLink = mapsLink,
                logoUri = logoUri,
                extraImages = extraImages,
                ownerPhone = creatorPhone
            )
            repository.insertProfile(updatedProfile)
        }
    }

    // DIRECT PROFILE SEARCH BY ID
    fun getProfileFlowById(id: Int): Flow<Profile?> {
        return repository.getProfileById(id)
    }

    // CREATE POST
    fun createCommunityPost(content: String, imageUri: String = "") {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val post = CommunityPost(
                authorName = user.name,
                authorPhone = user.phoneNumber,
                content = content,
                imageUri = imageUri
            )
            repository.insertPost(post)
        }
    }

    // GET COMMENTS FOR POST
    fun getComments(postId: Int): Flow<List<Comment>> {
        return repository.getCommentsForPost(postId)
    }

    // ADD COMMENT
    fun addCommentToPost(postId: Int, content: String, linkedProfileId: Int? = null) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val comment = Comment(
                postId = postId,
                authorName = user.name,
                authorPhone = user.phoneNumber,
                linkedProfileId = linkedProfileId,
                content = content
            )
            repository.insertComment(comment)
        }
    }

    // CHAT MANAGEMENT
    fun setActiveChatPartner(phone: String, name: String) {
        _activeChatPartnerPhone.value = phone
        _activeChatPartnerName.value = name
    }

    fun sendMessage(msgText: String) {
        val user = currentUser.value ?: return
        val partnerPhone = _activeChatPartnerPhone.value ?: return
        val partnerName = _activeChatPartnerName.value ?: "مستلم"
        if (msgText.trim().isEmpty()) return

        viewModelScope.launch {
            val message = Message(
                chatWithPhone = partnerPhone,
                senderPhone = user.phoneNumber,
                senderName = user.name,
                receiverPhone = partnerPhone,
                receiverName = partnerName,
                text = msgText
            )
            repository.insertMessage(message)
        }
    }
}

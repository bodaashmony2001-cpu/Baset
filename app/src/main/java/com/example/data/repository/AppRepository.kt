package com.example.data.repository

import com.example.data.dao.*
import com.example.data.model.*
import com.example.data.api.AppApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AppRepository(
    private val userDao: UserDao,
    private val profileDao: ProfileDao,
    private val postDao: PostDao,
    private val commentDao: CommentDao,
    private val messageDao: MessageDao
) {
    // Current User
    val currentUser: Flow<User?> = userDao.getCurrentUserFlow()

    suspend fun getLoggedInUserSync(): User? = userDao.getCurrentUser()

    suspend fun login(phoneNumber: String, name: String) {
        userDao.loginUser(phoneNumber, name)
    }

    suspend fun logout() {
        userDao.logout()
    }

    // Profiles
    val allProfiles: Flow<List<Profile>> = profileDao.getAllProfiles()

    fun getProfilesByCategory(category: String): Flow<List<Profile>> {
        return profileDao.getProfilesByCategory(category)
    }

    fun getProfileById(id: Int): Flow<Profile?> {
        return profileDao.getProfileById(id)
    }

    private fun generateUniqueId(): Int {
        return ((System.currentTimeMillis() % 1_000_000_000).toInt() + (1000..9999).random()).coerceAtLeast(1)
    }

    suspend fun insertProfile(profile: Profile) {
        val finalProfile = if (profile.id == 0) {
            profile.copy(id = generateUniqueId())
        } else {
            profile
        }
        profileDao.insertProfile(finalProfile)
        try {
            AppApi.instance.uploadProfile(finalProfile.id, finalProfile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun searchProfiles(query: String): Flow<List<Profile>> {
        return profileDao.searchProfiles(query)
    }

    // Community Posts
    val allPosts: Flow<List<CommunityPost>> = postDao.getAllPosts()

    suspend fun insertPost(post: CommunityPost) {
        val finalPost = if (post.id == 0) {
            post.copy(id = generateUniqueId())
        } else {
            post
        }
        postDao.insertPost(finalPost)
        try {
            AppApi.instance.uploadPost(finalPost.id, finalPost)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Comments
    fun getCommentsForPost(postId: Int): Flow<List<Comment>> {
        return commentDao.getCommentsForPost(postId)
    }

    suspend fun insertComment(comment: Comment) {
        val finalComment = if (comment.id == 0) {
            comment.copy(id = generateUniqueId())
        } else {
            comment
        }
        commentDao.insertComment(finalComment)
        try {
            AppApi.instance.uploadComment(finalComment.id, finalComment)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Messages / Chat
    fun getMessagesWith(myPhone: String, peerPhone: String): Flow<List<Message>> {
        return messageDao.getMessagesWith(myPhone, peerPhone)
    }

    suspend fun insertMessage(message: Message) {
        val finalMessage = if (message.id == 0) {
            message.copy(id = generateUniqueId())
        } else {
            message
        }
        messageDao.insertMessage(finalMessage)
        try {
            AppApi.instance.uploadMessage(finalMessage.id, finalMessage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Retrieve active chats grouped by user
    fun getRecentChats(myPhone: String): Flow<List<ChatPreview>> {
        return messageDao.getAllConversationsRaw(myPhone).map { messages ->
            messages.groupBy { msg ->
                if (msg.senderPhone == myPhone) msg.receiverPhone else msg.senderPhone
            }.map { (peerPhone, msgs) ->
                val latest = msgs.maxByOrNull { it.timestamp }!!
                val peerName = if (latest.senderPhone == myPhone) latest.receiverName else latest.senderName
                ChatPreview(
                    peerPhone = peerPhone,
                    peerName = peerName,
                    lastMessage = latest.text,
                    timestamp = latest.timestamp
                )
            }.sortedByDescending { it.timestamp }
        }
    }

    // Server-to-Local Synchronization
    suspend fun syncWithServer() {
        try {
            val remoteProfiles = AppApi.instance.getProfiles()
            if (remoteProfiles != null) {
                remoteProfiles.values.filterNotNull().forEach { profile ->
                    profileDao.insertProfile(profile)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val remotePosts = AppApi.instance.getPosts()
            if (remotePosts != null) {
                remotePosts.values.filterNotNull().forEach { post ->
                    postDao.insertPost(post)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val remoteComments = AppApi.instance.getComments()
            if (remoteComments != null) {
                remoteComments.values.filterNotNull().forEach { comment ->
                    commentDao.insertComment(comment)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val remoteMessages = AppApi.instance.getMessages()
            if (remoteMessages != null) {
                remoteMessages.values.filterNotNull().forEach { message ->
                    messageDao.insertMessage(message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun prepopulateIfNeeded() {
        // App starts from scratch (empty zero state) as requested by user.
    }
}

data class ChatPreview(
    val peerPhone: String,
    val peerName: String,
    val lastMessage: String,
    val timestamp: Long
)

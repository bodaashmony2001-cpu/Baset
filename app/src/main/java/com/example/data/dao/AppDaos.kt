package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getCurrentUserFlow(): Flow<User?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getCurrentUser(): User?

    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun clearLoginState()

    @Transaction
    suspend fun loginUser(phone: String, name: String) {
        clearLoginState()
        val existing = getUserByPhone(phone)
        if (existing != null) {
            insertUser(existing.copy(isLoggedIn = true, name = name))
        } else {
            insertUser(User(phoneNumber = phone, name = name, isLoggedIn = true))
        }
    }

    @Transaction
    suspend fun logout() {
        clearLoginState()
    }
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY id DESC")
    fun getAllProfiles(): Flow<List<Profile>>

    @Query("SELECT * FROM profiles WHERE category = :category ORDER BY id DESC")
    fun getProfilesByCategory(category: String): Flow<List<Profile>>

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    fun getProfileById(id: Int): Flow<Profile?>

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileByIdSync(id: Int): Profile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile)

    @Query("""
        SELECT * FROM profiles 
        WHERE name LIKE '%' || :query || '%' 
        OR title LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        ORDER BY id DESC
    """)
    fun searchProfiles(query: String): Flow<List<Profile>>
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<CommunityPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPost)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: Int): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)
}

@Dao
interface MessageDao {
    @Query("""
        SELECT * FROM messages 
        WHERE (senderPhone = :myPhone AND receiverPhone = :peerPhone) 
        OR (senderPhone = :peerPhone AND receiverPhone = :myPhone) 
        ORDER BY timestamp ASC
    """)
    fun getMessagesWith(myPhone: String, peerPhone: String): Flow<List<Message>>

    @Query("""
        SELECT * FROM messages 
        WHERE senderPhone = :myPhone OR receiverPhone = :myPhone 
        ORDER BY timestamp DESC
    """)
    fun getAllConversationsRaw(myPhone: String): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)
}

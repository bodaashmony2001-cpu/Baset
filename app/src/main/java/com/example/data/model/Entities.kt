package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val phoneNumber: String,
    val name: String,
    val isLoggedIn: Boolean = false
)

@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,         // e.g. "مطاعم", "أطباء ومستشفيات", "مهندسين وفنيين", "محلات وورش", "قطع غيار ومواتير", "خدمات عامة"
    val name: String,             // Owner or shop name
    val title: String,            // Profession or Business description
    val description: String,      // Services, products, e.g. "نبيع مسامير، أدوات كهربائية، أسلاك..."
    val phoneNumber: String,
    val mapsLink: String,         // Google Maps Link
    val logoUri: String = "",     // Profile photo URI or fallback representation
    val extraImages: String = "", // Comma-separated URIs/paths for work gallery
    val ownerPhone: String = ""   // Keep track of who created this profile (phone login)
)

@Entity(tableName = "posts")
data class CommunityPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val authorName: String,
    val authorPhone: String,
    val content: String,
    val imageUri: String = "",    // Optional photo of requested item
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val postId: Int,
    val authorName: String,
    val authorPhone: String,
    val linkedProfileId: Int? = null, // Link to a business profile
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatWithPhone: String,    // The phone number of the target peer
    val senderPhone: String,
    val senderName: String,
    val receiverPhone: String,
    val receiverName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

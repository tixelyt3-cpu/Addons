package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "addons")
data class AddonEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "Add-on", "Textura", "Skin Pack", "Shader"
    val description: String,
    val creatorName: String,
    val downloadUrl: String,
    val bannerType: String, // e.g. "creeper", "sword", "dirt", "portal", "water", "gold", "redstone"
    val isFavorite: Boolean = false,
    val isSaved: Boolean = false,
    val downloadsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "followed_creators")
data class FollowedCreatorEntity(
    @PrimaryKey val creatorName: String
)

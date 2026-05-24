package com.example.data

import kotlinx.coroutines.flow.Flow

class AddonRepository(private val addonDao: AddonDao) {
    val allAddons: Flow<List<AddonEntity>> = addonDao.getAllAddons()
    val followedCreators: Flow<List<FollowedCreatorEntity>> = addonDao.getFollowedCreators()

    suspend fun publishAddon(addon: AddonEntity) {
        addonDao.insertAddon(addon)
    }

    suspend fun toggleFavorite(id: Int, isFavorite: Boolean) {
        addonDao.updateFavorite(id, isFavorite)
    }

    suspend fun toggleSaved(id: Int, isSaved: Boolean) {
        addonDao.updateSaved(id, isSaved)
    }

    suspend fun recordDownload(id: Int) {
        addonDao.incrementDownloads(id)
    }

    suspend fun followCreator(creatorName: String) {
        addonDao.followCreator(FollowedCreatorEntity(creatorName))
    }

    suspend fun unfollowCreator(creatorName: String) {
        addonDao.unfollowCreator(creatorName)
    }
}

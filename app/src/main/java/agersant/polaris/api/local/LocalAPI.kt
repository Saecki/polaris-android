package agersant.polaris.api.local

import agersant.polaris.CollectionItem
import agersant.polaris.api.IPolarisAPI
import agersant.polaris.api.ThumbnailSize
import android.graphics.Bitmap
import com.google.android.exoplayer2.source.MediaSource

class LocalAPI : IPolarisAPI {
    private lateinit var offlineCache: OfflineCache

    fun initialize(offlineCache: OfflineCache) {
        this.offlineCache = offlineCache
    }

    fun hasAudio(item: CollectionItem): Boolean {
        val path = item.path
        return offlineCache.hasAudio(path)
    }

    override suspend fun getAudio(item: CollectionItem): MediaSource? {
        return offlineCache.getAudio(item.path)
    }

    fun hasImage(item: CollectionItem, size: ThumbnailSize): Boolean {
        val path = item.artwork ?: return false
        return offlineCache.hasImage(path, size)
    }

    fun getImage(item: CollectionItem, size: ThumbnailSize): Bitmap? {
        val path = item.artwork ?: return null
        return offlineCache.getImage(path, size)
    }

    override suspend fun browse(path: String): List<CollectionItem>? {
        return offlineCache.browse(path)
    }

    override suspend fun flatten(path: String): List<CollectionItem>? {
        return offlineCache.flatten(path)
    }
}

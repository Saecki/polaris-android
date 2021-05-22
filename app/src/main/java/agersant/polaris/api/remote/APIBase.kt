package agersant.polaris.api.remote

import agersant.polaris.CollectionItem
import agersant.polaris.api.ThumbnailSize
import agersant.polaris.api.remote.ServerAPI.Companion.apiRootURL
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.android.exoplayer2.source.MediaSource
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*

abstract class APIBase(
    private val downloadQueue: DownloadQueue,
    protected val client: HttpClient,
) : IRemoteAPI {

    abstract fun getAudioUrl(path: String): String

    abstract fun getThumbnailUrl(path: String, size: ThumbnailSize): String

    protected abstract suspend fun getAlbums(url: String): List<CollectionItem>?

    override suspend fun getAudioUri(path: String): Uri? {
        val url = getAudioUrl(path)
        return Uri.parse(url)
    }

    override suspend fun getAudio(item: CollectionItem): MediaSource? {
        return downloadQueue.getAudio(item)
    }

    override suspend fun getThumbnail(path: String, size: ThumbnailSize): Bitmap? {
        return try {
            val response: HttpResponse = client.get(getThumbnailUrl(path, size))
            val stream = response.content.toInputStream()
            BitmapFactory.decodeStream(stream)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getRandomAlbums(): List<CollectionItem>? {
        val url = "$apiRootURL/random/"
        return getAlbums(url)
    }

    override suspend fun getRecentAlbums(): List<CollectionItem>? {
        val url = "$apiRootURL/recent/"
        return getAlbums(url)
    }
}

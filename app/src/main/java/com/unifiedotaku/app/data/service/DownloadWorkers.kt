package com.unifiedotaku.app.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.unifiedotaku.app.R
import com.unifiedotaku.app.data.local.database.dao.DownloadDao
import com.unifiedotaku.app.data.local.database.entities.DownloadStatus
import com.unifiedotaku.app.data.local.database.entities.MediaType
import com.unifiedotaku.app.data.remote.scraper.AnimeScraper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

/**
 * Worker for downloading anime episodes (HLS/MP4).
 */
@HiltWorker
class AnimeDownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val downloadDao: DownloadDao,
    private val animeScraper: AnimeScraper,
    private val okHttpClient: OkHttpClient
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_DOWNLOAD_ID = "download_id"
        const val KEY_EPISODE_ID = "episode_id"
        const val KEY_SERIES_ID = "series_id"
        const val KEY_SERIES_TITLE = "series_title"
        const val KEY_EPISODE_NUMBER = "episode_number"
        
        private const val CHANNEL_ID = "download_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val downloadId = inputData.getString(KEY_DOWNLOAD_ID) ?: return@withContext Result.failure()
        val episodeId = inputData.getString(KEY_EPISODE_ID) ?: return@withContext Result.failure()
        val seriesTitle = inputData.getString(KEY_SERIES_TITLE) ?: "Anime"
        val episodeNumber = inputData.getInt(KEY_EPISODE_NUMBER, 1)

        try {
            // Update status to downloading
            downloadDao.updateProgress(downloadId, DownloadStatus.DOWNLOADING, 0)
            showNotification("Downloading $seriesTitle Ep $episodeNumber", 0)

            // Get stream sources
            val sources = animeScraper.getStreamSources(episodeId)
            if (sources.isEmpty()) {
                downloadDao.markFailed(downloadId, "No stream sources found")
                return@withContext Result.failure()
            }

            // Pick best quality MP4 source (prefer MP4 over HLS for downloads)
            val source = sources.find { !it.isM3U8 } ?: sources.first()

            // Create download directory
            val downloadDir = File(context.getExternalFilesDir(null), "downloads/anime")
            if (!downloadDir.exists()) downloadDir.mkdirs()

            val fileName = "${seriesTitle.replace("[^a-zA-Z0-9]".toRegex(), "_")}_E${episodeNumber}.mp4"
            val outputFile = File(downloadDir, fileName)

            // Download the file
            val request = Request.Builder()
                .url(source.url)
                .apply { source.headers.forEach { (k, v) -> addHeader(k, v) } }
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                downloadDao.markFailed(downloadId, "HTTP ${response.code}")
                return@withContext Result.failure()
            }

            val body = response.body ?: run {
                downloadDao.markFailed(downloadId, "Empty response")
                return@withContext Result.failure()
            }

            val totalBytes = body.contentLength()
            var downloadedBytes = 0L

            body.byteStream().use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        // Update progress
                        val progress = if (totalBytes > 0) {
                            ((downloadedBytes * 100) / totalBytes).toInt()
                        } else 0

                        downloadDao.updateProgress(downloadId, DownloadStatus.DOWNLOADING, progress)
                        showNotification("Downloading $seriesTitle Ep $episodeNumber", progress)
                        setProgress(workDataOf("progress" to progress))
                    }
                }
            }

            // Mark as completed
            downloadDao.markCompleted(downloadId)
            showNotification("Downloaded $seriesTitle Ep $episodeNumber", 100, completed = true)

            Result.success()
        } catch (e: Exception) {
            downloadDao.markFailed(downloadId, e.message)
            showNotification("Failed: ${e.message}", 0, failed = true)
            Result.failure()
        }
    }

    private fun showNotification(msg: String, progress: Int, completed: Boolean = false, failed: Boolean = false) {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Unified Otaku")
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(!completed && !failed)
            .apply {
                if (!completed && !failed) {
                    setProgress(100, progress, progress == 0)
                }
            }
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Download progress notifications"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}

/**
 * Worker for downloading manga chapters.
 */
@HiltWorker
class MangaDownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val downloadDao: DownloadDao,
    private val mangaRepository: com.unifiedotaku.app.data.repository.MangaRepository,
    private val okHttpClient: OkHttpClient
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_DOWNLOAD_ID = "download_id"
        const val KEY_CHAPTER_ID = "chapter_id"
        const val KEY_SERIES_ID = "series_id"
        const val KEY_SERIES_TITLE = "series_title"
        const val KEY_CHAPTER_NUMBER = "chapter_number"
        
        private const val CHANNEL_ID = "download_channel"
        private const val NOTIFICATION_ID = 1002
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val downloadId = inputData.getString(KEY_DOWNLOAD_ID) ?: return@withContext Result.failure()
        val chapterId = inputData.getString(KEY_CHAPTER_ID) ?: return@withContext Result.failure()
        val seriesTitle = inputData.getString(KEY_SERIES_TITLE) ?: "Manga"
        val chapterNumber = inputData.getFloat(KEY_CHAPTER_NUMBER, 1f)

        try {
            downloadDao.updateProgress(downloadId, DownloadStatus.DOWNLOADING, 0)
            showNotification("Downloading $seriesTitle Ch $chapterNumber", 0)

            // Extract extension from seriesId (format: "manga:{extensionId}:{rawId}")
            val seriesId = inputData.getString(KEY_SERIES_ID) ?: ""
            val extension = if (seriesId.startsWith("manga:")) {
                seriesId.split(":", limit = 3).getOrNull(1) ?: "comix.to"
            } else {
                "comix.to" // Fallback for old-format IDs
            }

            // Get chapter pages via Repository (Extension)
            val pagesResult = mangaRepository.getMangaPages(chapterId, extension)
            val pages = pagesResult.getOrNull() ?: emptyList()

            if (pages.isEmpty()) {
                downloadDao.markFailed(downloadId, "No pages found or error: ${pagesResult.exceptionOrNull()?.message}")
                return@withContext Result.failure()
            }

            // Create download directory
            val downloadDir = File(
                context.getExternalFilesDir(null),
                "downloads/manga/${seriesTitle.replace("[^a-zA-Z0-9]".toRegex(), "_")}/ch_$chapterNumber"
            )
            if (!downloadDir.exists()) downloadDir.mkdirs()

            // Download each page
            pages.forEachIndexed { index, imageUrl ->
                val fileName = "page_${String.format("%03d", index + 1)}.jpg"
                val outputFile = File(downloadDir, fileName)

                val request = Request.Builder()
                    .url(imageUrl)
                    .build()

                try {
                    val response = okHttpClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        response.body?.byteStream()?.use { input ->
                            FileOutputStream(outputFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Continue with other pages
                }

                val progress = ((index + 1) * 100) / pages.size
                downloadDao.updateProgress(downloadId, DownloadStatus.DOWNLOADING, progress)
                showNotification("Downloading $seriesTitle Ch $chapterNumber", progress)
                setProgress(workDataOf("progress" to progress))
            }

            downloadDao.markCompleted(downloadId)
            showNotification("Downloaded $seriesTitle Ch $chapterNumber", 100, completed = true)

            Result.success()
        } catch (e: Exception) {
            downloadDao.markFailed(downloadId, e.message)
            Result.failure()
        }
    }

    private fun showNotification(msg: String, progress: Int, completed: Boolean = false) {
        createNotificationChannel()
        // Similar to AnimeDownloadWorker
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Unified Otaku")
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(!completed)
            .apply {
                if (!completed) setProgress(100, progress, progress == 0)
            }
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Download progress notifications"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}

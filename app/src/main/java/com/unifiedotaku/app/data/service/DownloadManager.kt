package com.unifiedotaku.app.data.service

import android.content.Context
import androidx.work.*
import com.unifiedotaku.app.data.local.database.dao.DownloadDao
import com.unifiedotaku.app.data.local.database.entities.Download
import com.unifiedotaku.app.data.local.database.entities.DownloadStatus
import com.unifiedotaku.app.data.local.database.entities.MediaType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for handling all download operations.
 * Provides a clean API for queueing, pausing, and managing downloads.
 */
@Singleton
class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadDao: DownloadDao
) {
    private val workManager = WorkManager.getInstance(context)

    /**
     * Queue an anime episode for download.
     */
    suspend fun queueAnimeDownload(
        seriesId: String,
        seriesTitle: String,
        seriesCoverUrl: String,
        episodeId: String,
        episodeNumber: Int,
        episodeTitle: String
    ): String {
        val downloadId = UUID.randomUUID().toString()
        
        // Create download directory and file path
        val downloadDir = File(context.getExternalFilesDir(null), "downloads/anime")
        if (!downloadDir.exists()) downloadDir.mkdirs()
        val fileName = "${seriesTitle.replace("[^a-zA-Z0-9]".toRegex(), "_")}_E${episodeNumber}.mp4"
        val filePath = File(downloadDir, fileName).absolutePath

        // Create download record using Download entity
        val download = Download(
            id = downloadId,
            seriesId = seriesId,
            seriesTitle = seriesTitle,
            seriesCoverUrl = seriesCoverUrl,
            type = MediaType.ANIME,
            contentId = episodeId,
            number = episodeNumber,
            title = episodeTitle,
            filePath = filePath,
            fileSize = 0L,
            status = DownloadStatus.PENDING,
            progress = 0,
            error = null,
            createdAt = System.currentTimeMillis(),
            completedAt = null
        )
        downloadDao.upsert(download)

        // Create work request
        val workData = workDataOf(
            AnimeDownloadWorker.KEY_DOWNLOAD_ID to downloadId,
            AnimeDownloadWorker.KEY_EPISODE_ID to episodeId,
            AnimeDownloadWorker.KEY_SERIES_ID to seriesId,
            AnimeDownloadWorker.KEY_SERIES_TITLE to seriesTitle,
            AnimeDownloadWorker.KEY_EPISODE_NUMBER to episodeNumber
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<AnimeDownloadWorker>()
            .setInputData(workData)
            .setConstraints(constraints)
            .addTag("download")
            .addTag("anime")
            .addTag(seriesId)
            .build()

        workManager.enqueueUniqueWork(
            "download_$downloadId",
            ExistingWorkPolicy.KEEP,
            workRequest
        )

        return downloadId
    }

    /**
     * Queue a manga chapter for download.
     */
    suspend fun queueMangaDownload(
        seriesId: String,
        seriesTitle: String,
        seriesCoverUrl: String,
        chapterId: String,
        chapterNumber: Float,
        chapterTitle: String?
    ): String {
        val downloadId = UUID.randomUUID().toString()
        
        // Create download directory and path
        val downloadDir = File(
            context.getExternalFilesDir(null),
            "downloads/manga/${seriesTitle.replace("[^a-zA-Z0-9]".toRegex(), "_")}/ch_$chapterNumber"
        )
        if (!downloadDir.exists()) downloadDir.mkdirs()
        val filePath = downloadDir.absolutePath

        val download = Download(
            id = downloadId,
            seriesId = seriesId,
            seriesTitle = seriesTitle,
            seriesCoverUrl = seriesCoverUrl,
            type = MediaType.MANGA,
            contentId = chapterId,
            number = chapterNumber.toInt(),
            title = chapterTitle ?: "Chapter $chapterNumber",
            filePath = filePath,
            fileSize = 0L,
            status = DownloadStatus.PENDING,
            progress = 0,
            error = null,
            createdAt = System.currentTimeMillis(),
            completedAt = null
        )
        downloadDao.upsert(download)

        val workData = workDataOf(
            MangaDownloadWorker.KEY_DOWNLOAD_ID to downloadId,
            MangaDownloadWorker.KEY_CHAPTER_ID to chapterId,
            MangaDownloadWorker.KEY_SERIES_ID to seriesId,
            MangaDownloadWorker.KEY_SERIES_TITLE to seriesTitle,
            MangaDownloadWorker.KEY_CHAPTER_NUMBER to chapterNumber
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<MangaDownloadWorker>()
            .setInputData(workData)
            .setConstraints(constraints)
            .addTag("download")
            .addTag("manga")
            .addTag(seriesId)
            .build()

        workManager.enqueueUniqueWork(
            "download_$downloadId",
            ExistingWorkPolicy.KEEP,
            workRequest
        )

        return downloadId
    }

    /**
     * Pause a download.
     */
    suspend fun pauseDownload(downloadId: String) {
        workManager.cancelUniqueWork("download_$downloadId")
        downloadDao.pause(downloadId)
    }

    /**
     * Resume a paused download.
     */
    suspend fun resumeDownload(downloadId: String) {
        val download = downloadDao.getById(downloadId) ?: return
        downloadDao.resume(downloadId)
        
        // Re-queue the work based on type
        val workData = when (download.type) {
            MediaType.ANIME -> workDataOf(
                AnimeDownloadWorker.KEY_DOWNLOAD_ID to downloadId,
                AnimeDownloadWorker.KEY_EPISODE_ID to download.contentId,
                AnimeDownloadWorker.KEY_SERIES_ID to download.seriesId,
                AnimeDownloadWorker.KEY_SERIES_TITLE to download.seriesTitle,
                AnimeDownloadWorker.KEY_EPISODE_NUMBER to download.number
            )
            MediaType.MANGA -> workDataOf(
                MangaDownloadWorker.KEY_DOWNLOAD_ID to downloadId,
                MangaDownloadWorker.KEY_CHAPTER_ID to download.contentId,
                MangaDownloadWorker.KEY_SERIES_ID to download.seriesId,
                MangaDownloadWorker.KEY_SERIES_TITLE to download.seriesTitle,
                MangaDownloadWorker.KEY_CHAPTER_NUMBER to download.number.toFloat()
            )
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()

        val workRequest = when (download.type) {
            MediaType.ANIME -> OneTimeWorkRequestBuilder<AnimeDownloadWorker>()
            MediaType.MANGA -> OneTimeWorkRequestBuilder<MangaDownloadWorker>()
        }.setInputData(workData)
            .setConstraints(constraints)
            .addTag("download")
            .build()

        workManager.enqueueUniqueWork(
            "download_$downloadId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Cancel and delete a download.
     */
    suspend fun cancelDownload(downloadId: String) {
        workManager.cancelUniqueWork("download_$downloadId")
        downloadDao.deleteById(downloadId)
    }

    /**
     * Cancel all downloads for a series.
     */
    suspend fun cancelSeriesDownloads(seriesId: String) {
        workManager.cancelAllWorkByTag(seriesId)
        downloadDao.deleteForSeries(seriesId)
    }

    /**
     * Get all downloads.
     */
    fun getAllDownloads(): Flow<List<Download>> = downloadDao.getAll()

    /**
     * Get downloads by type.
     */
    fun getDownloadsByType(type: MediaType): Flow<List<Download>> = downloadDao.getByType(type)

    /**
     * Get downloads for a series.
     */
    fun getDownloadsForSeries(seriesId: String): Flow<List<Download>> = downloadDao.getForSeries(seriesId)

    /**
     * Get active downloads.
     */
    fun getActiveDownloads(): Flow<List<Download>> = downloadDao.getActiveDownloads()

    /**
     * Delete completed downloads.
     */
    suspend fun clearCompleted() {
        downloadDao.deleteCompleted()
    }

    /**
     * Get total downloaded size.
     */
    suspend fun getTotalDownloadedSize(): Long = downloadDao.getTotalDownloadedSize() ?: 0L
}

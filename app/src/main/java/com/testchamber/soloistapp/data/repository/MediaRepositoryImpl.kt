package com.testchamber.soloistapp.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.testchamber.soloistapp.domain.models.Track
import com.testchamber.soloistapp.domain.repository.MediaRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepositoryImpl
    @Inject
    constructor(
        private val context: Context,
    ) : MediaRepository {
        override suspend fun getLocalTracks(): List<Track> =
            withContext(Dispatchers.IO) {
                val tracks = mutableListOf<Track>()

                val projection =
                    arrayOf(
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ALBUM_ID,
                    )

                val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

                context.contentResolver
                    .query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        null,
                        null,
                    )?.use { cursor ->
                        while (cursor.moveToNext()) {
                            val id =
                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                            val title =
                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                            val artist =
                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                            val duration =
                                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                            val albumId =
                                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))

                            val uri =
                                ContentUris
                                    .withAppendedId(
                                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                        id.toLong(),
                                    ).toString()
                            val coverArtUri = getCoverArtUri(albumId)?.toString()

                            tracks.add(Track(id, title, artist, duration, uri, coverArtUri))
                        }
                    }
                tracks
            }

        private fun getCoverArtUri(albumId: Long): Uri? =
            try {
                ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId,
                )
            } catch (e: Exception) {
                Log.e("MediaRepository", "Error getting cover art uri", e)
                null
            }
    }

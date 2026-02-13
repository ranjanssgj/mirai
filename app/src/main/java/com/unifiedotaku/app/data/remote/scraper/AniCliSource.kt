package com.unifiedotaku.app.data.remote.scraper

import android.util.Log
import com.unifiedotaku.app.data.extensions.AnimeSource
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class AniCliSource @Inject constructor(
    private val client: OkHttpClient
) : AnimeSource {
    override val name = "AllAnime"
    private val host = "https://api.allanime.day/api"
    private val headers = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0",
        "Referer" to "https://allmanga.to"
    )

    // Decryption map from ani-cli (line 171)
    private fun decryptSource(hex: String): String {
        val cleanHex = if (hex.startsWith("--")) hex.substring(2) else hex
        val result = StringBuilder()
        var i = 0
        
        // Exact mapping from ani-cli
        // s/^79$/A/g;s/^7a$/B/g...
        val mapping = mapOf(
            "01" to "9", "08" to "0", "0a" to "2", "0b" to "3", "0c" to "4", "0d" to "5", "0e" to "6", "0f" to "7", "00" to "8",
            "02" to ":", "03" to ";", "07" to "?", "17" to "/", "1b" to "#", "05" to "=", "09" to "1",
            "15" to "-", "16" to ".", "67" to "_", "46" to "~", "63" to "[", "65" to "]", "78" to "@",
            "19" to "!", "1c" to "$", "1e" to "&", "10" to "(", "11" to ")", "12" to "*", "13" to "+", "14" to ",",
            "1d" to "%",
            // Letters
            "79" to "A", "7a" to "B", "7b" to "C", "7c" to "D", "7d" to "E", "7e" to "F", "7f" to "G",
            "70" to "H", "71" to "I", "72" to "J", "73" to "K", "74" to "L", "75" to "M", "76" to "N", 
            "77" to "O", "68" to "P", "69" to "Q", "6a" to "R", "6b" to "S", "6c" to "T", "6d" to "U", 
            "6e" to "V", "6f" to "W", "60" to "X", "61" to "Y", "62" to "Z",
            "59" to "a", "5a" to "b", "5b" to "c", "5c" to "d", "5d" to "e", "5e" to "f", "5f" to "g",
            "50" to "h", "51" to "i", "52" to "j", "53" to "k", "54" to "l", "55" to "m", "56" to "n",
            "57" to "o", "48" to "p", "49" to "q", "4a" to "r", "4b" to "s", "4c" to "t", "4d" to "u",
            "4e" to "v", "4f" to "w", "40" to "x", "41" to "y", "42" to "z"
        )
        
        while (i + 1 < cleanHex.length) {
            val byteStr = cleanHex.substring(i, i + 2)
            val mapped = mapping[byteStr]
            if (mapped != null) {
                result.append(mapped)
            } else {
                 try {
                     // Fallback for anything not in the map (though the map should be comprehensive based on the script)
                     val b = byteStr.toInt(16)
                     result.append(b.toChar())
                 } catch (e: Exception) {
                     // Ignore
                 }
            }
            i += 2
        }
        return result.toString()
    }


    suspend fun search(query: String): List<AnimeResult> = withContext(Dispatchers.IO) {
        val gql = """query( ${"$"}search: SearchInput ${"$"}limit: Int ${"$"}page: Int ${"$"}translationType: VaildTranslationTypeEnumType ${"$"}countryOrigin: VaildCountryOriginEnumType ) { shows( search: ${"$"}search limit: ${"$"}limit page: ${"$"}page translationType: ${"$"}translationType countryOrigin: ${"$"}countryOrigin ) { edges { _id name availableEpisodes __typename } }}"""
        val variables = """{"search":{"allowAdult":false,"allowUnknown":false,"query":"$query"},"limit":40,"page":1,"translationType":"sub","countryOrigin":"ALL"}"""
        
        val url = "$host?variables=${java.net.URLEncoder.encode(variables, "UTF-8")}&query=${java.net.URLEncoder.encode(gql, "UTF-8")}"
        
        val request = Request.Builder().url(url)
            .header("User-Agent", headers["User-Agent"]!!)
            .header("Referer", headers["Referer"]!!)
            .build()
            
        try {
             client.newCall(request).execute().use { response ->
                 if (!response.isSuccessful) return@withContext emptyList()
                 val json = JSONObject(response.body?.string() ?: "{}")
                 val edges = json.optJSONObject("data")?.optJSONObject("shows")?.optJSONArray("edges") ?: return@withContext emptyList()
                 val results = mutableListOf<AnimeResult>()
                 for (i in 0 until edges.length()) {
                     val item = edges.getJSONObject(i)
                     results.add(AnimeResult(
                         id = item.optString("_id"),
                         title = item.optString("name"),
                         totalEpisodes = item.optJSONObject("availableEpisodes")?.optInt("sub", 0) ?: 0
                     ))
                 }
                 results
             }
        } catch (e: Exception) {
            Log.e("AniCliSource", "Search failed", e)
            emptyList()
        }
    }
    
    suspend fun getEpisodes(animeId: String): List<EpisodeResult> = withContext(Dispatchers.IO) {
        val gql = """query (${"$"}showId: String!) { show( _id: ${"$"}showId ) { _id availableEpisodesDetail }}"""
        val variables = """{"showId":"$animeId"}"""
        val url = "$host?variables=${java.net.URLEncoder.encode(variables, "UTF-8")}&query=${java.net.URLEncoder.encode(gql, "UTF-8")}"
        
         val request = Request.Builder().url(url)
            .header("User-Agent", headers["User-Agent"]!!)
            .header("Referer", headers["Referer"]!!)
            .build()
            
         try {
             client.newCall(request).execute().use { response ->
                 if (!response.isSuccessful) return@withContext emptyList()
                 val json = JSONObject(response.body?.string() ?: "{}")
                 val details = json.optJSONObject("data")?.optJSONObject("show")?.optJSONObject("availableEpisodesDetail") ?: return@withContext emptyList()
                 val sub = details.optJSONArray("sub") ?: return@withContext emptyList()
                 
                 val results = mutableListOf<EpisodeResult>()
                 // sub is array of strings e.g. ["1", "2", "3"]
                 for (i in 0 until sub.length()) {
                     val epNum = sub.getString(i)
                     results.add(EpisodeResult(
                         id = epNum, // for allanime, episodeString is usually the number
                         number = epNum
                     ))
                 }
                 // reverse to show latest first? or sort numeric
                 results.sortedBy { it.number.toFloatOrNull() ?: 0f }
             }
         } catch (e: Exception) {
             Log.e("AniCliSource", "getEpisodes failed", e)
             emptyList()
         }
    }
    
    suspend fun getStreamUrl(animeId: String, episodeNumber: String): StreamResult? = withContext(Dispatchers.IO) {
        val gql = """query (${"$"}showId: String!, ${"$"}translationType: VaildTranslationTypeEnumType!, ${"$"}episodeString: String!) { episode( showId: ${"$"}showId translationType: ${"$"}translationType episodeString: ${"$"}episodeString ) { episodeString sourceUrls }}"""
        val variables = """{"showId":"$animeId","translationType":"sub","episodeString":"$episodeNumber"}"""
        val url = "$host?variables=${java.net.URLEncoder.encode(variables, "UTF-8")}&query=${java.net.URLEncoder.encode(gql, "UTF-8")}"
        
        val request = Request.Builder().url(url)
            .header("User-Agent", headers["User-Agent"]!!)
            .header("Referer", headers["Referer"]!!)
            .build()
            
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                 val json = JSONObject(response.body?.string() ?: "{}")
                 val episode = json.optJSONObject("data")?.optJSONObject("episode") ?: return@withContext null
                 val sourceUrls = episode.optJSONArray("sourceUrls") ?: return@withContext null
                 
                 // Priority Selection with fallback strategy
                 val sourceSelectionOrder = listOf(
                     // Tier 1: Encrypted high-quality sources
                     listOf("Luf-Mp4", "S-mp4", "Yt-mp4"),
                     // Tier 2: Reliable non-encrypted sources
                     listOf("Ok", "Mp4"),
                     // Tier 3: Other sources
                     listOf("Sup", "Uni", "Fm-Hls", "Filemoon", "Streamwish", "Sw")
                 )
                 
                 var streamResult: StreamResult? = null
                 
                 // Try each tier in order
                 for (tierSources in sourceSelectionOrder) {
                     for (sourceName in tierSources) {
                         // Find matching source
                         for (i in 0 until sourceUrls.length()) {
                             val source = sourceUrls.getJSONObject(i)
                             val name = source.optString("sourceName")
                             
                             if (name.contains(sourceName, ignoreCase = true)) {
                                 val sourceUrl = source.optString("sourceUrl")
                                 
                                 // Try to extract stream
                                 if (sourceUrl.startsWith("--")) {
                                     // Encrypted source - decrypt and fetch
                                     try {
                                         val decryptedUrl = decryptSource(sourceUrl)
                                         val clockResult = fetchClockUrl(decryptedUrl)
                                         if (clockResult != null) {
                                             Log.d("AniCliSource", "Successfully extracted stream from $name (encrypted, isMp4=${clockResult.third})")
                                             streamResult = StreamResult(
                                                 url = clockResult.first,
                                                 referer = clockResult.second,
                                                 userAgent = headers["User-Agent"],
                                                 isM3u8 = !clockResult.third  // if it's MP4, then it's NOT M3U8
                                             )
                                             break
                                         }
                                     } catch (e: Exception) {
                                         Log.w("AniCliSource", "Failed to extract from $name: ${e.message}")
                                         // Continue to next source
                                     }
                                 } else if (sourceUrl.startsWith("http")) {
                                     // Non-encrypted direct URL
                                     Log.d("AniCliSource", "Using direct stream from $name")
                                     streamResult = StreamResult(
                                         url = sourceUrl,
                                         referer = "https://allmanga.to",
                                         userAgent = headers["User-Agent"],
                                         isM3u8 = name.contains("hls", ignoreCase = true) || 
                                                   name.contains("m3u8", ignoreCase = true)
                                     )
                                     break
                                 }
                             }
                         }
                         
                         if (streamResult != null) break
                     }
                     
                     if (streamResult != null) break
                 }
                 
                 // Last resort: try any available source
                 if (streamResult == null && sourceUrls.length() > 0) {
                     Log.w("AniCliSource", "All preferred sources failed. Trying first available source.")
                     val source = sourceUrls.getJSONObject(0)
                     val sourceUrl = source.optString("sourceUrl")
                     val sourceName = source.optString("sourceName")
                     
                     if (sourceUrl.startsWith("http")) {
                         streamResult = StreamResult(
                             url = sourceUrl,
                             referer = "https://allmanga.to",
                             userAgent = headers["User-Agent"],
                             isM3u8 = sourceName.contains("hls", ignoreCase = true)
                         )
                     }
                 }
                 
                 streamResult
            }
        } catch (e: Exception) {
            Log.e("AniCliSource", "getStreamUrl failed", e)
            null // Return null on exception
        }
    }

    // Returns Triple(StreamUrl, Referer, isMp4)
    private suspend fun fetchClockUrl(url: String): Triple<String, String, Boolean>? = withContext(Dispatchers.IO) {
         var targetUrl = url
         if (url.startsWith("/")) {
             targetUrl = "https://allanime.day$url"
         } else if (!url.startsWith("http")) {
             targetUrl = "https://$url"
         }
         
         // ani-cli line 171: s/\/clock/\/clock\.json/
         targetUrl = targetUrl.replace("/clock", "/clock.json")
         
         Log.d("AniCliSource", "Fetching JSON from: $targetUrl")

         val request = Request.Builder().url(targetUrl)
            .header("User-Agent", headers["User-Agent"]!!)
            .header("Referer", headers["Referer"]!!)
            .build()
            
         try {
             client.newCall(request).execute().use { response ->
                 if(!response.isSuccessful) {
                     Log.e("AniCliSource", "Fetch JSON failed: ${response.code} for $targetUrl")
                     return@withContext null
                 }
                 val body = response.body?.string() ?: return@withContext null
                 val json = JSONObject(body)
                 val links = json.optJSONArray("links")
                 
                 // Check for dynamic referer in the JSON (ani-cli line 152: m3u8_refr)
                 // Note: ani-cli looks for "Referer" but lowercase in JSON usually? 
                 // It grep-s for 'Referer":"([^"]*)"'. 
                 // Let's check for both "Referer" and any other likely key.
                 // ani-cli uses: sed -nE 's|.*Referer":"([^"]*)".*|\1|p'
                 // This implies it's looking for the key "Referer" specifically.
                 // However, standard JSON keys are usually lowercase "referrer" or case-sensitive "Referer".
                 // We'll check the top-level keys safely.
                 
                 // Note: ani-cli parses the RAW response string for "Referer".
                 // In standard JSON parsing using JSONObject, we should look keys.
                 // But if the JSON structure is flat, we can just check optString("Referer").
                 
                 var dynamicReferer = headers["Referer"]!!
                 if (body.contains("Referer")) {
                      // Manual regex or try to find it in the object?
                      // The JSON structure for these sources isn't fully documented here, but let's trust JSONObject
                      // If it's a top level key:
                      val explicitRef = json.optString("Referer")
                      if (explicitRef.isNotEmpty()) {
                          dynamicReferer = explicitRef
                      }
                 }

                 if (links != null && links.length() > 0) {
                     // Extract the link field from the first item
                     val linkObj = links.getJSONObject(0)
                     val streamUrl = linkObj.optString("link")
                     val isMp4 = linkObj.optBoolean("mp4", false)
                     
                     Log.d("AniCliSource", "Extracted stream: $streamUrl (isMp4=$isMp4)")
                     return@withContext Triple(streamUrl, dynamicReferer, isMp4)
                 }
                 Log.e("AniCliSource", "No links found in JSON")
                 null
             }
         } catch (e: Exception) {
             Log.e("AniCliSource", "fetchClock failed", e)
             null
         }
    }

    data class AnimeResult(val id: String, val title: String, val totalEpisodes: Int)
    data class EpisodeResult(val id: String, val number: String)
    data class StreamResult(
        val url: String, 
        val referer: String?, 
        val userAgent: String?, 
        val isM3u8: Boolean = true
    )
}

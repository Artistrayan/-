package com.example.engine

import com.example.BuildConfig
import com.example.model.GameState
import com.example.model.PlayerColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiAssistant {

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun getMatchCommentary(state: GameState): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateFallbackCommentary(state)
        }

        val prompt = """
            You are a charismatic, legendary Grandmaster Backgammon (Takhte Nard) commentator in Persian and English.
            Analyze the current board state:
            - White Pip Count: ${state.whitePipCount} (Borne off: ${state.offWhite}, On Bar: ${state.barWhite})
            - Black Pip Count: ${state.blackPipCount} (Borne off: ${state.offBlack}, On Bar: ${state.barBlack})
            - Current Turn: ${state.currentTurn}
            - Doubling Cube: ${state.doublingCubeValue}x

            Give a short (1-2 sentences), dramatic, encouraging commentary with emojis in English with Persian flair.
        """.trimIndent()

        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("$API_URL?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val respStr = response.body?.string() ?: ""
                    val jsonResp = JSONObject(respStr)
                    val text = jsonResp.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                    text.trim()
                } else {
                    generateFallbackCommentary(state)
                }
            }
        } catch (e: Exception) {
            generateFallbackCommentary(state)
        }
    }

    private fun generateFallbackCommentary(state: GameState): String {
        val diff = state.whitePipCount - state.blackPipCount
        return when {
            state.barWhite > 0 -> "⚠️ White is on the Bar! Needs a swift re-entry to stay in the match!"
            state.barBlack > 0 -> "🎯 Black checker hit! White has taken the tactical upper hand!"
            diff < -15 -> "🔥 White is leading the race by ${-diff} pips! Excellent tempo!"
            diff > 15 -> "⚡ Black holds a strong pip lead by $diff pips! Precision moves needed!"
            state.doublingCubeValue > 1 -> "💥 Stakes escalated to ${state.doublingCubeValue}X! Double or nothing!"
            else -> "🎲 High-stakes Takhte Nard match underway! Every roll counts!"
        }
    }
}

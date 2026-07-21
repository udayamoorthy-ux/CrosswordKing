package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Attempts to fetch a word's dictionary definition using the Gemini 3.5 Flash API.
     * If the API key is missing or the call fails, it falls back to the offline dictionary or
     * returns a placeholder indicating the API key can be set in the AI Studio Secrets panel.
     */
    suspend fun fetchDefinition(word: String): WordDefinition = withContext(Dispatchers.IO) {
        val uppercaseWord = word.trim().uppercase()

        // 1. Check if we have an offline definition pre-configured for game words
        val offlineData = LevelsData.offlineMeanings[uppercaseWord]
        
        // 2. Try to query Gemini API if API key is present
        val apiKey = try {
            com.example.BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNullOrEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // No API key -> Return offline data if available, or a friendly guide
            if (offlineData != null) {
                return@withContext WordDefinition(
                    word = uppercaseWord,
                    meaning = offlineData.first,
                    partOfSpeech = offlineData.second,
                    example = offlineData.third,
                    source = "Offline Dictionary"
                )
            } else {
                return@withContext WordDefinition(
                    word = uppercaseWord,
                    meaning = "Enter your Gemini API key in the AI Studio secrets panel to dynamically generate dictionary definitions for any English word!",
                    partOfSpeech = "Configure API Key",
                    example = "Configure GEMINI_API_KEY in the Secrets panel in AI Studio sidebar.",
                    source = "Setup Guide"
                )
            }
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val systemPrompt = """
            You are a precise English dictionary. Provide a complete definition for the English word '$uppercaseWord'.
            You MUST return a JSON object with these exact string fields:
            {
               "word": "$uppercaseWord",
               "meaning": "Clear, concise definition of the word",
               "partOfSpeech": "Noun/Verb/Adjective/Adverb/etc.",
               "example": "A short, illustrative example sentence using the word"
            }
            Do NOT wrap the output in markdown code blocks or triple backticks. Return ONLY the raw JSON string.
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemPrompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.1)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonRequest.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("API returned code ${response.code}")
                }
                val responseBody = response.body?.string() ?: throw Exception("Empty response body")
                
                val rootJson = JSONObject(responseBody)
                val candidates = rootJson.getJSONArray("candidates")
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                val textResponse = parts.getJSONObject(0).getString("text")

                // Parse the inner JSON produced by Gemini
                val innerJson = JSONObject(textResponse.trim())
                return@withContext WordDefinition(
                    word = innerJson.optString("word", uppercaseWord).uppercase(),
                    meaning = innerJson.optString("meaning", "No meaning returned."),
                    partOfSpeech = innerJson.optString("partOfSpeech", "Unknown"),
                    example = innerJson.optString("example", "No example sentence available."),
                    source = "Gemini AI"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback on error (offline or default instruction)
            if (offlineData != null) {
                return@withContext WordDefinition(
                    word = uppercaseWord,
                    meaning = offlineData.first,
                    partOfSpeech = offlineData.second,
                    example = offlineData.third,
                    source = "Offline Fallback"
                )
            } else {
                return@withContext WordDefinition(
                    word = uppercaseWord,
                    meaning = "Failed to connect to AI server. Check your connection or API Key.",
                    partOfSpeech = "Error",
                    example = "Error details: ${e.message}",
                    source = "Error Fallback"
                )
            }
        }
    }
}

data class WordDefinition(
    val word: String,
    val meaning: String,
    val partOfSpeech: String,
    val example: String,
    val source: String
)

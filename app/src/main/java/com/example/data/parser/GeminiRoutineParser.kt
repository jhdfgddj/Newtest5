package com.example.data.parser

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.DataQualityStatus
import com.example.data.model.Routine
import com.example.data.model.RoutineEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class GeminiRoutineParser {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun isApiKeyAvailable(): Boolean {
        val apiKey = BuildConfig.GEMINI_API_KEY
        return !apiKey.isNullOrBlank() && !apiKey.startsWith("MY_GEMINI") && apiKey != "null"
    }

    suspend fun parseWithGemini(extractedText: String, defaultTitle: String): Pair<Routine, List<RoutineEntry>>? = withContext(Dispatchers.IO) {
        if (!isApiKeyAvailable()) return@withContext null

        val apiKey = BuildConfig.GEMINI_API_KEY

        val prompt = """
            You are an expert exam routine document parser.
            Parse the following raw extracted text from an exam routine PDF into valid structured JSON matching this EXACT schema:
            {
              "title": "Exam Routine Title",
              "organization": "Organization Name",
              "noticeNumber": "Notice/Ref number",
              "publicationDate": "DD-MM-YYYY",
              "examSession": "Session Name",
              "regulation": "2022 Regulation",
              "entries": [
                {
                  "date": "YYYY-MM-DD",
                  "day": "Day of week e.g. Saturday",
                  "time": "10:00 AM or 02:00 PM",
                  "session": "Morning or Afternoon",
                  "semester": ["4th", "6th"],
                  "subjectCode": "26811",
                  "subjectName": "Electrical Engineering Fundamentals",
                  "technology": ["Electrical", "Computer"],
                  "regulation": "2022 Regulation",
                  "rawText": "original string"
                }
              ]
            }
            
            Extract ALL exam subjects, dates, times, subject codes, semesters, and technologies accurately.
            Never invent subject codes or dates.
            Format dates strictly as YYYY-MM-DD.
            
            Routine Raw Text:
            $extractedText
        """.trimIndent()

        try {
            val partObj = JSONObject().put("text", prompt)
            val partsArr = JSONArray().put(partObj)
            val contentObj = JSONObject().put("parts", partsArr)
            val contentsArr = JSONArray().put(contentObj)

            val reqBodyJson = JSONObject()
                .put("contents", contentsArr)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

            val httpRequest = Request.Builder()
                .url(url)
                .post(reqBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(httpRequest).execute()
            if (!response.isSuccessful) {
                Log.e("GeminiParser", "API call failed with code: ${response.code}")
                return@withContext null
            }

            val responseStr = response.body?.string() ?: return@withContext null
            val respObj = JSONObject(responseStr)
            val candidates = respObj.optJSONArray("candidates") ?: return@withContext null
            val firstCand = candidates.optJSONObject(0) ?: return@withContext null
            val content = firstCand.optJSONObject("content") ?: return@withContext null
            val parts = content.optJSONArray("parts") ?: return@withContext null
            var rawJsonText = parts.optJSONObject(0)?.optString("text") ?: return@withContext null

            // Clean markdown codeblocks if wrapped in ```json ... ```
            rawJsonText = rawJsonText.trim()
            if (rawJsonText.startsWith("```")) {
                rawJsonText = rawJsonText.substringAfter("\n").substringBeforeLast("```").trim()
            }

            val parsedJson = JSONObject(rawJsonText)

            val routineId = "routine_" + UUID.randomUUID().toString().take(8)
            val routine = Routine(
                id = routineId,
                title = parsedJson.optString("title").ifBlank { defaultTitle },
                organization = parsedJson.optString("organization").ifBlank { "Bangladesh Technical Education Board" },
                noticeNumber = parsedJson.optString("noticeNumber"),
                publicationDate = parsedJson.optString("publicationDate"),
                examSession = parsedJson.optString("examSession"),
                regulation = parsedJson.optString("regulation").ifBlank { "2022 Regulation" }
            )

            val entriesArray = parsedJson.optJSONArray("entries")
            val entries = mutableListOf<RoutineEntry>()

            if (entriesArray != null) {
                for (i in 0 until entriesArray.length()) {
                    val item = entriesArray.optJSONObject(i) ?: continue

                    val semArr = item.optJSONArray("semester")
                    val semesters = mutableListOf<String>()
                    if (semArr != null) {
                        for (s in 0 until semArr.length()) {
                            semesters.add(semArr.optString(s))
                        }
                    }

                    val techArr = item.optJSONArray("technology")
                    val technologies = mutableListOf<String>()
                    if (techArr != null) {
                        for (t in 0 until techArr.length()) {
                            technologies.add(techArr.optString(t))
                        }
                    }

                    val subjectCode = item.optString("subjectCode")
                    val subjectName = item.optString("subjectName")

                    entries.add(
                        RoutineEntry(
                            id = UUID.randomUUID().toString(),
                            routineId = routineId,
                            date = item.optString("date").ifBlank { "2026-08-08" },
                            day = item.optString("day").ifBlank { "Saturday" },
                            time = item.optString("time").ifBlank { "10:00 AM" },
                            session = item.optString("session").ifBlank { "Morning" },
                            semester = semesters.ifEmpty { listOf("4th") },
                            subjectCode = subjectCode.ifBlank { "00000" },
                            subjectName = subjectName.ifBlank { "Unknown Subject" },
                            technology = technologies.ifEmpty { listOf("General") },
                            regulation = item.optString("regulation").ifBlank { routine.regulation },
                            examSession = routine.examSession,
                            rawText = item.optString("rawText"),
                            status = if (subjectCode.length >= 4 && subjectName.isNotBlank()) DataQualityStatus.VERIFIED else DataQualityStatus.NEEDS_REVIEW
                        )
                    )
                }
            }

            if (entries.isEmpty()) return@withContext null

            return@withContext Pair(routine, entries)
        } catch (e: Exception) {
            Log.e("GeminiParser", "Error parsing with Gemini: ${e.localizedMessage}", e)
            return@withContext null
        }
    }
}

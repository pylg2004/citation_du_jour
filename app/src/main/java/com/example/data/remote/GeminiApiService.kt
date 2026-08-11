package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.QuizQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiApiService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    suspend fun generateQuiz(theme: String, count: Int, difficulty: String): List<QuizQuestion> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("GeminiApiService", "Gemini API Key missing or default, using fallback generator.")
            return@withContext getFallbackQuiz(theme, count)
        }

        val prompt = """
            Génère exactement $count questions de quizz à choix multiples sur le thème "$theme" (difficulté: $difficulty).
            Réponds uniquement avec un objet JSON respectant ce format :
            {
              "questions": [
                {
                  "question": "Question ici ?",
                  "options": ["Choix A", "Choix B", "Choix C", "Choix D"],
                  "correctAnswerIndex": 0,
                  "explanation": "Explication courte..."
                }
              ]
            }
            Assure-toi d'avoir exactement 4 choix par question, et que correctAnswerIndex soit entre 0 et 3.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            })
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                Log.e("GeminiApiService", "API Error HTTP ${response.code}: $bodyString")
                return@withContext getFallbackQuiz(theme, count)
            }

            val jsonResponse = JSONObject(bodyString)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val responseText = parts?.optJSONObject(0)?.optString("text") ?: ""

            parseQuizJson(responseText, count)
        } catch (e: Exception) {
            Log.e("GeminiApiService", "Error calling Gemini API for quiz", e)
            getFallbackQuiz(theme, count)
        }
    }

    suspend fun sendChatMessage(userPrompt: String, history: List<Pair<String, String>>): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Je suis votre assistant IA cdj_ia. Pour activer le chat IA dynamique en direct, configurez votre clé API Gemini dans le panneau Secrets de l'application !\n\nEn attendant, je peux répondre hors-ligne aux questions courantes sur les citations, la philosophie et la culture générale !"
        }

        val contentsArray = JSONArray()

        // System Instruction
        val systemInstructionText = "Tu es cdj_ia, un assistant IA intelligent, courtois, inspirant et cultivé, spécialisé en citations, philosophie, poésie et culture générale. Réponds de façon élégante, claire et bien structurée en français."

        // Add history turns (limit last 10 for context)
        val recentHistory = history.takeLast(10)
        for ((sender, text) in recentHistory) {
            val role = if (sender == "USER") "user" else "model"
            contentsArray.put(JSONObject().apply {
                put("role", role)
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", text) })
                })
            })
        }

        // Add current prompt
        contentsArray.put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().apply {
                put(JSONObject().apply { put("text", userPrompt) })
            })
        })

        val jsonBody = JSONObject().apply {
            put("contents", contentsArray)
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstructionText) })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
            })
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext "Désolé, une erreur de communication est survenue (${response.code}). Veuillez réessayer."
            }

            val jsonResponse = JSONObject(bodyString)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            parts?.optJSONObject(0)?.optString("text") ?: "Aucune réponse générée."
        } catch (e: Exception) {
            Log.e("GeminiApiService", "Error calling Gemini Chat", e)
            "Réseau indisponible. Vérifiez votre connexion internet."
        }
    }

    private fun parseQuizJson(jsonText: String, requestedCount: Int): List<QuizQuestion> {
        val list = mutableListOf<QuizQuestion>()
        try {
            val cleanJson = jsonText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val root = JSONObject(cleanJson)
            val questionsArray = root.optJSONArray("questions") ?: JSONArray()

            for (i in 0 until questionsArray.length()) {
                val qObj = questionsArray.getJSONObject(i)
                val qText = qObj.optString("question", "Question ${i + 1}")
                val optsArray = qObj.optJSONArray("options") ?: JSONArray()
                val optionsList = mutableListOf<String>()
                for (j in 0 until optsArray.length()) {
                    optionsList.add(optsArray.getString(j))
                }
                while (optionsList.size < 4) {
                    optionsList.add("Option ${optionsList.size + 1}")
                }
                val correctIndex = qObj.optInt("correctAnswerIndex", 0).coerceIn(0, 3)
                val explanation = qObj.optString("explanation", "Bonne réponse !")

                list.add(
                    QuizQuestion(
                        id = i + 1,
                        question = qText,
                        options = optionsList.take(4),
                        correctAnswerIndex = correctIndex,
                        explanation = explanation
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("GeminiApiService", "Failed to parse quiz json: $jsonText", e)
        }

        if (list.isEmpty()) {
            return getFallbackQuiz("Culture Générale", requestedCount)
        }
        return list
    }

    fun getFallbackQuiz(theme: String, count: Int): List<QuizQuestion> {
        val pool = when (theme.lowercase()) {
            "géographie" -> listOf(
                QuizQuestion(1, "Quelle est la capitale du Japon ?", listOf("Tokyo", "Kyoto", "Osaka", "Nagoya"), 0, "Tokyo est la capitale et plus grande métropole du Japon."),
                QuizQuestion(2, "Quel est le plus long fleuve du monde ?", listOf("Le Nil", "L'Amazone", "Le Mississippi", "Le Yangtze"), 1, "L'Amazone est considéré comme le fleuve le plus long et puissant du monde."),
                QuizQuestion(3, "Combien de continents compte la Terre selon le modèle standard ?", listOf("5", "6", "7", "8"), 2, "Les 7 continents sont l'Asie, l'Afrique, l'Amérique du Nord, l'Amérique du Sud, l'Antarctique, l'Europe et l'Océanie."),
                QuizQuestion(4, "Quel pays possède le plus grand nombre d'îles au monde ?", listOf("Indonésie", "Philippines", "Suède", "Grèce"), 2, "La Suède compte plus de 267 000 îles !"),
                QuizQuestion(5, "Quel est le point le plus bas de la Terre à la surface des terres émergées ?", listOf("La Vallée de la Mort", "La Mer Morte", "Le Lac Baïkal", "La Fosse des Mariannes"), 1, "La rive de la Mer Morte se situe à -430 mètres sous le niveau de la mer.")
            )
            "histoire" -> listOf(
                QuizQuestion(1, "En quelle année a eu lieu la Révolution Française ?", listOf("1789", "1799", "1804", "1815"), 0, "La Révolution Française a débuté en 1789 avec la prise de la Bastille."),
                QuizQuestion(2, "Qui était le premier empereur romain ?", listOf("Jules César", "Auguste", "Néron", "Marc Aurèle"), 1, "Octave est devenu Auguste, premier empereur romain en 27 av. J.-C."),
                QuizQuestion(3, "Quelle civilisation a construit les pyramides de Gizeh ?", listOf("Les Mayas", "Les Incas", "Les Égyptiens", "Les Sumériens"), 2, "Les Égyptiens de l'Ancien Empire ont édifié les Pyramides de Gizeh."),
                QuizQuestion(4, "En quelle année la chute du mur de Berlin s'est-elle produite ?", listOf("1987", "1989", "1991", "1993"), 1, "Le mur de Berlin est tombé le 9 novembre 1989."),
                QuizQuestion(5, "Qui a été surnommé le 'Roi-Soleil' ?", listOf("Louis XIV", "Louis XV", "François 1er", "Henri IV"), 0, "Louis XIV régna de 1643 à 1715 sous le symbole du Soleil.")
            )
            "cinéma" -> listOf(
                QuizQuestion(1, "Qui a réalisé le film 'Inception' et 'Interstellar' ?", listOf("Steven Spielberg", "Christopher Nolan", "Quentin Tarantino", "Denis Villeneuve"), 1, "Christopher Nolan est célèbre pour ses thrillers de science-fiction."),
                QuizQuestion(2, "Quel film a remporté le premier Oscar du meilleur film d'animation en 2002 ?", listOf("Shrek", "Monstres et Cie", "Toy Story", "Le Voyage de Chihiro"), 0, "Shrek a remporté le tout premier Oscar de cette catégorie."),
                QuizQuestion(3, "Quel acteur incarne Iron Man dans le Marvel Cinematic Universe ?", listOf("Chris Evans", "Robert Downey Jr.", "Chris Hemsworth", "Mark Ruffalo"), 1, "Robert Downey Jr. interprète Tony Stark/Iron Man."),
                QuizQuestion(4, "Combien d'Oscars le film 'Titanic' (1997) a-t-il remportés ?", listOf("9", "10", "11", "12"), 2, "Titanic a égalé le record de 11 Oscars remportés."),
                QuizQuestion(5, "Quel est le nom de la planète natale de Luke Skywalker dans Star Wars ?", listOf("Tatooine", "Alderaan", "Naboo", "Coruscant"), 0, "Tatooine est la planète désertique aux deux soleils.")
            )
            else -> listOf(
                QuizQuestion(1, "Quel est l'élément chimique symbolisé par 'Au' ?", listOf("Argent", "Or", "Cuivre", "Aluminium"), 1, "Au vient du latin 'Aurum' qui signifie Or."),
                QuizQuestion(2, "Qui a peint 'La Joconde' ?", listOf("Léonard de Vinci", "Claude Monet", "Pablo Picasso", "Vincent van Gogh"), 0, "Léonard de Vinci a réalisé La Joconde entre 1503 et 1519."),
                QuizQuestion(3, "Quelle planète est surnommée la 'Planète Rouge' ?", listOf("Vénus", "Jupiter", "Mars", "Saturne"), 2, "Mars doit sa couleur rouge aux oxydes de fer à sa surface."),
                QuizQuestion(4, "Quel est l'auteur du roman 'Les Misérables' ?", listOf("Émile Zola", "Victor Hugo", "Gustave Flaubert", "Honoré de Balzac"), 1, "Victor Hugo a publié Les Misérables en 1862."),
                QuizQuestion(5, "Combien de joues possède la Joconde ?", listOf("1", "2", "3", "0"), 1, "La Joconde a bien 2 joues !")
            )
        }

        val result = mutableListOf<QuizQuestion>()
        var idCounter = 1
        while (result.size < count) {
            val q = pool[(result.size) % pool.size]
            result.add(q.copy(id = idCounter++))
        }
        return result
    }
}

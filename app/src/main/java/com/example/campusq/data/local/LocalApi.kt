package com.example.campusq.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI

class LocalApiException(val status: Int, val code: String, override val message: String) : IOException(message)

/** Platform TLS validation is retained. No redirects, request logs or Firebase dependency. */
class LocalApi(val baseUrl: String, private val token: () -> String?) {
    init {
        val uri = URI(baseUrl)
        require(uri.scheme in setOf("http", "https") && !uri.host.isNullOrBlank() &&
            uri.userInfo == null && uri.query == null && uri.fragment == null &&
            uri.path.orEmpty() in setOf("", "/")) { "Configure a valid backend origin." }
    }

    suspend fun request(method: String, path: String, body: JSONObject? = null, requestKey: String? = null): JSONObject {
        val requestCredential = token()
        return withContext(Dispatchers.IO) {
            require(path.startsWith("/api/") || path == "/health")
            val connection = URI(baseUrl.trimEnd('/') + path).toURL().openConnection() as HttpURLConnection
            try {
                connection.requestMethod = method
                connection.connectTimeout = 8_000
                connection.readTimeout = 15_000
                connection.instanceFollowRedirects = false
                connection.setRequestProperty("Accept", "application/json")
                requestCredential?.let { connection.setRequestProperty("Authorization", "Bearer $it") }
                requestKey?.let { connection.setRequestProperty("Idempotency-Key", it) }
                if (body != null) {
                    connection.doOutput = true
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                }
                val status = connection.responseCode
                val stream = if (status in 200..299) connection.inputStream else connection.errorStream
                val bytes = stream?.use { input ->
                    val output = java.io.ByteArrayOutputStream()
                    val buffer = ByteArray(8_192)
                    var count = input.read(buffer)
                    while (count != -1) {
                        if (output.size() + count > 4_000_000) throw IOException("Response is too large.")
                        output.write(buffer, 0, count)
                        count = input.read(buffer)
                    }
                    output.toByteArray()
                } ?: ByteArray(0)
                val json = try { JSONObject(bytes.toString(Charsets.UTF_8)) }
                catch (_: Exception) { throw IOException("The server returned an invalid response.") }
                if (status !in 200..299) {
                    val error = json.optJSONObject("error")
                    throw LocalApiException(status, error?.optString("code") ?: "REQUEST_FAILED",
                        error?.optString("message") ?: "The request was not completed.")
                }
                json
            } finally { connection.disconnect() }
        }
    }
}

fun jsonBody(vararg entries: Pair<String, Any?>): JSONObject =
    JSONObject().apply { entries.forEach { (key, value) -> put(key, value ?: JSONObject.NULL) } }


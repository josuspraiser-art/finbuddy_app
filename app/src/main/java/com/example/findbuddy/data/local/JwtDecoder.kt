package com.example.findbuddy.data.local

object JwtDecoder {
    fun getUserIdFromToken(token: String): String {
        return try {
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payloadString = decodeBase64Url(parts[1])
                // Extract "sub" claim from JSON string using regex to avoid JSON library stub errors in JVM unit tests
                val subRegex = "\"sub\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                val subMatch = subRegex.find(payloadString)
                if (subMatch != null) {
                    return subMatch.groupValues[1]
                }
                
                val userIdRegex = "\"userId\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                val userIdMatch = userIdRegex.find(payloadString)
                if (userIdMatch != null) {
                    return userIdMatch.groupValues[1]
                }
                
                "default_user"
            } else {
                "default_user"
            }
        } catch (e: Exception) {
            "default_user"
        }
    }

    private fun decodeBase64Url(base64Url: String): String {
        var normalized = base64Url.replace('-', '+').replace('_', '/')
        while (normalized.length % 4 != 0) {
            normalized += "="
        }
        val bytes = decodeBase64(normalized)
        return String(bytes, Charsets.UTF_8)
    }

    private fun decodeBase64(base64: String): ByteArray {
        val table = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        val clean = base64.filter { it in table || it == '=' }
        val output = mutableListOf<Byte>()
        var buffer = 0
        var bitsCollected = 0
        var paddingCount = 0

        for (char in clean) {
            if (char == '=') {
                paddingCount++
                continue
            }
            if (paddingCount > 0) break
            val value = table.indexOf(char)
            buffer = (buffer shl 6) or value
            bitsCollected += 6
            if (bitsCollected >= 8) {
                bitsCollected -= 8
                output.add(((buffer shr bitsCollected) and 0xFF).toByte())
            }
        }
        return output.toByteArray()
    }
}

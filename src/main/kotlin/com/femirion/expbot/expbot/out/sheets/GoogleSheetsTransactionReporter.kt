package com.femirion.expbot.expbot.out.sheets

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.femirion.expbot.expbot.domain.entity.MoneyTransaction
import com.femirion.expbot.expbot.domain.service.MoneyTransactionReporter
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.util.Base64
import java.util.logging.Logger

@Service
class GoogleSheetsTransactionReporter(
    @Value("\${expbot.google-sheets.enabled:false}")
    private val enabled: Boolean,
    @Value("\${expbot.google-sheets.spreadsheet-id:}")
    private val spreadsheetId: String,
    @Value("\${expbot.google-sheets.range:Transactions!A:I}")
    private val range: String,
    @Value("\${expbot.google-sheets.service-account-json:}")
    private val serviceAccountJson: String,
) : MoneyTransactionReporter {
    private val restClient = RestClient.create()
    private val objectMapper = ObjectMapper()

    override fun report(transaction: MoneyTransaction) {
        if (!enabled) {
            return
        }
        if (spreadsheetId.isBlank() || serviceAccountJson.isBlank()) {
            log.warning { "Google Sheets reporting is enabled but spreadsheet id or service account json is missing" }
            return
        }

        runCatching {
            val accessToken = getAccessToken()
            appendRow(accessToken, transaction)
        }.onFailure { error ->
            log.warning { "Failed to report transaction to Google Sheets: ${error.message}" }
        }
    }

    private fun getAccessToken(): String {
        val credentials = objectMapper.readValue(serviceAccountJson, ServiceAccountCredentials::class.java)
        val tokenUri = credentials.tokenUri.ifBlank { GOOGLE_TOKEN_URI }
        val now = Instant.now().epochSecond
        val header = mapOf("alg" to "RS256", "typ" to "JWT")
        val claims = mapOf(
            "iss" to credentials.clientEmail,
            "scope" to "https://www.googleapis.com/auth/spreadsheets",
            "aud" to tokenUri,
            "iat" to now,
            "exp" to now + 3600,
        )
        val unsignedJwt = "${base64Url(objectMapper.writeValueAsBytes(header))}.${base64Url(objectMapper.writeValueAsBytes(claims))}"
        val jwt = "$unsignedJwt.${sign(unsignedJwt, credentials.privateKey)}"
        val form = LinkedMultiValueMap<String, String>()
        form.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
        form.add("assertion", jwt)

        return restClient.post()
            .uri(tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(TokenResponse::class.java)
            ?.accessToken
            ?: error("Google token response did not contain access_token")
    }

    private fun appendRow(accessToken: String, transaction: MoneyTransaction) {
        val values = listOf(
            listOf(
                transaction.occurredAt.toString(),
                transaction.type.name,
                transaction.category.code,
                transaction.category.name,
                transaction.amount.toPlainString(),
                transaction.note.orEmpty(),
                transaction.telegramUserId.toString(),
                transaction.chatId.toString(),
                transaction.telegramMessageId.toString(),
            )
        )

        restClient.post()
            .uri(
                "https://sheets.googleapis.com/v4/spreadsheets/{spreadsheetId}/values/{range}:append?valueInputOption=USER_ENTERED&insertDataOption=INSERT_ROWS",
                spreadsheetId,
                range,
            )
            .header("Authorization", "Bearer $accessToken")
            .body(mapOf("values" to values))
            .retrieve()
            .toBodilessEntity()
    }

    private fun sign(payload: String, privateKeyPem: String): String {
        val privateKeyBytes = privateKeyPem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")
            .let { Base64.getDecoder().decode(it) }
        val privateKey = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(payload.toByteArray(Charsets.UTF_8))
        return base64Url(signature.sign())
    }

    private fun base64Url(bytes: ByteArray): String {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    companion object {
        private const val GOOGLE_TOKEN_URI = "https://oauth2.googleapis.com/token"
        private val log: Logger = Logger.getLogger(GoogleSheetsTransactionReporter::class.java.name)
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ServiceAccountCredentials(
    @JsonProperty("client_email")
    val clientEmail: String,
    @JsonProperty("private_key")
    val privateKey: String,
    @JsonProperty("token_uri")
    val tokenUri: String = "",
)

data class TokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
)

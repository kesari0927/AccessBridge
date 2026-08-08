package org.accessbridge.domain.repository

fun interface TextRecognitionRepository {
    suspend fun recognize(sharedImageUri: String): String
}

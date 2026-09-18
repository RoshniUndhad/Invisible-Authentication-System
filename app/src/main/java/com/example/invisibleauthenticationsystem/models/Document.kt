package com.example.invisibleauthenticationsystem.models

data class Document(
    val id: Long = -1,
    val title: String,
    val uriString: String,
    val mimeType: String
)

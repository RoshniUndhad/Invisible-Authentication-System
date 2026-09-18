package com.example.invisibleauthenticationsystem.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.FileProvider
import com.example.invisibleauthenticationsystem.databinding.ItemDocumentBinding
import com.example.invisibleauthenticationsystem.models.Document
import com.example.invisibleauthenticationsystem.utils.CryptoManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class DocumentAdapter(
    private val context: Context,
    private val documentList: MutableList<Document>,
    private val onDeleteClick: (Document, Int) -> Unit
) : RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder>() {
    
    private val cryptoManager = CryptoManager()

    inner class DocumentViewHolder(val binding: ItemDocumentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val binding = ItemDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DocumentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        val doc = documentList[position]
        holder.binding.tvDocName.text = doc.title
        
        holder.itemView.setOnClickListener {
            try {
                // 1. Read encrypted file
                val encryptedFile = File(doc.uriString)
                val decryptedBytes = FileInputStream(encryptedFile).use {
                    cryptoManager.decrypt(it)
                }

                // 2. Write to temporary decrypted cache file
                val tempFile = File(context.cacheDir, "decrypted_${doc.title}")
                FileOutputStream(tempFile).use {
                    it.write(decryptedBytes)
                }

                // 3. Expose the temporary file through FileProvider securely
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile
                )

                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, doc.mimeType)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(intent)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        holder.binding.btnDeleteDoc.setOnClickListener {
            onDeleteClick(doc, holder.adapterPosition)
        }
    }

    override fun getItemCount() = documentList.size
}

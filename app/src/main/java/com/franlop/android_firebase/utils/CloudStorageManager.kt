package com.franlop.android_firebase.utils

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await

/** Sube y lista las fotos del usuario en Firebase Cloud Storage (photos/<uid>/...). */
class CloudStorageManager(private val uid: String) {

    private val storageRef = FirebaseStorage.getInstance().reference

    private fun getStorageReference(): StorageReference =
        storageRef.child("photos").child(uid)

    suspend fun uploadFile(fileName: String, filePath: Uri) {
        getStorageReference().child(fileName).putFile(filePath).await()
    }

    suspend fun getUserImages(): List<String> {
        val listResult = getStorageReference().listAll().await()
        return listResult.items.map { item ->
            item.downloadUrl.await().toString()
        }
    }
}

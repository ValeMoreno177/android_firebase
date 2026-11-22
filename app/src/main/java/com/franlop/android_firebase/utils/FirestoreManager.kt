package com.franlop.android_firebase.utils

import com.franlop.android_firebase.model.Note
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** CRUD de notas con Cloud Firestore. */
class FirestoreManager(private val uid: String) {

    private val notesCollection = FirebaseFirestore.getInstance().collection("notes")

    suspend fun addNote(note: Note) {
        val doc = notesCollection.document()
        doc.set(note.copy(id = doc.id, userId = uid)).await()
    }

    suspend fun updateNote(note: Note) {
        val id = note.id ?: return
        notesCollection.document(id).set(note.copy(userId = uid)).await()
    }

    suspend fun deleteNote(noteId: String) {
        notesCollection.document(noteId).delete().await()
    }

    /** Devuelve en tiempo real las notas del usuario actual. */
    fun getNotesFlow(): Flow<List<Note>> = callbackFlow {
        val registration = notesCollection
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Note::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(notes)
            }
        awaitClose { registration.remove() }
    }
}

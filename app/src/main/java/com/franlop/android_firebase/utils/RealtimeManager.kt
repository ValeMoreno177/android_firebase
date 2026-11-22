package com.franlop.android_firebase.utils

import com.franlop.android_firebase.model.Contact
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** CRUD de contactos con Firebase Realtime Database. */
class RealtimeManager {

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("contacts")

    fun addContact(contact: Contact) {
        val key = databaseReference.push().key
        if (key != null) {
            databaseReference.child(key).setValue(contact.copy(key = key))
        }
    }

    fun updateContact(contact: Contact) {
        val key = contact.key ?: return
        databaseReference.child(key).setValue(contact)
    }

    fun deleteContact(contactKey: String) {
        databaseReference.child(contactKey).removeValue()
    }

    /** Devuelve en tiempo real los contactos del usuario [uid]. */
    fun getContactsFlow(uid: String): Flow<List<Contact>> = callbackFlow {
        val query = databaseReference.orderByChild("uid").equalTo(uid)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contacts = snapshot.children.mapNotNull { child ->
                    child.getValue(Contact::class.java)?.copy(key = child.key)
                }
                trySend(contacts)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }
}

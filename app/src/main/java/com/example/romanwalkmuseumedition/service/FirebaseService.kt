package com.example.romanwalkmuseumedition.service

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.romanwalkmuseumedition.model.Code
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import javax.security.auth.callback.Callback


class FirebaseService(appContext: Context) {
    private val firestoreDB : FirebaseFirestore = FirebaseFirestore.getInstance()
    val context = appContext

    fun downloadCodes(museumID: String, projectID: String, callback: (HashMap<String, Code>) -> Unit) {
        var codes: HashMap<String, Code> = HashMap<String, Code>()
        var codeReference = firestoreDB.collection("Organisation")
            .document(museumID)
            .collection("Project")
            .document(projectID)
            .collection("Code")
        codeReference
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val code = Code(
                            gameID = document.getString("GameID")!!.toLowerCase(),
                            percentage = document.getLong("Percentage")!!,
                            validated = document.getBoolean("Validated")
                            )
                        codes.put(document.id, code)
                    }
                    callback(codes)
                } else {
                    Log.w(TAG, "Error getting documents.", task.exception)
                }
            })
            .addOnFailureListener {
                Toast.makeText(context, "Download failed..", Toast.LENGTH_LONG).show()
            }
    }

    fun redeemDiscount(museumID: String, projectID: String, gameID: String, callback: (String) -> Unit) {
        val item = HashMap<String, Any>()
        item.put("Validated", true)

        var specificCodeReference = firestoreDB.collection("Organisation")
            .document(museumID)
            .collection("Project")
            .document(projectID)
            .collection("Code")
            .document(gameID)
        specificCodeReference.set(item, SetOptions.merge())
            .addOnSuccessListener {
                callback("Success")
            }
            .addOnFailureListener {
                callback("Failure")
            }
    }
}
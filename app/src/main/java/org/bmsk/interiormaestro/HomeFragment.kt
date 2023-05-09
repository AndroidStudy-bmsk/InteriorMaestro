package org.bmsk.interiormaestro

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import org.bmsk.interiormaestro.data.ArticleModel

class HomeFragment : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = Firebase.firestore
        db.collection("articles").document("hKnGPHLWWsNTzhoktBtV")
            .get()
            .addOnSuccessListener { result ->
                val article = result.toObject<ArticleModel>()
                Log.e("homefragment", article.toString())
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }
}
package org.bmsk.interiormaestro.ui.article

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.bmsk.interiormaestro.R
import org.bmsk.interiormaestro.databinding.FragmentWriteArticleBinding
import java.util.UUID

class WriteArticleFragment : Fragment(R.layout.fragment_write_article) {
    private lateinit var binding: FragmentWriteArticleBinding
    private var selectedUri: Uri? = null
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->

            if (uri != null) {
                selectedUri = uri
                binding.photoImageView.setImageURI(uri)
            } else {

            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWriteArticleBinding.bind(view)

        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

        binding.photoImageView.setOnClickListener {

        }

        binding.cancelButton.setOnClickListener {
            findNavController().navigate(WriteArticleFragmentDirections.actionWriteArticleFragmentToHomeFragmentCancel())
        }

        binding.submitButton.setOnClickListener {
            if (selectedUri != null) {
                val photoUri = selectedUri ?: return@setOnClickListener

                val fileName = "${UUID.randomUUID()}.png"
                Firebase.storage.reference.child("articles/photo").child(fileName)
                    .putFile(photoUri)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Firebase.storage.reference.child("articles/photo/$fileName")
                                .downloadUrl
                                .addOnSuccessListener {
                                    Log.e("aa", it.toString())
                                }.addOnFailureListener {

                                }
                        } else {
                            task.exception?.printStackTrace()
                            // error Handle
                        }
                    }

            } else {
                Snackbar.make(view, getString(R.string.image_not_selected), Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.backButton.setOnClickListener {
            findNavController().navigate(WriteArticleFragmentDirections.actionWriteArticleFragmentToHomeFragmentBack())
        }
    }
}
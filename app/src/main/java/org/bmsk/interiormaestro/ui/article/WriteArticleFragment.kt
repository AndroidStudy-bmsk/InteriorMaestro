package org.bmsk.interiormaestro.ui.article

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.bmsk.interiormaestro.R
import org.bmsk.interiormaestro.data.ArticleModel
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
                binding.addButton.isVisible = false
                binding.cancelButton.isVisible = true
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWriteArticleBinding.bind(view)

        startPicker()
        setUpPhotoImageView()
        setUpCancelButton()
        setUpSubmitButton(view)
        setUpBackButton()
    }

    private fun setUpCancelButton() {
        binding.cancelButton.setOnClickListener {
            binding.photoImageView.setImageURI(null)
            selectedUri = null
            binding.cancelButton.isVisible = false
            binding.addButton.isVisible = true
        }
    }

    private fun setUpPhotoImageView() {
        binding.photoImageView.setOnClickListener {
            if (selectedUri == null) {
                startPicker()
            }
        }
    }

    private fun setUpSubmitButton(view: View) {
        binding.submitButton.setOnClickListener {
            showProgress()

            if (selectedUri != null) {
                val photoUri = selectedUri ?: return@setOnClickListener
                uploadImage(
                    photoUri,
                    successHandler = {
                        uploadArticle(it, binding.descriptionEditText.text.toString())
                    },
                    errorHandler = {
                        Snackbar.make(
                            view,
                            getString(R.string.fail_image_upload),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        hideProgress()
                    }
                )
            } else {
                Snackbar.make(view, getString(R.string.image_not_selected), Snackbar.LENGTH_SHORT)
                    .show()
                hideProgress()
            }
        }
    }

    private fun setUpBackButton() {
        binding.backButton.setOnClickListener {
            findNavController().navigate(WriteArticleFragmentDirections.actionWriteArticleFragmentToHomeFragmentBack())
        }
    }

    private fun startPicker() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun showProgress() {
        binding.progressBarLayout.isVisible = true
    }

    private fun hideProgress() {
        binding.progressBarLayout.isVisible = false
    }

    private fun uploadImage(
        uri: Uri,
        successHandler: (String) -> Unit,
        errorHandler: (Throwable?) -> Unit
    ) {
        val fileName = "${UUID.randomUUID()}.png"
        Firebase.storage.reference.child("articles/photo").child(fileName)
            .putFile(uri)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Firebase.storage.reference.child("articles/photo/$fileName")
                        .downloadUrl
                        .addOnSuccessListener {
                            successHandler(it.toString())
                        }.addOnFailureListener {
                            errorHandler(it)
                        }
                } else {
                    errorHandler(task.exception)
                }
            }
    }

    private fun uploadArticle(photoUri: String, description: String) {
        val articleId = UUID.randomUUID().toString()
        val articleModel = ArticleModel(
            articleId = articleId,
            createAt = System.currentTimeMillis(),
            description = description,
            imageUrl = photoUri,
        )
        Firebase.firestore.collection("articles").document(articleId)
            .set(articleModel)
            .addOnSuccessListener {
                findNavController().navigate(WriteArticleFragmentDirections.actionWriteArticleFragmentToHomeFragmentBack())
                hideProgress()
            }.addOnFailureListener {
                it.printStackTrace()
                view?.let { view ->
                    Snackbar.make(view, "글 작성에 실패해습니다", Snackbar.LENGTH_SHORT).show()
                }
                hideProgress()
            }

        hideProgress()
    }
}
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
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
    private lateinit var viewModel: WriteArticleViewModel

    //    private var selectedUri: Uri? = null
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                viewModel.updateSelectedUri(uri)
            } else {
                Log.e("WriteArticleFragment", "선택하지 않음")
            }
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWriteArticleBinding.bind(view)

        setUpViewModel()
        if (viewModel.selectedUri.value == null) {
            startPicker()
        }
        setUpPhotoImageView()
        setUpCancelButton()
        setUpSubmitButton(view)
        setUpBackButton()
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(requireActivity()).get()
        viewModel.selectedUri.observe(viewLifecycleOwner) {
            binding.photoImageView.setImageURI(it)

            if (it != null) {
                binding.addButton.isVisible = true
                binding.cancelButton.isVisible = false
            } else {
                binding.addButton.isVisible = false
                binding.cancelButton.isVisible = true
            }
        }
    }

    private fun setUpCancelButton() {
        binding.cancelButton.setOnClickListener {
            viewModel.updateSelectedUri(null)
        }
    }

    private fun setUpPhotoImageView() {
        binding.photoImageView.setOnClickListener {
            if (viewModel.selectedUri.value == null) {
                startPicker()
            }
        }
    }

    private fun setUpSubmitButton(view: View) {
        binding.submitButton.setOnClickListener {
            showProgress()

            val photoUri = viewModel.selectedUri.value
            if (photoUri != null) {
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
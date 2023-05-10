package org.bmsk.interiormaestro.ui.home

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import org.bmsk.interiormaestro.R
import org.bmsk.interiormaestro.data.ArticleModel
import org.bmsk.interiormaestro.databinding.FragmentHomeBinding
import org.bmsk.interiormaestro.ui.home.adapter.HomeArticleAdapter

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private val articleAdapter by lazy { initHomeArticleAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        setUpWriteButton()
        setUpBookMarkButton()
        setUpRecyclerView()
        fetchArticlesData()
    }

    private fun setUpWriteButton() {
        binding.writeButton.setOnClickListener {
            if (isUserLoggedIn()) {
                navigateToWriteArticleFragment()
            } else {
                showLoginSnackbar()
            }
        }
    }

    private fun setUpBookMarkButton() {
        binding.bookMarkButton.setOnClickListener {
            navigateToBookmarkArticleFragment()
        }
    }

    private fun setUpRecyclerView() {
        binding.homeRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = articleAdapter
        }
    }

    private fun initHomeArticleAdapter(): HomeArticleAdapter {
        return HomeArticleAdapter(
            onItemClicked = { navigateToArticleFragment(it.articleId.orEmpty()) },
            onBookmarkClicked = { articleId, isBookmark -> updateBookmark(articleId, isBookmark) }
        )
    }

    private fun fetchArticlesData() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        Firebase.firestore.collection("bookmark").document(uid)
            .get()
            .addOnSuccessListener { document ->
                val bookMarkList = document.get("articleIds") as? List<*>
                fetchArticles(bookMarkList)
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    private fun fetchArticles(bookMarkList: List<*>?) {
        Firebase.firestore.collection("articles")
            .get()
            .addOnSuccessListener { result ->
                val list = result.map { snapshot -> snapshot.toObject<ArticleModel>() }
                    .map { model -> createArticleItem(model, bookMarkList) }
                articleAdapter.submitList(list)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    private fun createArticleItem(model: ArticleModel, bookMarkList: List<*>?): ArticleItem {
        return ArticleItem(
            articleId = model.articleId.orEmpty(),
            description = model.description.orEmpty(),
            imageUrl = model.imageUrl.orEmpty(),
            isBookmark = bookMarkList?.contains(model.articleId.orEmpty()) ?: false
        )
    }

    private fun updateBookmark(articleId: String, isBookmark: Boolean) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val document = Firebase.firestore.collection("bookmark").document(uid)
        val updateAction = if (isBookmark) {
            FieldValue.arrayUnion(articleId)
        } else {
            FieldValue.arrayRemove(articleId)
        }

        document.update("articleIds", updateAction).addOnFailureListener {
            if (it is FirebaseFirestoreException && it.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                if (isBookmark) {
                    document.set(hashMapOf("articleIds" to listOf(articleId)))
                }
            }
        }
    }

    private fun isUserLoggedIn(): Boolean {
        return Firebase.auth.currentUser != null
    }

    private fun navigateToWriteArticleFragment() {
        val action = HomeFragmentDirections.actionHomeFragmentToWriteArticleFragment()
        findNavController().navigate(action)
    }

    private fun navigateToBookmarkArticleFragment() {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToBookMarkArticleFragment())
    }

    private fun navigateToArticleFragment(articleId: String) {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToArticleFragment(articleId))
    }

    private fun showLoginSnackbar() {
        Snackbar.make(
            requireView(),
            getString(R.string.guide_use_after_login),
            Snackbar.LENGTH_SHORT
        ).show()
    }
}
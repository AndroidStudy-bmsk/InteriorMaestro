package org.bmsk.interiormaestro.ui.bookmark

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import org.bmsk.interiormaestro.R
import org.bmsk.interiormaestro.databinding.FragmentBookmarkArticleBinding
import org.bmsk.interiormaestro.ui.home.adapter.BookmarkArticleAdapter

class BookmarkArticleFragment : Fragment(R.layout.fragment_bookmark_article) {
    private lateinit var binding: FragmentBookmarkArticleBinding
    private val bookmarkAdapter by lazy { initBookmarkArticleAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBookmarkArticleBinding.bind(view)

        setupToolbar()
        setupRecyclerView()
        fetchBookmarkedArticles()
    }

    private fun setupToolbar() {
        binding.toolbar.setupWithNavController(findNavController())
    }

    private fun setupRecyclerView() {
        binding.articleRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = bookmarkAdapter
        }
    }

    private fun fetchBookmarkedArticles() {
        val uid = Firebase.auth.currentUser?.uid.orEmpty()
        Firebase.firestore.collection("bookmark")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val articleIds = document.get("articleIds") as List<*>
                fetchArticles(articleIds)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    private fun fetchArticles(articleIds: List<*>) {
        if (articleIds.isNotEmpty()) {
            Firebase.firestore.collection("articles")
                .whereIn("articleId", articleIds)
                .get()
                .addOnSuccessListener { result ->
                    bookmarkAdapter.submitList(result.map { article -> article.toObject() })
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }

    private fun initBookmarkArticleAdapter(): BookmarkArticleAdapter {
        return BookmarkArticleAdapter { navigateToArticleFragment(it.articleId.orEmpty()) }
    }

    private fun navigateToArticleFragment(articleId: String) {
        findNavController().navigate(
            BookmarkArticleFragmentDirections.actionBookMarkArticleFragmentToArticleFragment(
                articleId
            )
        )
    }
}
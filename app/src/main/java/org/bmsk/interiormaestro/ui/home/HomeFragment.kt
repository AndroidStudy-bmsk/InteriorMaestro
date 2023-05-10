package org.bmsk.interiormaestro.ui.home

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
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

        setUpWriteButton(view)
        setUpRecyclerView()
        fetchArticlesData()
    }

    private fun setUpWriteButton(view: View) {
        binding.writeButton.setOnClickListener {
            if (Firebase.auth.currentUser != null) {
                val action = HomeFragmentDirections.actionHomeFragmentToWriteArticleFragment()
                findNavController().navigate(action)
            } else {
                Snackbar.make(
                    view,
                    getString(R.string.guide_use_after_login),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setUpRecyclerView() {

        binding.homeRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = articleAdapter
        }
    }

    private fun initHomeArticleAdapter(): HomeArticleAdapter {
        return HomeArticleAdapter {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToArticleFragment(
                    articleId = it.articleId.orEmpty()
                )
            )
        }
    }

    private fun fetchArticlesData() {
        Firebase.firestore.collection("articles")
            .get()
            .addOnSuccessListener { result ->
                val list = result.map {
                    it.toObject<ArticleModel>()
                }
                articleAdapter.submitList(list)
            }.addOnFailureListener {

            }
    }
}
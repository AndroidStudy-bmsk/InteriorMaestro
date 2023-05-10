package org.bmsk.interiormaestro.ui.home

data class ArticleItem(
    val articleId: String,
    val description: String,
    val imageUrl: String,
    var isBookmark: Boolean
)

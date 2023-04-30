package com.androiddevs.mvvmnewsapp.repository

import com.androiddevs.mvvmnewsapp.api.RetrofitInstance
import com.androiddevs.mvvmnewsapp.db.ArticleDatabase
import com.androiddevs.mvvmnewsapp.models.Article

//purpose of repository is to get the data from our database and our remote data source from retrofit from api
class NewsRepository(
    val db: ArticleDatabase // we will need this db to access the functions of our database
    //because we can call api from RetrofitInstance we don't need pass api as a parameter to access it
    ) {
    //write a fun to get the breaking news from api
    suspend fun getBreakingNews(countryCode: String, pageNumber: Int) =
        RetrofitInstance.api.getBreakingNews(countryCode,pageNumber)

    suspend fun searchNews(searchQuery:String,pageNumber: Int) =
        RetrofitInstance.api.searchForNews(searchQuery,pageNumber)

    suspend fun upsert(article:Article) = db.getArticleDao().upsert(article)

    fun getSavedNews() = db.getArticleDao().getAllArticles()

    suspend fun deleteArticle(article:Article) = db.getArticleDao().deleteArticle(article)
}
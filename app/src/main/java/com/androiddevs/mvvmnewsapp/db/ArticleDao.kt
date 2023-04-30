package com.androiddevs.mvvmnewsapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.androiddevs.mvvmnewsapp.models.Article

@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(article: Article): Long // for insert article or replace it if exist (return Long id )

    @Query("SELECT * FROM articles")
    fun getAllArticles(): LiveData<List<Article>> // not suspend fun because will return livedata object and that doesn't work with suspend fun

    @Delete
    suspend fun deleteArticle(article: Article)
}
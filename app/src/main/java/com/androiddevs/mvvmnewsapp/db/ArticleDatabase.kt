package com.androiddevs.mvvmnewsapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.androiddevs.mvvmnewsapp.models.Article

@Database(entities = [Article::class], version = 5)
@TypeConverters(Convertors::class)
abstract class ArticleDatabase:RoomDatabase() {

    abstract fun getArticleDao(): ArticleDao

    companion object{
        @Volatile // to make other threads immediately see when a thread changes this instance
        private var instance: ArticleDatabase? = null// only have a single instance of that database
        private val LOCK = Any()

        // in invoke fun whenever we create that instance of articleDatabase then return the current instance
        //and if it null we will set that instance in synchronized block
        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            // inside this block code can't be accessed by other threads at the same time
            // also set our instance to the result of our create database
            instance ?: createDatabase(context).also { instance = it }
            // then our instance of that database class will then be used to access our article Dao
        }
        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                ArticleDatabase::class.java,
                "article_db.db"
            ).fallbackToDestructiveMigration().build()
    }
}
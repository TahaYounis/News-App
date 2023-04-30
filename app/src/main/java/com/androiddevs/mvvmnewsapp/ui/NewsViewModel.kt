package com.androiddevs.mvvmnewsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.mvvmnewsapp.NewsApplication
import com.androiddevs.mvvmnewsapp.models.Article
import com.androiddevs.mvvmnewsapp.models.NewsResponse
import com.androiddevs.mvvmnewsapp.repository.NewsRepository
import com.androiddevs.mvvmnewsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    app:Application,
    /* we need our news repository in view model but we can't use constructor parameter by default for viewModel
    , so we will use viewModelProviderFactory to define how our viewModel should be created */
    val newsRepository: NewsRepository
):AndroidViewModel(app) {
    //we will call the functions from repository and also handle our responses of our requests and then
    //we will have livedata objects that will notify all fragments about changes regarding this request

    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    var breakingNewsResponse:NewsResponse? = null // need to save current response in viewModel to make response alive even if we rotated mobile

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null
    var newSearchQuery:String? = null
    var oldSearchQuery:String? = null

    init {
        getBreakingNews("eg")
    }
    /* fun that execute our API call from repository, we don't send page as parameter for make it in viewModel to make it live
     and not destroy it when rotate, viewModelScope make sure coroutine is stays only as long as our ViewModel alive */
    fun getBreakingNews(countryCode: String) = viewModelScope.launch {
        safeBreakingNewsCall(countryCode)
    }

    fun searchNews(searchQuery:String) = viewModelScope.launch {
        safeSearchNewsCall(searchQuery)
    }

    //for pagination
    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let { resultResponse ->
                breakingNewsPage++ // when response success increase page to load next page
                if (breakingNewsResponse == null) {
                    breakingNewsResponse = resultResponse //put the resultResponse we got from API to breakingNewsResponse variable
                }else{
                    val oldArticles = breakingNewsResponse?.articles
                    val newArticle = resultResponse.articles
                    oldArticles?.addAll(newArticle)
                }
                return Resource.Success(breakingNewsResponse?:resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let { resultResponse ->
                if(searchNewsResponse == null || newSearchQuery != oldSearchQuery) {
                    searchNewsPage = 1
                    oldSearchQuery = newSearchQuery
                    searchNewsResponse = resultResponse
                }else{
                    searchNewsPage++
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse?:resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun saveArticle(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getSavedArticle() = newsRepository.getSavedNews()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }
    //create a fun for getBreakingNews & searchNews that make a safe API call
    private suspend fun safeBreakingNewsCall(countryCode: String){
        breakingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()){
                val response = newsRepository.getBreakingNews(countryCode,breakingNewsPage) // network response saved in variable response
                breakingNews.postValue(handleBreakingNewsResponse(response))
            }else{
                breakingNews.postValue(Resource.Error("No Internet connection"))
            }
        }catch (t: Throwable){
            when(t){
                is IOException -> breakingNews.postValue(Resource.Error("Network failure"))
                else -> breakingNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }
    private suspend fun safeSearchNewsCall(searchQuery: String){
        newSearchQuery = searchQuery
        searchNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()){
                val response = newsRepository.searchNews(searchQuery,searchNewsPage) // network response saved in variable response
                searchNews.postValue(handleSearchNewsResponse(response))
            }else{
                searchNews.postValue(Resource.Error("No Internet connection"))
            }
        }catch (t: Throwable){
            when(t){
                is IOException -> searchNews.postValue(Resource.Error("Network failure"))
                else -> searchNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    // fun to check if the user is connected to the internet
    private fun hasInternetConnection():Boolean{
        // we need activity manager , we need this fun in viewModel class ,we don't pass activity context in viewModel constructor
        // because we should separate the activity data from DUI, and if activity gets destroyed you can't use that context
        // so create Application class to pass getApplicationContext to viewModel
        val connectivityManager = getApplication<NewsApplication>().getSystemService(// get the reference to our connectivity manager tp detect if user is currently connected to internet or not
            Context.CONNECTIVITY_SERVICE) as ConnectivityManager // used as to cast to NewsApplication because kotlin not know is a connectivity manager because getSystemService return anonymous object
        // because activityNetworkInfo deprecated in 23 version we will used one more way for +23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // if return null then we know we don't have internet connection so return false directly
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false // capabilities قدرات
            // we those capabilities we have access to the different type of the network and check if available or not like wife
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when(type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }
}
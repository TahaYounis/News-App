package com.androiddevs.mvvmnewsapp.util

/* this class recommended by google to be used to wrap around our network responses and that will be a generic
class to differentiate between successful and error responses and also help us to handle the loading state
so when we make a response that we can show a progress bar while that response is processing and when get the answer
we can use that class to tell us whether that answer successful or an error, and handle that error or show successful response */
// sealed class it type of abstract can define which classes can inherit from that class
sealed class Resource<T>(
    val data: T? =null,
    val message: String? = null
) {
    class Success<T>(data: T): Resource<T>(data)
    class Error<T>(message: String, data: T? = null):Resource<T>(data,message)
    //returned when our request fired off and when the response comes will instead success or error
    class Loading<T>: Resource<T>()

}
package com.androiddevs.mvvmnewsapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.mvvmnewsapp.R
import com.androiddevs.mvvmnewsapp.adapters.NewsAdapter
import com.androiddevs.mvvmnewsapp.ui.NewsActivity
import com.androiddevs.mvvmnewsapp.ui.NewsViewModel
import com.androiddevs.mvvmnewsapp.util.Constants.Companion.QUERY_PAGE_SIZE
import com.androiddevs.mvvmnewsapp.util.Resource
import kotlinx.android.synthetic.main.fragment_breaking_news.*
import kotlinx.android.synthetic.main.item_error_message.*

class BreakingNewsFragment:Fragment(R.layout.fragment_breaking_news) {

    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel// we have access viewModel created in NewsActivity
        setupRecyclerView()

        /* we have the article as argument on which we clicked, so we need to take article and put it in bundle
        * then attach this bundle to our navigation components and navigation handle the transition for us and
        * pass our argument to our article fragment */

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            // pass resource id of an action that we want perform ( navigate : التنقل)
            findNavController().navigate(
                R.id.action_breakingNewsFragment_to_articleFragment,
                bundle
            )
        }
        viewModel.breakingNews.observe(viewLifecycleOwner, Observer { response ->
            when(response) {
                is Resource.Success -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        //every time get new response it could be our last page and we need notify our scroll isn't about that
                        //so it knows if it should paginate further or not.
                        //we put +2 because we have integer division that always rounded off so that is why we have
                        //to add one, and the last page of our response will always be empty so we added one more.
                        val totalPages = newsResponse.totalResults / QUERY_PAGE_SIZE + 2 // get total amount of pages
                        isLastPage = viewModel.breakingNewsPage == totalPages // if those is equal then we are in last page
                        if(isLastPage){ // to reset padding and cancel the 50dp because we clipped to padding (look xml file)
                            rvBreakingNews.setPadding(0,0,0,0)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity, "An error occured: $message", Toast.LENGTH_LONG).show()
                        showErrorMessage(message)
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
        btnRetry.setOnClickListener {
            viewModel.getBreakingNews("eg")
        }
    }//make our network request and get the response of the breaking news and display that in the recyclerView
    private fun hideProgressBar() {
        paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideErrorMessage() {
        itemErrorMessage.visibility = View.INVISIBLE
        isError = false
    }

    private fun showErrorMessage(message: String) {
        itemErrorMessage.visibility = View.VISIBLE
        tvErrorMessage.text = message
        isError = true
    }

    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    val scrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){ // we are currently scrolling
                isScrolling = true
            }
        }
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            // sadly there is not a default mechanism that tells us whether we scroll until the bottom or not
            // so we need make some calculations with layout manager of our recyclerView
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager // used as to convert it to LinearLayoutManager
            // this 3 variables to be able to make some calculations to check if we scroll until the bottom of recycerView
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition() // get first visual visible item position of layoutManager
            val visibleItemCount = layoutManager.childCount // total visible item count
            val totalItemCount = layoutManager.itemCount // total item count in recyclerView

            // add many booleans
            val isNoErrors = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            // to check if we are in last item ( totalItemCount=20 - visibleItemCount=10 we check if firstVisibleItemPosition=1
            // then 1+10 >= 20 result false, if firstVisibleItemPosition=10 then 10+10>=20 result true then we know that our lastItem visible in screen
            val isOurLastItemIsVisible = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isOurFirstItemNotVisible = firstVisibleItemPosition >= 0 // if we scroll and first item not visible
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            // with all those booleans we can check determine if we should paginate or not
            val shouldPaginate = isNoErrors && isNotLoadingAndNotLastPage && isOurLastItemIsVisible
                    && isOurFirstItemNotVisible && isTotalMoreThanVisible && isScrolling
            if (shouldPaginate){
                viewModel.getBreakingNews("eg") // whenever we call this fun breakingNewsPage will automatically be increased in viewModel
                isScrolling = false

            }
        }
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }
    }
}
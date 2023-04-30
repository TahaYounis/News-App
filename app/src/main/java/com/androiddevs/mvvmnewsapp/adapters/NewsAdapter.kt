package com.androiddevs.mvvmnewsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.mvvmnewsapp.R
import com.androiddevs.mvvmnewsapp.models.Article
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_article_preview.view.*

class NewsAdapter: RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    //instead of our new adapter class we create inner class article view
    inner class ViewHolder(itemView:View): RecyclerView.ViewHolder(itemView)

    /* normally you pass list of article in constructor and every time you want to add an article add it in
     in the list and call adapter.notifyDataSetChange but this is very inefficient because using notifyDataSetChange
     will update whole recyclerView items even the items didn't change, to solve this problem we can use de future
     it calculate the differences between two lists and enables us update just the different items, another advantages
     it will actually happen in the background so we don't block our main thread */

    // create the callback for async list differ
    private val differCallback = object : DiffUtil.ItemCallback<Article>(){
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }
        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }
    //async list differ it the tool that compare between our two list and only update changed items
    val differ = AsyncListDiffer(this,differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article_preview2,parent,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val  article = differ.currentList[position]
        //apply make you immediately call the views directly without create Object
        holder.itemView.apply {
            Glide.with(this).load(article.urlToImage).into(ivArticleImage)
            tvSource.text = article.source?.name
            tvTitle.text = article.title
//            tvDescription.text = article.description
            tvPublishedAt.text = article.publishedAt
            //setOnClickLister refer to oru to our itemView
            setOnClickListener{
                // it refer to our onItemClickListener lambda fun and lambda fun takes article as parameter on which we click basically
                onItemClickListener?.let { it(article) }
            }

        }
    }

    //we will pass the current article when we click on an item to that lamda function, so we will be able to open the corrednt web view page
    private var onItemClickListener: ((Article) -> Unit)? = null

    fun setOnItemClickListener(listener: (Article)->Unit){
        //set our own listener to our passed listener
        onItemClickListener = listener
    }
}
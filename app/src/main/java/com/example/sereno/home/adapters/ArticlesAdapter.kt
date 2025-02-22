package com.example.sereno.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.sereno.R
import com.example.sereno.common.extensions.onClickWithHaptics
import com.example.sereno.databinding.ArticleItemBinding
import com.example.sereno.home.model.ArticleModel


class ArticlesAdapter : RecyclerView.Adapter<ArticlesAdapter.ArticlesViewHolder>() {
    private val articles = mutableListOf<ArticleModel>()

    inner class ArticlesViewHolder(private val binding: ArticleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            val article = articles[adapterPosition]
            binding.date.text = article.date
            binding.title.text = article.title
            var requestOptions = RequestOptions()
            requestOptions = requestOptions
                .centerCrop()
                .transform(
                    RoundedCorners(
                        binding.imageView.context.resources.getDimensionPixelSize(
                            R.dimen._18dp
                        )
                    )
                )

            Glide.with(binding.imageView)
                .load(article.thumbnailURL)
                .apply(requestOptions)
                .fallback(R.drawable.article_bg_1)
                .into(binding.imageView)
            binding.bg.onClickWithHaptics { }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticlesViewHolder {
        val binding = ArticleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticlesViewHolder(binding)
    }

    override fun getItemCount(): Int = articles.size

    override fun onBindViewHolder(holder: ArticlesViewHolder, position: Int) {
        holder.bind()
    }

    fun setArticles(articles: List<ArticleModel>) {
        this.articles.clear()
        this.articles.addAll(articles)
        notifyDataSetChanged()
    }
}
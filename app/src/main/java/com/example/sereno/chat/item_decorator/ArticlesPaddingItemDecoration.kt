package com.example.sereno.home.item_decorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ArticlesPaddingItemDecoration(private val padding: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val itemCount = state.itemCount

        when (position) {
            0 -> {
                outRect.left = padding
            }
            itemCount - 1 -> {
                outRect.right = padding
                outRect.left = padding / 2
            }
            else -> {
                outRect.left = padding / 2
            }
        }
    }
}

package com.example.sereno.home.item_decorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val horizontalSpacing: Int,
    private val verticalSpacing: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        if (position % spanCount != spanCount - 1) {
            outRect.right = horizontalSpacing
        }

        val totalItems = state.itemCount
        val lastRowStartIndex = totalItems - (totalItems % spanCount)

        if (position < lastRowStartIndex) {
            outRect.bottom = verticalSpacing
        }
    }
}

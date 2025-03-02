package com.example.sereno.chat.utils

import android.content.res.Resources
import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.sereno.chat.adapter.ChatAdapter
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ReplaySwiperHelper(
    private val chatAdapter: ChatAdapter,
    private val onSwipe: (pos: Int) -> Unit
) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
    private val swipeThreshold = 40f * Resources.getSystem().displayMetrics.density
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val position = viewHolder.adapterPosition
        if (position == RecyclerView.NO_POSITION) return 0

        val isBotMessage = chatAdapter.isBot(position)
        val swipeDir = if (isBotMessage) ItemTouchHelper.RIGHT else ItemTouchHelper.LEFT

        return makeMovementFlags(0, swipeDir)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        onSwipe(position)
    }

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {

        val position = viewHolder.adapterPosition
        if (!chatAdapter.isSwipeable(position)) return
        if (position == RecyclerView.NO_POSITION) return
        val isBotMessage = chatAdapter.isBot(position)
        val clampedDx = if (isBotMessage) min(dX, swipeThreshold) else max(dX, -swipeThreshold)

        if (!isCurrentlyActive && abs(dX) > swipeThreshold) {
            viewHolder.itemView.animate().translationX(0f).setDuration(200).start()
        }

        super.onChildDraw(
            c,
            recyclerView,
            viewHolder,
            clampedDx,
            dY,
            actionState,
            isCurrentlyActive
        )
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return .1f
    }

    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.animate().translationX(0f).setDuration(200).start()
    }
}
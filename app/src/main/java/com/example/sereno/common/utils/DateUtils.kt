package com.example.sereno.common.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateUtils {
    private val DAY_FORMAT = SimpleDateFormat("EEEE", Locale.getDefault())
    private val DATE_FORMAT = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
    private val DATE_WITH_YEAR_FORMAT = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isWithinLastWeek(chatDate: Calendar, today: Calendar): Boolean {
        val sixDaysAgo = Calendar.getInstance().apply {
            timeInMillis = today.timeInMillis
            add(Calendar.DAY_OF_YEAR, -6)
        }
        return chatDate.after(sixDaysAgo) && chatDate.before(today)
    }

    fun formatDate(timestamp: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }

        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }

        return when {
            isSameDay(calendar, today) -> "Today"
            isSameDay(calendar, yesterday) -> "Yesterday"
            isWithinLastWeek(calendar, today) -> DAY_FORMAT.format(calendar.time)
            calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) ->
                DATE_FORMAT.format(calendar.time)

            else -> DATE_WITH_YEAR_FORMAT.format(calendar.time)
        }
    }
}
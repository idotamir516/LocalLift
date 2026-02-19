package com.workout.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Centralized date formatting utilities to maintain consistency across the app.
 * All date formats use the device's default locale.
 */
object DateFormatUtils {
    
    // Common date format patterns
    private val fullDateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    private val mediumDateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val compactDateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    private val monthDayFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    private val dayMonthFormat = SimpleDateFormat("M/d", Locale.getDefault())
    private val dayOfWeekWithDateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val monthOnlyFormat = SimpleDateFormat("MMMM", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val fileNameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    /**
     * Full date format: "Wednesday, January 15, 2025"
     * Used for: Workout details, weight entry dialogs
     */
    fun formatFullDate(timestamp: Long): String = fullDateFormat.format(Date(timestamp))
    fun formatFullDate(date: Date): String = fullDateFormat.format(date)
    
    /**
     * Medium date format: "Wed, Jan 15, 2025"
     * Used for: Weight entry list items, edit workout screen
     */
    fun formatMediumDate(timestamp: Long): String = mediumDateFormat.format(Date(timestamp))
    fun formatMediumDate(date: Date): String = mediumDateFormat.format(date)
    
    /**
     * Short date format: "Jan 15, 2025"
     * Used for: Training phases, exercise history, last updated dates
     */
    fun formatShortDate(timestamp: Long): String = shortDateFormat.format(Date(timestamp))
    fun formatShortDate(date: Date): String = shortDateFormat.format(date)
    
    /**
     * Compact date format: "Wed, Jan 15"
     * Used for: Recent workout cards, session lists
     */
    fun formatCompactDate(timestamp: Long): String = compactDateFormat.format(Date(timestamp))
    fun formatCompactDate(date: Date): String = compactDateFormat.format(date)
    
    /**
     * Month and day only: "Jan 15"
     * Used for: Chart axis labels
     */
    fun formatMonthDay(timestamp: Long): String = monthDayFormat.format(Date(timestamp))
    fun formatMonthDay(date: Date): String = monthDayFormat.format(date)
    
    /**
     * Day/month format: "1/15"
     * Used for: Compact chart labels
     */
    fun formatDayMonth(timestamp: Long): String = dayMonthFormat.format(Date(timestamp))
    fun formatDayMonth(date: Date): String = dayMonthFormat.format(date)
    
    /**
     * Day of week with date: "Wednesday, January 15"
     * Used for: Home screen header, calendar selection
     */
    fun formatDayOfWeekDate(timestamp: Long): String = dayOfWeekWithDateFormat.format(Date(timestamp))
    fun formatDayOfWeekDate(date: Date): String = dayOfWeekWithDateFormat.format(date)
    
    /**
     * Month and year: "January 2025"
     * Used for: Calendar headers, progress screen
     */
    fun formatMonthYear(timestamp: Long): String = monthYearFormat.format(Date(timestamp))
    fun formatMonthYear(date: Date): String = monthYearFormat.format(date)
    
    /**
     * Month only: "January"
     * Used for: Calendar navigation
     */
    fun formatMonth(timestamp: Long): String = monthOnlyFormat.format(Date(timestamp))
    fun formatMonth(date: Date): String = monthOnlyFormat.format(date)
    
    /**
     * Time format: "3:45 PM"
     * Used for: Workout start/end times
     */
    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))
    fun formatTime(date: Date): String = timeFormat.format(date)
    
    /**
     * ISO date format: "2025-01-15"
     * Used for: Data export, navigation arguments
     */
    fun formatIsoDate(timestamp: Long): String = isoDateFormat.format(Date(timestamp))
    fun formatIsoDate(date: Date): String = isoDateFormat.format(date)
    
    /**
     * File name format: "20250115_154532"
     * Used for: Export file names
     */
    fun formatForFileName(timestamp: Long = System.currentTimeMillis()): String = 
        fileNameFormat.format(Date(timestamp))
    
    /**
     * Parse an ISO date string to timestamp
     */
    fun parseIsoDate(dateString: String): Long? {
        return try {
            isoDateFormat.parse(dateString)?.time
        } catch (e: Exception) {
            null
        }
    }
}

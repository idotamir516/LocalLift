package com.workout.app.ui.components.shared

/**
 * Input validation utilities for workout-related numeric inputs.
 * Provides consistent validation logic across the app for weights, reps, rest times, etc.
 */
object InputValidation {
    
    // Weight validation constants
    private const val MIN_WEIGHT = 0.0
    private const val MAX_WEIGHT_KG = 1000.0  // 1000 kg max (world records are ~500kg)
    private const val MAX_WEIGHT_LBS = 2200.0 // ~1000 kg in lbs
    
    // Reps validation constants
    private const val MIN_REPS = 0
    private const val MAX_REPS = 999
    
    // RPE validation constants
    private const val MIN_RPE = 1.0f
    private const val MAX_RPE = 10.0f
    
    // Set count validation
    private const val MIN_SETS = 1
    private const val MAX_SETS = 99
    
    // Body weight validation (for tracking)
    private const val MIN_BODY_WEIGHT_KG = 20.0
    private const val MAX_BODY_WEIGHT_KG = 500.0
    
    /**
     * Result of a validation check.
     */
    sealed class ValidationResult {
        /** Input is valid */
        data object Valid : ValidationResult()
        
        /** Input is empty (may or may not be acceptable depending on context) */
        data object Empty : ValidationResult()
        
        /** Input is invalid with an error message */
        data class Invalid(val message: String) : ValidationResult()
        
        /** Check if the result represents a valid input */
        val isValid: Boolean get() = this is Valid
        
        /** Check if the result represents an empty input */
        val isEmpty: Boolean get() = this is Empty
        
        /** Get error message if invalid, null otherwise */
        val errorMessage: String? get() = (this as? Invalid)?.message
    }
    
    // ==================== Numeric String Filtering ====================
    
    /**
     * Filter a string to only allow integer digits.
     * Use this for onValueChange in text fields for reps, sets, etc.
     *
     * @param input The raw input string
     * @return Filtered string containing only digits
     */
    fun filterIntegerInput(input: String): String {
        return input.filter { it.isDigit() }
    }
    
    /**
     * Filter a string to only allow decimal numbers (digits and one decimal point).
     * Use this for onValueChange in text fields for weight, body weight, etc.
     *
     * @param input The raw input string
     * @return Filtered string containing only valid decimal characters
     */
    fun filterDecimalInput(input: String): String {
        var hasDecimal = false
        return input.filter { char ->
            when {
                char.isDigit() -> true
                char == '.' && !hasDecimal -> {
                    hasDecimal = true
                    true
                }
                else -> false
            }
        }
    }
    
    /**
     * Filter and limit integer input to a maximum number of digits.
     *
     * @param input The raw input string
     * @param maxDigits Maximum number of digits allowed
     * @return Filtered and truncated string
     */
    fun filterIntegerInput(input: String, maxDigits: Int): String {
        return filterIntegerInput(input).take(maxDigits)
    }
    
    // ==================== Weight Validation ====================
    
    /**
     * Validate a weight input string.
     *
     * @param input The weight string to validate
     * @param isKg Whether the weight is in kilograms (affects max limit)
     * @return ValidationResult indicating if the weight is valid
     */
    fun validateWeight(input: String, isKg: Boolean = true): ValidationResult {
        if (input.isBlank()) return ValidationResult.Empty
        
        val weight = input.toDoubleOrNull()
            ?: return ValidationResult.Invalid("Invalid number format")
        
        val maxWeight = if (isKg) MAX_WEIGHT_KG else MAX_WEIGHT_LBS
        val unit = if (isKg) "kg" else "lbs"
        
        return when {
            weight < MIN_WEIGHT -> ValidationResult.Invalid("Weight cannot be negative")
            weight > maxWeight -> ValidationResult.Invalid("Weight cannot exceed $maxWeight $unit")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Parse a weight string to an integer (for storage).
     * Returns null for empty or invalid input.
     *
     * @param input The weight string
     * @return The weight as an integer, or null if invalid/empty
     */
    fun parseWeightToInt(input: String): Int? {
        if (input.isBlank()) return null
        return input.toDoubleOrNull()?.toInt()
    }
    
    /**
     * Parse a weight string to a double.
     * Returns null for empty or invalid input.
     *
     * @param input The weight string
     * @return The weight as a double, or null if invalid/empty
     */
    fun parseWeight(input: String): Double? {
        if (input.isBlank()) return null
        return input.toDoubleOrNull()?.takeIf { it >= MIN_WEIGHT }
    }
    
    // ==================== Reps Validation ====================
    
    /**
     * Validate a reps input string.
     *
     * @param input The reps string to validate
     * @return ValidationResult indicating if the reps value is valid
     */
    fun validateReps(input: String): ValidationResult {
        if (input.isBlank()) return ValidationResult.Empty
        
        val reps = input.toIntOrNull()
            ?: return ValidationResult.Invalid("Invalid number")
        
        return when {
            reps < MIN_REPS -> ValidationResult.Invalid("Reps cannot be negative")
            reps > MAX_REPS -> ValidationResult.Invalid("Reps cannot exceed $MAX_REPS")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Parse a reps string to an integer.
     * Returns null for empty or invalid input.
     *
     * @param input The reps string
     * @return The reps as an integer, or null if invalid/empty
     */
    fun parseReps(input: String): Int? {
        if (input.isBlank()) return null
        return input.toIntOrNull()?.takeIf { it in MIN_REPS..MAX_REPS }
    }
    
    // ==================== RPE Validation ====================
    
    /**
     * Validate an RPE input.
     *
     * @param rpe The RPE value to validate
     * @return ValidationResult indicating if the RPE is valid
     */
    fun validateRpe(rpe: Float?): ValidationResult {
        if (rpe == null) return ValidationResult.Empty
        
        return when {
            rpe < MIN_RPE -> ValidationResult.Invalid("RPE must be at least $MIN_RPE")
            rpe > MAX_RPE -> ValidationResult.Invalid("RPE cannot exceed $MAX_RPE")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate an RPE string input.
     *
     * @param input The RPE string to validate
     * @return ValidationResult indicating if the RPE is valid
     */
    fun validateRpe(input: String): ValidationResult {
        if (input.isBlank()) return ValidationResult.Empty
        
        val rpe = input.toFloatOrNull()
            ?: return ValidationResult.Invalid("Invalid RPE format")
        
        return validateRpe(rpe)
    }
    
    /**
     * Parse an RPE string to a float.
     * Returns null for empty or invalid input.
     *
     * @param input The RPE string
     * @return The RPE as a float, or null if invalid/empty
     */
    fun parseRpe(input: String): Float? {
        if (input.isBlank()) return null
        return input.toFloatOrNull()?.takeIf { it in MIN_RPE..MAX_RPE }
    }
    
    // ==================== Rest Time Validation ====================
    
    /**
     * Validate a rest time in seconds.
     *
     * @param seconds The rest time to validate
     * @return ValidationResult indicating if the rest time is valid
     */
    fun validateRestTime(seconds: Int?): ValidationResult {
        if (seconds == null) return ValidationResult.Empty
        
        return when {
            seconds < RestTimeConstants.MinRestTimeSeconds -> 
                ValidationResult.Invalid("Rest time must be at least ${RestTimeConstants.MinRestTimeSeconds} seconds")
            seconds > RestTimeConstants.MaxRestTimeSeconds -> 
                ValidationResult.Invalid("Rest time cannot exceed ${RestTimeConstants.MaxRestTimeSeconds / 60} minutes")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Clamp a rest time value to valid bounds.
     *
     * @param seconds The rest time to clamp
     * @return The clamped rest time value
     */
    fun clampRestTime(seconds: Int): Int {
        return seconds.coerceIn(RestTimeConstants.MinRestTimeSeconds, RestTimeConstants.MaxRestTimeSeconds)
    }
    
    // ==================== Set Count Validation ====================
    
    /**
     * Validate a set count.
     *
     * @param count The number of sets to validate
     * @return ValidationResult indicating if the set count is valid
     */
    fun validateSetCount(count: Int): ValidationResult {
        return when {
            count < MIN_SETS -> ValidationResult.Invalid("Must have at least $MIN_SETS set")
            count > MAX_SETS -> ValidationResult.Invalid("Cannot exceed $MAX_SETS sets")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Parse a set count string to an integer.
     * Returns null for empty or invalid input.
     *
     * @param input The set count string
     * @return The set count as an integer, or null if invalid/empty
     */
    fun parseSetCount(input: String): Int? {
        if (input.isBlank()) return null
        return input.toIntOrNull()?.takeIf { it in MIN_SETS..MAX_SETS }
    }
    
    // ==================== Body Weight Validation ====================
    
    /**
     * Validate a body weight input string.
     *
     * @param input The body weight string to validate
     * @param isKg Whether the weight is in kilograms
     * @return ValidationResult indicating if the body weight is valid
     */
    fun validateBodyWeight(input: String, isKg: Boolean = true): ValidationResult {
        if (input.isBlank()) return ValidationResult.Empty
        
        val weight = input.toDoubleOrNull()
            ?: return ValidationResult.Invalid("Invalid number format")
        
        // Convert limits if needed
        val minWeight = if (isKg) MIN_BODY_WEIGHT_KG else MIN_BODY_WEIGHT_KG * 2.205
        val maxWeight = if (isKg) MAX_BODY_WEIGHT_KG else MAX_BODY_WEIGHT_KG * 2.205
        
        return when {
            weight <= 0 -> ValidationResult.Invalid("Weight must be positive")
            weight < minWeight -> ValidationResult.Invalid("Weight seems too low")
            weight > maxWeight -> ValidationResult.Invalid("Weight seems too high")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Parse a body weight string to a double.
     * Returns null for empty or invalid input.
     *
     * @param input The body weight string
     * @return The body weight as a double, or null if invalid/empty
     */
    fun parseBodyWeight(input: String): Double? {
        if (input.isBlank()) return null
        return input.toDoubleOrNull()?.takeIf { it > 0 }
    }
    
    // ==================== Generic Helpers ====================
    
    /**
     * Check if a string represents a valid positive integer.
     *
     * @param input The string to check
     * @return true if the string is a valid positive integer
     */
    fun isValidPositiveInt(input: String): Boolean {
        if (input.isBlank()) return false
        val value = input.toIntOrNull() ?: return false
        return value > 0
    }
    
    /**
     * Check if a string represents a valid non-negative integer.
     *
     * @param input The string to check
     * @return true if the string is a valid non-negative integer
     */
    fun isValidNonNegativeInt(input: String): Boolean {
        if (input.isBlank()) return false
        val value = input.toIntOrNull() ?: return false
        return value >= 0
    }
    
    /**
     * Check if a string represents a valid positive decimal number.
     *
     * @param input The string to check
     * @return true if the string is a valid positive decimal
     */
    fun isValidPositiveDecimal(input: String): Boolean {
        if (input.isBlank()) return false
        val value = input.toDoubleOrNull() ?: return false
        return value > 0
    }
}

/**
 * Extension function to convert nullable Int to display string.
 * Returns empty string for null values.
 */
fun Int?.toDisplayString(): String = this?.toString() ?: ""

/**
 * Extension function to convert nullable Double to display string with optional decimal places.
 * Returns empty string for null values.
 *
 * @param decimals Number of decimal places (default 1)
 */
fun Double?.toDisplayString(decimals: Int = 1): String {
    if (this == null) return ""
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        "%.${decimals}f".format(this)
    }
}

/**
 * Extension function to convert nullable Float to display string.
 * Returns empty string for null values.
 *
 * @param decimals Number of decimal places (default 1)
 */
fun Float?.toDisplayString(decimals: Int = 1): String {
    if (this == null) return ""
    return if (this == this.toLong().toFloat()) {
        this.toLong().toString()
    } else {
        "%.${decimals}f".format(this)
    }
}

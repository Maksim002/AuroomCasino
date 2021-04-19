package com.example.auroomcasino.utils


object MyUtils {
    fun toMyKey(date: String): String {
        return try {
            date.substring(0, 10)
        } catch (e: Exception) {
            ""
        }

    }
}

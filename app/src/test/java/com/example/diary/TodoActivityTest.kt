package com.example.diary

import org.junit.Test
import kotlin.test.assertEquals

internal class TodoActivityTest {

    private val testTodoActivity: TodoActivity = TodoActivity()


    @Test
    fun getTimeInMillisecond() {
        val expected: Long = 4_800_000
        assertEquals(expected, testTodoActivity.getTimeInMillisecond(20, 1))
    }
}
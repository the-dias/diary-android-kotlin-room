package com.example.diary

import org.junit.Test
import kotlin.test.assertEquals

internal class MainActivityTest {

    private val testMainActivity: MainActivity = MainActivity()


    @Test
    fun normalizeTimeString() {
        val expected = "01"
        assertEquals(expected, testMainActivity.normalizeTimeString(1))

        val expected2 = "10"
        assertEquals(expected2, testMainActivity.normalizeTimeString(10))

    }
}
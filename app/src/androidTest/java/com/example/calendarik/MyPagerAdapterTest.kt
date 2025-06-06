package com.example.calendarik

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class MyPagerAdapterTest {

    private lateinit var adapter: MyPagerAdapter
    private val testItems = listOf(
        PageData(
            imageRes = 1,
            imageRes1 = 2,
            title = "Title",
            welcomeMessage = "Welcome",
            headerText1 = "Header1",
            headerText2 = "Header2",
            backgroundImageRes = 3,
            slideImageRes = 4
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        adapter = MyPagerAdapter(testItems)
    }

    @Test
    fun getItemCount_returnsCorrectSize() {
        assertEquals(testItems.size, adapter.itemCount)
    }

}
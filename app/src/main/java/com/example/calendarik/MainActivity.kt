package com.example.calendarik

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {

    private lateinit var indicators: Array<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        indicators = arrayOf(
            findViewById(R.id.indicator1),
            findViewById(R.id.indicator2),
            findViewById(R.id.indicator3)
        )

        val items = listOf(
            PageData(
                imageRes = R.drawable.icon_11,
                imageRes1 = R.drawable.icon_1_2,
                title = "Calender.io",
                welcomeMessage = "Welcome Michael!",
                headerText1 = "It'S Time to",
                headerText2 = "Organize your Day!",
                backgroundImageRes = R.drawable.picture_2,
                slideImageRes = R.drawable.slide
            ),
            PageData(
                imageRes = R.drawable.icon_11,
                imageRes1 = R.drawable.icon_1_2,
                title = "Calender.io",
                welcomeMessage = "Welcome Michael!",
                headerText1 = "It'S Time to",
                headerText2 = "Organize your Day!",
                backgroundImageRes = R.drawable.picture_2,
                slideImageRes = R.drawable.slide
            ),
            PageData(
                imageRes = R.drawable.icon_11,
                imageRes1 = R.drawable.icon_1_2,
                title = "Calender.io",
                welcomeMessage = "Welcome Michael!",
                headerText1 = "It'S Time to",
                headerText2 = "Organize your Day!",
                backgroundImageRes = R.drawable.picture_2,
                slideImageRes = R.drawable.slide
            )
        )
        viewPager.adapter = MyPagerAdapter(items)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicators(position)
            }
        })

    }

    private fun updateIndicators(position: Int) {
        indicators.forEachIndexed { index, imageView ->
            if (index == position) {
                imageView.setImageResource(R.drawable.indicator_active_1)
            } else {
                imageView.setImageResource(R.drawable.indicator_inactive_1)
            }
        }
    }
}
package com.example.calendarik

import android.view.View
import androidx.viewpager2.widget.ViewPager2

class ZoomOutPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        val scaleFactor = Math.max(0.85f, 1 - Math.abs(position))
        val pageWidth = view.width
        val pageHeight = view.height
        view.scaleX = scaleFactor
        view.scaleY = scaleFactor
        view.alpha = 0.5f + (scaleFactor - 0.5f) / (1 - 0.5f) * (1 - Math.abs(position))

        val verticalMargin = pageHeight * (1 - scaleFactor) / 2
        val horizontalMargin = pageWidth * (1 - scaleFactor) / 2
        if (position < 0) {
            view.translationX = horizontalMargin - verticalMargin / 2
        } else {
            view.translationX = -horizontalMargin + verticalMargin / 2
        }
    }
}

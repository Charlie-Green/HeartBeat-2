package by.vadim_churun.individual.heartbeat2.trans

import android.view.View
import androidx.viewpager2.widget.ViewPager2


/** Implements "cube" transition for [ViewPager2] with vertical orientation. **/
class VerticalCubePageTransformer: ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        page.pivotX = 0.5f*page.width
        page.pivotY = if(position < 0) page.height.toFloat() else 0f
        page.rotationX = -position*18f
    }
}
package by.vadim_churun.individual.heartbeat2.app.ui.common
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior


object UiUtils {
    fun navBarHeight(res: Resources, display: Display): Int {
        val idNavSize = res.getIdentifier("navigation_bar_height", "dimen", "android")
        if(idNavSize != 0)
            return res.getDimensionPixelSize(idNavSize)

        // Typical navigation bar smaller size is 48dp.
        val density = DisplayMetrics().also { display.getMetrics(it) }.density
        return 48f.times(density).toInt()
    }

    fun timeString(millis: Long): String {
        var remain = millis
        val h = remain/3600000L; remain -= h*3600000L
        val m = remain/60000L; remain -= m*60000L
        val s = remain/1000L
        val hs = if(h == 0L) "" else "$h:"
        val ms = "${m.toString().padStart(2, '0')}:"
        val ss = "$s".padStart(2, '0')
        return "$hs$ms$ss"
    }

    fun reportSystemUiVisibilityToFragments
    (fragmMan: FragmentManager, isSystemUiVisible: Boolean) {
        for(fragm in fragmMan.fragments) {
            if(fragm.isAdded && fragm is SystemUiOverlapped)
                fragm.onSystemUiVisibilityChanged(isSystemUiVisible)
        }
    }

    fun preventBottomSheetOverlap(bottomSheet: View, contentView: View) {
        fun setContentViewMargin(bottomMargin: Int) {
            val params = contentView.layoutParams as CoordinatorLayout.LayoutParams?
                ?: CoordinatorLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            params.bottomMargin = bottomMargin
            contentView.layoutParams = params
        }

        val behav = BottomSheetBehavior.from(bottomSheet)
        setContentViewMargin(behav.peekHeight)
        behav.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val deltaHeight = slideOffset.times(bottomSheet.height - behav.peekHeight).toInt()
                setContentViewMargin(behav.peekHeight + deltaHeight)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int)
            {   }
        })
    }
}
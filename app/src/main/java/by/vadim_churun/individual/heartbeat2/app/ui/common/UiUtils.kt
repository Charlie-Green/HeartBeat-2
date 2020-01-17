package by.vadim_churun.individual.heartbeat2.app.ui.common
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.Display
import androidx.fragment.app.FragmentManager


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

    fun doForServiceDependentFragments
    (fragmMan: FragmentManager, action: (dependent: ServiceDependent) -> Unit) {
        for(fragm in fragmMan.fragments) {
            if(fragm.isAdded && fragm is ServiceDependent)
                action(fragm)
        }
    }

    fun reportSystemUiVisibilityToFragments
    (fragmMan: FragmentManager, isSystemUiVisible: Boolean) {
        for(fragm in fragmMan.fragments) {
            if(fragm.isAdded && fragm is SystemUiOverlapped)
                fragm.onSystemUiVisibilityChanged(isSystemUiVisible)
        }
    }
}
package by.vadim_churun.individual.heartbeat2.app.ui

import android.animation.Animator
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.EditText
import android.widget.ImageView
import androidx.core.view.isVisible
import com.google.android.material.tabs.TabLayout
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import java.util.concurrent.TimeUnit


internal object MainActivitySearchHelper {
    //////////////////////////////////////////////////////////////////////////////////////////
    // FIELDS:

    private lateinit var iconView: ImageView
    private lateinit var queryView: EditText
    private lateinit var crossView: ImageView
    private lateinit var tabs: TabLayout
    private var bound = false
    private var expanded = false


    //////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    fun observableSearchQuery(): Observable<CharSequence> {
        if(!bound)
            throw IllegalStateException("Not bound")
        return queryView
            .textChanges()  // Omit redundant search requests when the user is typing.
            .debounce(200L, TimeUnit.MILLISECONDS)
    }


    //////////////////////////////////////////////////////////////////////////////////////////
    // ANIMATIONS:

    private fun ViewPropertyAnimator.configAndStart(onEnd: () -> Unit) {
        duration = 2000L
        setListener( object: Animator.AnimatorListener {
            override fun onAnimationStart(anim: Animator)  {   }
            override fun onAnimationRepeat(anim: Animator) {   }
            override fun onAnimationCancel(anim: Animator) {   }
            override fun onAnimationEnd(anim: Animator) {
                onEnd()
            }
        } )
        start()
    }

    private fun animateExpand() {
        expanded = true
        iconView.isEnabled = false

        tabs.pivotY = 0f                                     // Collapse to top
        tabs.animate().scaleY(0f).configAndStart {
            android.util.Log.v("HbSearch", "tabs.animate.scaleY(0f) ended")
            tabs.visibility = View.GONE
            crossView.visibility = View.INVISIBLE

            queryView.visibility = View.VISIBLE
            queryView.scaleX = 0f; queryView.pivotX = 0f    // Expand from start.
            queryView.animate().scaleX(1f).configAndStart {
                crossView.visibility = View.VISIBLE
                iconView.isEnabled = true
            }
        }
    }

    private fun animateIconify() {
        expanded = false
        iconView.isEnabled = false

        crossView.visibility = View.INVISIBLE
        queryView.pivotX = 0f                            // Collapse to start.
        queryView.animate().scaleX(0f).configAndStart {
            queryView.visibility = View.GONE
            crossView.visibility = View.GONE

            tabs.visibility = View.VISIBLE
            tabs.scaleY = 0f; tabs.pivotY = 0f           // Expand from top.
            tabs.animate().scaleY(1f).configAndStart {
                iconView.isEnabled = true
            }
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////
    // STATES:

    private fun setStateNow() {
        tabs.isVisible = !expanded
        queryView.isVisible = expanded
        crossView.isVisible = expanded
    }


    //////////////////////////////////////////////////////////////////////////////////////////
    // INITIALIZING LISTENERS:

    fun bind(
        searchIconView: ImageView,
        searchQueryView: EditText,
        closeIconView: ImageView,
        tabLayout: TabLayout
    ) {
        if(bound) return
        bound = true

        iconView = searchIconView; queryView = searchQueryView
        crossView = closeIconView; tabs = tabLayout

        setStateNow()
        iconView.setOnClickListener {
            if(!expanded)
                animateExpand()
        }
        closeIconView.setOnClickListener {
            if(queryView.text.isEmpty())
                animateIconify()
            else
                queryView.setText("")
        }
    }

    fun unbind() {
        if(!bound) return
        bound = false
        tabs.animate().cancel()
        queryView.animate().cancel()
        crossView.animate().cancel()
    }
}
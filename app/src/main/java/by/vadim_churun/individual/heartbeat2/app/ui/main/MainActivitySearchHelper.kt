package by.vadim_churun.individual.heartbeat2.app.ui.main

import android.animation.Animator
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.tabs.TabLayout
import com.jakewharton.rxbinding3.widget.textChangeEvents
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit


internal object MainActivitySearchHelper {
    //////////////////////////////////////////////////////////////////////////////////////////
    // FIELDS AND PROPERTIES:

    private lateinit var iconView: ImageView
    private lateinit var queryView: EditText
    private lateinit var crossView: ImageView
    private lateinit var tabs: TabLayout
    private val subjectExpanded = BehaviorSubject.create<Boolean>()
    private var bound = false
    private var lastSearchQuery = ""

    private val inputMan
        get() =  ContextCompat.getSystemService(
            queryView.context, InputMethodManager::class.java
        )!!

    val isViewExpanded
        get() = subjectExpanded.value ?: false


    //////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    fun observableSearchQuery(): Observable<CharSequence> {
        if(!bound)
            throw IllegalStateException("Not bound")
        return queryView
            .textChangeEvents() // Omit redundant search requests when the user is typing.
            .debounce(300L, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .map { event -> event.text }
            .map { query ->
                if(isViewExpanded) query else ""
            }.mergeWith(
                subjectExpanded.flatMap { expanded ->
                    if(expanded)
                        Observable.just(queryView.text)
                    else
                        Observable.empty()
                }
            )
    }


    //////////////////////////////////////////////////////////////////////////////////////////
    // ANIMATIONS:

    private fun ViewPropertyAnimator.configAndStart(onEnd: () -> Unit) {
        duration = 400L
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
        subjectExpanded.onNext(true)
        iconView.isEnabled = false

        tabs.pivotY = 0f                                     // Collapse to top
        tabs.animate().scaleY(0f).configAndStart {
            tabs.visibility = View.GONE
            crossView.visibility = View.INVISIBLE

            queryView.visibility = View.VISIBLE
            queryView.scaleX = 0f; queryView.pivotX = 0f    // Expand from start.
            queryView.animate().scaleX(1f).configAndStart {
                crossView.visibility = View.VISIBLE
                iconView.isEnabled = true
                queryView.requestFocus()
                this.inputMan.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            }
        }
    }

    private fun animateIconify() {
        subjectExpanded.onNext(false)
        iconView.isEnabled = false
        this.inputMan.hideSoftInputFromWindow(queryView.windowToken, 0)

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
        val expanded = this.isViewExpanded
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
        searchIconView.setOnClickListener {
            if(!isViewExpanded)
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
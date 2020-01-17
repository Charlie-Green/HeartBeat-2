package by.vadim_churun.individual.heartbeat2.app.ui.main

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.coordinatorlayout.widget.CoordinatorLayout
import by.vadim_churun.individual.heartbeat2.app.R
import by.vadim_churun.individual.heartbeat2.app.presenter.service.*
import by.vadim_churun.individual.heartbeat2.app.service.HeartBeatMediaService
import by.vadim_churun.individual.heartbeat2.app.ui.common.UiUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.main_activity.*


class HeartBeatMainActivity: AppCompatActivity(), ServiceBoundUI {
    /////////////////////////////////////////////////////////////////////////////////////////
    // MVI:

    private val presenter = ServiceBindingPresenter()

    override val boundContext: Context
        get() = this

    /* ServiceBoundUI */
    override fun onConnected(service: HeartBeatMediaService) {
        val fragmMan = super.getSupportFragmentManager()
        UiUtils.doForServiceDependentFragments(fragmMan) { dependent ->
            dependent.useBoundService(service)
        }
    }


    private fun bindPresenter() {
        presenter.bind(this)
    }

    private fun unbindPresenter() {
        val fragmMan = super.getSupportFragmentManager()
        UiUtils.doForServiceDependentFragments(fragmMan) { dependent ->
            dependent.notifyServiceUnbound()
        }
        presenter.unbind(this)
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    // UI:

    private var preventFragmentsOverlapCalled = false

    private fun hideSystemUi() {
        val FLAGS =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        val decorView = super.getWindow().decorView

        decorView.systemUiVisibility = FLAGS
        decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            val fragmMan = super.getSupportFragmentManager()
            UiUtils.reportSystemUiVisibilityToFragments(fragmMan, true)
            if(visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0)
                decorView.systemUiVisibility = FLAGS
        }
    }

    private fun preventFragmentsOverlap() {
        if(preventFragmentsOverlapCalled) return
        preventFragmentsOverlapCalled = true

        fun setSongsFragmentMargin(bottomMargin: Int) {
            val v = fragmSongs.requireView()
            val params = v.layoutParams as CoordinatorLayout.LayoutParams?
                ?: CoordinatorLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            params.bottomMargin = bottomMargin
            v.layoutParams = params
        }

        val vMediaControl = fragmMediaControl.requireView()
        val behav = BottomSheetBehavior.from(vMediaControl)
        setSongsFragmentMargin(behav.peekHeight)
        behav.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val deltaHeight = slideOffset.times(bottomSheet.height - behav.peekHeight).toInt()
                setSongsFragmentMargin(behav.peekHeight + deltaHeight)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int)
            {   }
        })
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.main_activity)
    }

    override fun onStart() {
        super.onStart()
        preventFragmentsOverlap()
        bindPresenter()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
    }

    override fun onStop() {
        unbindPresenter()
        super.onStop()
    }
}
package by.vadim_churun.individual.heartbeat2.ui.main

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.coordinatorlayout.widget.CoordinatorLayout
import by.vadim_churun.individual.heartbeat2.R
import by.vadim_churun.individual.heartbeat2.presenter.service.*
import by.vadim_churun.individual.heartbeat2.service.HeartBeatMediaService
import by.vadim_churun.individual.heartbeat2.ui.UiUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.main_activity.*


class HeartBeatMainActivity: AppCompatActivity(), ServiceBoundUI {
    /////////////////////////////////////////////////////////////////////////////////////////
    // MVI:

    private val presenter = ServiceBindingPresenter()

    override val boundContext: Context
        get() = this

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

    private fun setupWindowFlags() {
        super.getWindow().decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
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
        setupWindowFlags()
        super.setContentView(R.layout.main_activity)
    }

    override fun onStart() {
        super.onStart()
        preventFragmentsOverlap()
        bindPresenter()
    }

    override fun onStop() {
        unbindPresenter()
        super.onStop()
    }
}
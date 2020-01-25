package by.vadim_churun.individual.heartbeat2.app.ui.main

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import by.vadim_churun.individual.heartbeat2.app.R
import by.vadim_churun.individual.heartbeat2.app.presenter.service.*
import by.vadim_churun.individual.heartbeat2.app.service.HeartBeatMediaService
import by.vadim_churun.individual.heartbeat2.app.ui.common.*
import com.google.android.material.tabs.TabLayoutMediator
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragm_media_control.*
import kotlinx.android.synthetic.main.main_activity.*


class HeartBeatMainActivity: AppCompatActivity(), ServiceBoundUI, ServiceSource {
    /////////////////////////////////////////////////////////////////////////////////////////
    // MVI:

    private val presenter = ServiceBindingPresenter()
    private val subjectService = BehaviorSubject.create<HeartBeatMediaService>()

    /* ServiceSource */
    override fun observableService(): Observable<HeartBeatMediaService>
        = subjectService


    /* ServiceBoundUI */
    override val boundContext: Context
        get() = this

    /* ServiceBoundUI */
    override fun onConnected(service: HeartBeatMediaService) {
        // Notify the dependent fragments that a service instance is available.
        subjectService.onNext(service)
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
        UiUtils.preventBottomSheetOverlap(fragmMediaControl.requireView(), vlltTabsPart)
    }

    private fun setupTabs() {
        val tabsAdapter = MainActivityTabsAdapter(this)
        tabPager.adapter = tabsAdapter
        TabLayoutMediator(tabLayout, tabPager) { tab, position ->
            tab.text = tabsAdapter.labelAt(super.getResources(), position)
        }.attach()
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.main_activity)
        setupTabs()
    }

    override fun onStart() {
        super.onStart()
        preventFragmentsOverlap()
        presenter.bind(this)
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
    }

    override fun onStop() {
        presenter.unbind(this)
        super.onStop()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        tabPager.requestDisallowInterceptTouchEvent(true)
        return true
    }
}
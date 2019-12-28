package by.vadim_churun.individual.heartbeat2.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import by.vadim_churun.individual.heartbeat2.R


class HeartBeatMainActivity: AppCompatActivity() {
    private fun setupWindowFlags() {
        super.getWindow().decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindowFlags()
        super.setContentView(R.layout.main_activity)
    }
}
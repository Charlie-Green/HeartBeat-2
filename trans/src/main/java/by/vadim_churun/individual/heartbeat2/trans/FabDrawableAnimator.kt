package by.vadim_churun.individual.heartbeat2.trans

import android.animation.Animator
import android.animation.ValueAnimator
import com.google.android.material.floatingactionbutton.FloatingActionButton


/** Implements fade out/fade in replacement of the given [FloatingActionButton]'s drawable. **/
class FabDrawableAnimator(
    val target: FloatingActionButton
) {
    private fun animator(int1: Int, int2: Int)
        = ValueAnimator.ofInt(int1, int2).apply {
            addUpdateListener { animator ->
                target.drawable.alpha = animator.animatedValue as Int
            }
            duration = 600L
        }

    fun start(nextDrawableID: Int) {
        val anim1 = animator(255, 0)  // Fade out.
        anim1.addListener( object: Animator.AnimatorListener {
            override fun onAnimationStart(animer:  Animator) {   }
            override fun onAnimationRepeat(animer: Animator) {   }
            override fun onAnimationCancel(animer: Animator) {   }
            override fun onAnimationEnd(animer: Animator) {
                target.setImageResource(nextDrawableID)
                val anim2 = animator(0, 255)  // Fade in
                anim2.start()
            }
        } )
        anim1.start()
    }
}
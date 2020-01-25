package by.vadim_churun.individual.heartbeat2.trans

import android.animation.AnimatorInflater
import android.animation.LayoutTransition
import android.view.ViewGroup


/** [LayoutTransition] that animates its target view to appear
  * by scaling it horizontally from its very end. **/
class HorizontalFromEndTransition(
    private val target: ViewGroup
): LayoutTransition() {
    private fun setAnimator(transitionType: Int, animatorID: Int) {
        AnimatorInflater.loadAnimator(target.context, animatorID)
            .also { super.setAnimator(transitionType, it) }
    }

    init {
        target.measure(0, 0); target.pivotX = target.measuredWidth.toFloat()
        setAnimator(LayoutTransition.APPEARING,    R.animator.horizontal_from_end_appear)
        setAnimator(LayoutTransition.DISAPPEARING, R.animator.horizontal_from_end_disappear)

        val res = target.context.resources
        val duration = res.getInteger(R.integer.horizontal_from_end_duration).toLong()
        super.setDuration(LayoutTransition.APPEARING,           duration)
        super.setDuration(LayoutTransition.DISAPPEARING,        duration)
        super.setDuration(LayoutTransition.CHANGE_APPEARING,    duration)
        super.setDuration(LayoutTransition.CHANGE_DISAPPEARING, duration)
    }

    companion object {
        fun setOn(target: ViewGroup) {
            val existingTransition = target.layoutTransition
            if(existingTransition !is HorizontalFromEndTransition)
                target.layoutTransition = HorizontalFromEndTransition(target)
        }
    }
}
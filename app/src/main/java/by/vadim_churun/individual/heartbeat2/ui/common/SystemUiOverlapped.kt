package by.vadim_churun.individual.heartbeat2.ui.common


/** Interface for a UI class whose views can be overlapped by the system's navigation bar. **/
interface SystemUiOverlapped {
    fun onSystemUiVisibilityChanged(isVisible: Boolean)
}
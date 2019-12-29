package by.vadim_churun.individual.heartbeat2.ui


object UiUtils {
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
}
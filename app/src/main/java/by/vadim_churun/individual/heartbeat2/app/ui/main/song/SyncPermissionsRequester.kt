package by.vadim_churun.individual.heartbeat2.app.ui.main.song

import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import by.vadim_churun.individual.heartbeat2.app.R


class SyncPermissionsRequester(private val requestingFragment: Fragment) {
    private val REQCODE = 135

    private fun actualRequest(perms: List<String>) {
        val permsArray = Array(perms.size) { perms[it] }
        requestingFragment.requestPermissions(permsArray, REQCODE)
    }

    fun request(permissions: List<String>, songsSourceName: CharSequence) {
        val showRationale = permissions.find { perm ->
            requestingFragment.shouldShowRequestPermissionRationale(perm)
        }?.let { true } ?: false
        if(showRationale) {
            val msg = requestingFragment
                .getString(R.string.permissions_needed_f, songsSourceName)
            AlertDialog.Builder(requestingFragment.requireContext())
                .setTitle(R.string.one_more_step)
                .setMessage(msg)
                .setPositiveButton(R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                    actualRequest(permissions)
                }.setNegativeButton(R.string.not_request_permissions) { dialog, _ ->
                    dialog.dismiss()
                }.show()
        } else {
            actualRequest(permissions)
        }
    }

    fun handleResult
    (requestCode: Int, grantResults: IntArray, onAllGranted: () -> Unit) {
        if(requestCode != REQCODE) return
        grantResults.find { result ->
            result != PackageManager.PERMISSION_GRANTED
        } ?: onAllGranted()
    }
}
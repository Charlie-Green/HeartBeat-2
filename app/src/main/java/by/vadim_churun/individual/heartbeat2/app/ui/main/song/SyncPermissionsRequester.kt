package by.vadim_churun.individual.heartbeat2.app.ui.main.song

import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import by.vadim_churun.individual.heartbeat2.app.R
import by.vadim_churun.individual.heartbeat2.app.model.state.SyncState
import by.vadim_churun.individual.heartbeat2.app.presenter.song.SongsCollectionAction


internal class SyncPermissionsRequester(
    private val requestingFragment: Fragment ) {

    private fun actualRequest(sourceCode: Byte, perms: List<String>) {
        val permsArray = Array(perms.size) { perms[it] }
        requestingFragment.requestPermissions(permsArray, sourceCode.toInt())
    }

    fun request(state: SyncState.MissingPermissions) {
        if(state.consumed) return

        val showRationale = state.permissions.find { perm ->
            requestingFragment.shouldShowRequestPermissionRationale(perm)
        }?.let { true } ?: false
        if(showRationale) {
            val msg = requestingFragment
                .getString(R.string.permissions_needed_f, state.sourceName)
            AlertDialog.Builder(requestingFragment.requireContext())
                .setTitle(R.string.one_more_step)
                .setMessage(msg)
                .setPositiveButton(R.string.ok) { dialog, _ ->
                    state.consumed = true
                    dialog.dismiss()
                    actualRequest(state.sourceCode, state.permissions)
                }.setNegativeButton(R.string.not_request_permissions) { dialog, _ ->
                    state.consumed = true
                    dialog.dismiss()
                }.show()
        } else {
            actualRequest(state.sourceCode, state.permissions)
        }
    }

    fun handleResult(requestCode: Int, grantResults: IntArray) {
        if((requestCode and 0x11111100) != 0x00000000)
            return  // This Int cannot be properly converted to Byte.
        val sourceCode = requestCode.toByte()

        val isAllGranted = grantResults.find { result ->
            result != PackageManager.PERMISSION_GRANTED
        }?.let { false } ?: true

        SongsCollectionSubjects.SUBMIT_PERMISSION_RESULT.onNext(
            SongsCollectionAction.SubmitPermissionsResult(sourceCode, isAllGranted) )
    }
}
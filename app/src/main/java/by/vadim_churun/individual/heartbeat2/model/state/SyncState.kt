package by.vadim_churun.individual.heartbeat2.model.state


/** MVI states for synchronization with [SongSource]s. **/
sealed class SyncState {
    class Active: SyncState()
    class NotSyncing: SyncState()
    class Error(
        val shouldDisturbUser: Boolean,
        val sourceName: String,
        val cause: Throwable
    ): SyncState() { var consumed = false }
    class MissingPermissions(val sourceName: String, val permissions: List<String>): SyncState()
}
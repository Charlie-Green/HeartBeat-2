package by.vadim_churun.individual.heartbeat2.player

import by.vadim_churun.individual.heartbeat2.shared.Song


/** This interface is used to abstractiate usage of player from its implementation. **/
interface HeartBeatPlayer {
    /** Playback position in millisecond.
      * If the player is released, the getter returns a negative number. **/
    var position: Long

    /** Playback rate, where 1.0 is the normal value.
      * If the player is released, the getter returns the last set value, or the default value. **/
    var rate: Float

    /** Relative playback volume, from 0.0 to 1.0.
      * For behaviour on released player, see [rate] property. **/
    var volume: Float

    /** Play the given song from the beginning. **/
    fun play(song: Song)

    /** Pause playback if it's playing, or resume if it's paused.
      * If the player is released, call results in no-op. **/
    fun pauseOrResume()

    /** Stop playback and release resources held by the player. **/
    fun release()
}
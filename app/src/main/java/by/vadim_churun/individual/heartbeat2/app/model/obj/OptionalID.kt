package by.vadim_churun.individual.heartbeat2.app.model.obj


/** Wraps a nullable integer representing ID of an entity or "no entity".
  * This is needed because RxJava 2 doesn't let null downstream. **/
class OptionalID private constructor(
    val idOrNull: Int?
) {
    companion object {
        fun wrap(idOrNull: Int?)
            = OptionalID(idOrNull)
    }
}
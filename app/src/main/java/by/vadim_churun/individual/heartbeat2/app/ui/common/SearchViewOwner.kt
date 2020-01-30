package by.vadim_churun.individual.heartbeat2.app.ui.common

import io.reactivex.Observable


/** Interface for a class that provides search UI but does not trigger the search itself,
  * instead, supplying search queries to another UI component. **/
interface SearchViewOwner {
    companion object {
        val KEY_COMPONENT_ID
            get() = "searchUi"
    }

    fun observableSearchQuery(componentID: Int): Observable<CharSequence>
}
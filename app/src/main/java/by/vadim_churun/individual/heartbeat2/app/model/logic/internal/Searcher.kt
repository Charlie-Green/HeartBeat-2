package by.vadim_churun.individual.heartbeat2.app.model.logic.internal

import javax.inject.Inject


/** Implements the common search algorithm. Not thread safe. **/
class Searcher<ItemType> @Inject constructor() {
    private var source: List<ItemType>? = null
    private lateinit var tokens: List< List<String> >

    fun prepare(dataSource: List<ItemType>, tokensForItem: (ItemType) -> List<String>) {
        source = dataSource
        tokens = dataSource.map(tokensForItem)
    }

    private fun checkToken
    (requiredToken: CharSequence, candidates: List<CharSequence>): Boolean {
        return candidates.find { candidate ->
            candidate.contains(requiredToken, ignoreCase = true)
        } != null
    }

    fun search(query: CharSequence): List<ItemType> {
        val dataSource = source
            ?: throw IllegalStateException("prepare() wasn't called")
        if(query.isEmpty())
            return dataSource

        val queryTokens = query.split(' ').filter { it.isNotEmpty() }
        return dataSource.filterIndexed { index, _ ->
            for(queryToken in queryTokens) {
                if( checkToken(queryToken, tokens[index]) == false )
                    return@filterIndexed false
            }
            return@filterIndexed true
        }
    }
}
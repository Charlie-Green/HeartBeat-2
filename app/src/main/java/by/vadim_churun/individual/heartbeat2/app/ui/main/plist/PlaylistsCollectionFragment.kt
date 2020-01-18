package by.vadim_churun.individual.heartbeat2.app.ui.main.plist

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import by.vadim_churun.individual.heartbeat2.app.R
import by.vadim_churun.individual.heartbeat2.app.ui.common.VerticalCubePageTransformer
import kotlinx.android.synthetic.main.plists_collection_fragment.*


class PlaylistsCollectionFragment: Fragment() {
    /////////////////////////////////////////////////////////////////////////////////////////
    // UI:

    private fun setupPager() {
        pagerPlists.adapter = PlaylistsCollectionAdapter()
        pagerPlists.setPageTransformer(VerticalCubePageTransformer())
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onCreateView
    (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.plists_collection_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupPager()
    }
}
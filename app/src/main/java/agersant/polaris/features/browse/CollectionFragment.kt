package agersant.polaris.features.browse

import agersant.polaris.PolarisApplication
import agersant.polaris.R
import agersant.polaris.api.API
import agersant.polaris.databinding.FragmentCollectionBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController

class CollectionFragment : Fragment() {
    private lateinit var random: View
    private lateinit var recent: View
    private lateinit var api: API

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setHasOptionsMenu(true)

        val state = PolarisApplication.getState()
        api = state.api

        val binding = FragmentCollectionBinding.inflate(inflater)
        random = binding.random
        recent = binding.recent
        binding.directories.setOnClickListener(this::browseDirectories)
        binding.random.setOnClickListener(this::browseRandom)
        binding.recent.setOnClickListener(this::browseRecent)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        updateButtons()
    }

    override fun onResume() {
        super.onResume()
        updateButtons()
    }

    private fun browseDirectories(view: View) {
        val args = Bundle()
        args.putSerializable(BrowseFragment.NAVIGATION_MODE, BrowseFragment.NavigationMode.PATH)
        view.findNavController().navigate(R.id.action_nav_collection_to_nav_browse, args)
    }

    private fun browseRandom(view: View) {
        val args = Bundle()
        args.putSerializable(BrowseFragment.NAVIGATION_MODE, BrowseFragment.NavigationMode.RANDOM)
        view.findNavController().navigate(R.id.action_nav_collection_to_nav_browse, args)
    }

    private fun browseRecent(view: View) {
        val args = Bundle()
        args.putSerializable(BrowseFragment.NAVIGATION_MODE, BrowseFragment.NavigationMode.RECENT)
        view.findNavController().navigate(R.id.action_nav_collection_to_nav_browse, args)
    }

    private fun updateButtons() {
        val isOffline = api.isOffline
        random.isEnabled = !isOffline
        recent.isEnabled = !isOffline
    }
}

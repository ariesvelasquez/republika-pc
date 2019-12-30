package ariesvelasquez.com.republikapc.ui.dashboard.rpc

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.ui.dashboard.DashboardFragment
import ariesvelasquez.com.republikapc.ui.dashboard.SettingsFragment
import ariesvelasquez.com.republikapc.ui.dashboard.rpc.items.PartsFragment
import ariesvelasquez.com.republikapc.ui.dashboard.rpc.rigs.RigsFragment
import kotlinx.android.synthetic.main.fragment_republika_pc.view.*

class RepublikaPCFragment : DashboardFragment() {

    private lateinit var rootView: View

    private var listener: OnRepublikaPCInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_republika_pc, container, false)

        setupTabAndPager()

        return rootView
    }

    private fun setupTabAndPager() {

        val viewPagerFragmentAdapter =
            RPCFragmentPagerAdapter(childFragmentManager)
        rootView.viewPagerRpc.setPagingEnabled(false)
        rootView.viewPagerRpc.adapter = viewPagerFragmentAdapter
        rootView.viewPagerRpc.offscreenPageLimit = viewPagerFragmentAdapter.count - 1

        rootView.tabLayoutRpc.setupWithViewPager(rootView.viewPagerRpc)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRepublikaPCInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnRepublikaPCInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onUserLoggedIn() {

    }

    override fun onUserLoggedOut() {

    }

    private class RPCFragmentPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> RigsFragment.newInstance()
                1 -> SettingsFragment.newInstance()
                else -> PartsFragment.newInstance()
            }
        }

        override fun getCount(): Int {
            return 3
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> "Rigs"
                1 -> "Saved"
                else ->  "Parts"
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnRepublikaPCInteractionListener {

    }

    companion object {

        @JvmStatic
        fun newInstance() =
            RepublikaPCFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}
package ariesvelasquez.com.republikapc.ui.dashboard.rpc.rigs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ariesvelasquez.com.republikapc.R

private const val ARG_PARAM1 = "param1"

class RigListFragment : DialogFragment() {

    private lateinit var rootView: View

    private var listener: OnRigListFragmentListener? = null

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
        rootView = inflater.inflate(R.layout.fragment_rig_list, container, false)



        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRigListFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnRigFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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
    interface OnRigListFragmentListener {
        fun onAddedToRig(rigId: String)
    }

    companion object {

        const val TAG = "RIG_LIST_FRAGMENT"

        @JvmStatic
        fun newInstance() =
            RigListFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}

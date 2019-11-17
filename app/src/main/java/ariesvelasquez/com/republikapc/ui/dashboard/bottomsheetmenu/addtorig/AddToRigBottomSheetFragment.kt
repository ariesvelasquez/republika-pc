package ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.addtorig

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.DashboardViewModel
import ariesvelasquez.com.republikapc.utils.ServiceLocator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class AddToRigBottomSheetFragment : BottomSheetDialogFragment() {

    val TAG = "RigCreatorBottomSheetFragment"

    val dashboardViewModel: DashboardViewModel by activityViewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repo = ServiceLocator.instance(context!!)
                    .getDashboardRepository()
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(repo) as T
            }
        }
    }

    private lateinit var rootView: View

    private var listener: AddToRigBottomSheetFragmentListener? = null

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_add_to_rig_bottom_sheet, container, false)

        // Setup something...
        setupRigList()

        return rootView
    }

    private fun setupRigList() {
        // Sign Out Event.

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AddToRigBottomSheetFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement AddToRigBottomSheetFragment")
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
    interface AddToRigBottomSheetFragmentListener {

    }

    companion object {

        @JvmStatic
        fun newInstance() =
            AddToRigBottomSheetFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}
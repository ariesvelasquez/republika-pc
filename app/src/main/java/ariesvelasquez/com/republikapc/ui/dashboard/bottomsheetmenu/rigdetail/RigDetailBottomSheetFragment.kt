package ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.rigdetail

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.DashboardViewModel
import ariesvelasquez.com.republikapc.utils.ServiceLocator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_rig_detail_bottom_sheet.view.*


class RigDetailBottomSheetFragment : BottomSheetDialogFragment() {

    val TAG = "RigDetailBottomSheetFragment"
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

    private lateinit var rigRef: Rig
    private lateinit var rootView: View

    private var listener: OnRigDetailInteractionListener? = null

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            rigRef = Gson().fromJson(it.getString(RAW_RIG_REFERENCE), Rig::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_rig_detail_bottom_sheet, container, false)

        setupOnClickEvents()

        return rootView
    }

    private fun setupOnClickEvents() {
        rootView.buttonDeleteRig.setOnClickListener {
            dashboardViewModel.deleteRig(rigRef.id)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRigDetailInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnRigDetailInteractionListener")
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
    interface OnRigDetailInteractionListener {

    }

    companion object {
        const val RAW_RIG_REFERENCE = "rawRigRef"

        @JvmStatic
        fun newInstance(rawRigRef: String) =
            RigDetailBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(RAW_RIG_REFERENCE, rawRigRef)
                }
            }
    }
}

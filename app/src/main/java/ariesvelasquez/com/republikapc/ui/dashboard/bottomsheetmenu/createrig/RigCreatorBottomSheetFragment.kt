package ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.createrig

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import ariesvelasquez.com.republikapc.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_rig_creator_bottom_sheet.view.*


class RigCreatorBottomSheetFragment : BottomSheetDialogFragment() {

    val TAG = "RigCreatorBottomSheetFragment"

    private lateinit var rootView: View

    private lateinit var mBehavior: BottomSheetBehavior<View>

    private var listener: RigCreatorBottomSheetInteractionListener? = null

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_rig_creator_bottom_sheet, container, false)

        // Setup something...
        setupViewEvents()

        return rootView
    }

    private fun setupViewEvents() {
        // Sign Out Event.
        rootView.buttonNewRigCreated.setOnClickListener {
            setButtonState(false)
            validate()
        }
    }

    private fun validate() {
        // Check if field is not null
        when {
            rootView.editTextRigName.text.toString().isEmpty() -> {
                setButtonState(true)
                Toast.makeText(context, "Please enter your Rig Name", Toast.LENGTH_SHORT).show()
            }
            else -> listener?.onNewRigCreated(rootView.editTextRigName.text.toString())
        }
    }

    fun setButtonState(enable: Boolean) {
        rootView.buttonNewRigCreated.isEnabled = enable
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RigCreatorBottomSheetInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement RigCreatorBottomSheetInteractionListener")
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
    interface RigCreatorBottomSheetInteractionListener {
        fun onNewRigCreated(rigName: String)
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            RigCreatorBottomSheetFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}
package ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.console

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.DashboardViewModel
import ariesvelasquez.com.republikapc.utils.ServiceLocator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_bottom_sheet_console.view.*
import timber.log.Timber


class ConsoleBottomSheetFragment : BottomSheetDialogFragment() {

    val TAG = "ConsoleBottomSheetFragment"

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

    private var listener: ConsoleBottomSheetInteractionListener? = null

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

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
        rootView = inflater.inflate(R.layout.fragment_bottom_sheet_console, container, false)

        // Setup something...

        // Check for User Status
        dashboardViewModel.isUserSignedIn.observe(this, Observer { isUserLoggedIn ->
            if (isUserLoggedIn) {
                setupLoggedInLayout()
                // Todo: For improvement, implement databinding
                Timber.e("Name: " + dashboardViewModel.firebaseUserModel.value?.displayName)
            } else {
                setupLoggedOutLayout()
//                onUserLoggedOut()
            }
        })

        setupViewEvents()

        return rootView
    }

    private fun setupViewEvents() {
        // Sign Out Event.
        rootView.imageViewLogOut.setOnClickListener { listener?.onLogoutInvoked() }
        rootView.buttonSignIn.setOnClickListener { listener?.onLoginInvoked() }
    }

    private fun setupLoggedInLayout() {
        val userData = dashboardViewModel.firebaseUserModel.value
        rootView.constraintLayoutSignedIn.visibility = View.VISIBLE
        rootView.constraintLayoutLoggedOutLayout.visibility = View.GONE

        // Display Name
        rootView.textViewDisplayName.text = userData?.displayName
        // Email
        rootView.textViewEmail.text = userData?.email

        rootView.buttonCreateRig.setOnClickListener { listener?.onCreateRigInvoked() }
    }

    private fun setupLoggedOutLayout() {
        rootView.constraintLayoutSignedIn.visibility = View.GONE
        rootView.constraintLayoutLoggedOutLayout.visibility = View.VISIBLE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ConsoleBottomSheetInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ConsoleBottomSheetInteractionListener")
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
    interface ConsoleBottomSheetInteractionListener {
        fun onCreateRigInvoked()
        fun onCreatePartInvoked()
        fun onLogoutInvoked()
        fun onLoginInvoked()
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            ConsoleBottomSheetFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}

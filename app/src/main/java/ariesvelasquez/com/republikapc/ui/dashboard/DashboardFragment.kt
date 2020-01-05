package ariesvelasquez.com.republikapc.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.DashboardViewModel
import ariesvelasquez.com.republikapc.utils.ServiceLocator
import timber.log.Timber

abstract class DashboardFragment: Fragment() {

    abstract fun onUserLoggedIn()
    abstract fun onUserLoggedOut()

    protected var mIsUserLoggedIn: Boolean = false
    protected var mIsRigInitialized: Boolean = false
    protected var mIsSavedInitialized: Boolean = false

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Check for User Status
        dashboardViewModel.isUserSignedIn.observe( viewLifecycleOwner, Observer { isUserLoggedIn ->
            mIsUserLoggedIn = isUserLoggedIn

            if (isUserLoggedIn) {
                onUserLoggedIn()
            } else {
                onUserLoggedOut()
            }
        })

        dashboardViewModel.isRigsInitialized.observe( viewLifecycleOwner, Observer {
            mIsRigInitialized = it
        })

        dashboardViewModel.isSavedInitialized.observe( viewLifecycleOwner, Observer {
            mIsSavedInitialized = it
        })

        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
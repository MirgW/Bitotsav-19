package `in`.bitotsav.profile.ui

import `in`.bitotsav.NavBitotsavDirections
import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentProfileBinding
import `in`.bitotsav.databinding.ItemRegistrationHistoryBinding
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.profile.data.RegistrationHistoryItem
import `in`.bitotsav.shared.ui.SimpleRecyclerViewAdapter
import `in`.bitotsav.shared.utils.executeAfter
import `in`.bitotsav.shared.utils.setObserver
import `in`.bitotsav.teams.data.BasicTeam
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.sharedViewModel

class ProfileFragment : Fragment() {

    companion object {
        private const val TAG = "ProfileF"
    }

    private val profileViewModel by sharedViewModel<ProfileViewModel>()

    private val adapter by lazy {
        SimpleRecyclerViewAdapter<RegistrationHistoryItem>(
            { inflater, parent, bool ->
                ItemRegistrationHistoryBinding.inflate(inflater, parent, bool)
            },
            { itemBinding, registrationHistoryItem ->
                (itemBinding as ItemRegistrationHistoryBinding).executeAfter {
                    this.item = registrationHistoryItem
                    this.color = profileViewModel.mColor
                    this.listener = getHistoryEventClickListener(registrationHistoryItem)
                    this.teamListener = getHistoryTeamClickListener(registrationHistoryItem)
                    lifecycleOwner = this@ProfileFragment
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        profileViewModel.mColor = TypedValue().apply {
            activity?.theme?.resolveAttribute(R.attr.colorPrimary, this, true)
        }.data

        when (CurrentUser.isLoggedIn) {
            true -> profileViewModel.syncUser()
            false -> findNavController().navigate(R.id.action_destProfile_to_destLogin)
        }

        return FragmentProfileBinding.inflate(inflater, container, false)
            .apply {
                viewModel = profileViewModel
                lifecycleOwner = this@ProfileFragment
                content.registrations.adapter = adapter.apply {
                    submitList(profileViewModel.user.value?.getRegistrationHistory())
                }
                setObservers()
                setClickListeners(this)
            }
            .root
    }

    private fun setObservers() {
        profileViewModel.user.setObserver(viewLifecycleOwner) { user ->
            Log.d(TAG, "${user?.name} received")
            with(adapter) {
                submitList(profileViewModel.user.value?.getRegistrationHistory())
                notifyDataSetChanged()
            }
        }

        profileViewModel.loggedOut.setObserver(viewLifecycleOwner) { loggedOut ->
            if (loggedOut) {
                profileViewModel.loggedOut.value = false
                findNavController().navigate(R.id.action_destProfile_to_destLogin)
            }
        }
    }

    private fun setClickListeners(binding: FragmentProfileBinding) {
        binding.content.register.setOnClickListener {
            findNavController().navigate(
                ProfileFragmentDirections.registerForChampionship()
            )
        }
        binding.content.championshipTeam.setOnClickListener {
            binding.content.user?.let { user ->
                BasicTeam(
                    user.championshipTeam ?: "",
                    user.getChampionshipTeamMembers().map {
                        "${it.name}\n${it.bitotsavId} • ${it.email}"
                    }.toTypedArray()
                ).showDialog(it.context)
            }
        }
    }

    private fun getHistoryEventClickListener(registrationHistoryItem: RegistrationHistoryItem) =
        View.OnClickListener {
            it.findNavController().navigate(
                NavBitotsavDirections.actionGlobalDestEventDetail(registrationHistoryItem.eventId)
            )
        }

    private fun getHistoryTeamClickListener(registrationHistoryItem: RegistrationHistoryItem) =
        View.OnClickListener {
            with(registrationHistoryItem) {
                BasicTeam(teamName, members.toTypedArray()).showDialog(it.context)
            }
        }


    override fun onDestroyView() {
        profileViewModel.waitingForLogout.value = false
        profileViewModel.waitingForRegistration.value = false
        super.onDestroyView()
    }
}

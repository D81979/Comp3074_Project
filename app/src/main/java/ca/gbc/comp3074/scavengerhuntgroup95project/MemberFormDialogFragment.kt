package ca.gbc.comp3074.scavengerhuntgroup95project

import android.app.Application
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ca.gbc.comp3074.scavengerhuntgroup95project.data.HuntDatabase
import ca.gbc.comp3074.scavengerhuntgroup95project.data.HuntRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MemberFormDialogFragment : DialogFragment() {

    private lateinit var model: MemberFormViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model = ViewModelProvider(this)[MemberFormViewModel::class.java]

        val memberId = requireArguments().getLong(ARG_MEMBER_ID)
        model.load(memberId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.dialog_member_form,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val title = view.findViewById<TextView>(R.id.tvMemberFormTitle)
        val nameInput = view.findViewById<EditText>(R.id.etMemberName)
        val emailInput = view.findViewById<EditText>(R.id.etMemberEmail)
        val phoneInput = view.findViewById<EditText>(R.id.etMemberPhone)
        val status = view.findViewById<TextView>(R.id.tvMemberFormStatus)
        val saveButton = view.findViewById<Button>(R.id.btnSaveMember)
        val cancelButton = view.findViewById<Button>(R.id.btnCancelMember)
        val deleteButton = view.findViewById<Button>(R.id.btnDeleteMember)

        title.text =
            if (model.memberId == 0L) "Add Member" else "Edit Member"

        // Restore text after rotating the device.
        nameInput.setText(model.name)
        emailInput.setText(model.email)
        phoneInput.setText(model.phone)

        nameInput.doAfterTextChanged {
            model.name = it.toString()
        }

        emailInput.doAfterTextChanged {
            model.email = it.toString()
        }

        phoneInput.doAfterTextChanged {
            model.phone = it.toString()
        }

        saveButton.setOnClickListener {
            nameInput.error = null
            emailInput.error = null

            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()

            var valid = true

            if (name.isEmpty()) {
                nameInput.error = "Enter a member name"
                valid = false
            }

            if (
                email.isNotEmpty() &&
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            ) {
                emailInput.error = "Enter a valid email address"
                valid = false
            }

            if (valid) {
                model.save()
            } else if (name.isEmpty()) {
                nameInput.requestFocus()
            } else {
                emailInput.requestFocus()
            }
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        // First tap asks for confirmation inside the form.
        deleteButton.setOnClickListener {
            model.askToDelete()
        }

        model.state.observe(viewLifecycleOwner) { state ->
            if (state.completed) {
                parentFragmentManager.setFragmentResult(
                    RESULT_KEY,
                    Bundle().apply {
                        putLong("member_id", state.resultId)
                        putBoolean("deleted", state.deleted)
                    }
                )

                dismiss()
                return@observe
            }

            // Fill the form after the saved member has loaded.
            if (nameInput.text.toString() != model.name) {
                nameInput.setText(model.name)
            }
            if (emailInput.text.toString() != model.email) {
                emailInput.setText(model.email)
            }
            if (phoneInput.text.toString() != model.phone) {
                phoneInput.setText(model.phone)
            }

            val canEdit = state.ready && !state.busy

            nameInput.isEnabled = canEdit
            emailInput.isEnabled = canEdit
            phoneInput.isEnabled = canEdit
            saveButton.isEnabled = canEdit
            cancelButton.isEnabled = !state.busy
            deleteButton.isEnabled = canEdit

            // Prevent closing halfway through a database operation.
            isCancelable = !state.busy
            dialog?.setCanceledOnTouchOutside(false)

            saveButton.text = when {
                state.busy -> "Please wait..."
                !state.ready -> "Loading..."
                model.memberId == 0L -> "Save Member"
                else -> "Save Changes"
            }

            deleteButton.visibility =
                if (model.memberId > 0L) View.VISIBLE else View.GONE

            deleteButton.text =
                if (state.confirmDelete) "Confirm Delete" else "Delete Member"

            val message = when {
                state.error != null -> state.error
                state.confirmDelete ->
                    "Delete this member permanently? Tap Confirm Delete to continue."
                else -> ""
            }

            status.text = message
            status.visibility =
                if (message.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()

        // Fit the dialog to the available screen space.
        dialog?.window?.apply {
            val width = (resources.displayMetrics.widthPixels * 0.92).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            )
        }

        dialog?.setCanceledOnTouchOutside(false)
    }

    companion object {
        private const val ARG_MEMBER_ID = "member_id"
        const val RESULT_KEY = "member_form_result"
        const val TAG = "MemberFormDialog"

        // Pass 0 to add, or an existing ID to edit.
        fun newInstance(memberId: Long = 0L): MemberFormDialogFragment {
            return MemberFormDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_MEMBER_ID, memberId)
                }
            }
        }
    }
}

data class MemberFormState(
    val ready: Boolean = false,
    val busy: Boolean = false,
    val error: String? = null,
    val confirmDelete: Boolean = false,
    val completed: Boolean = false,
    val resultId: Long = 0L,
    val deleted: Boolean = false
)

class MemberFormViewModel(
    application: Application,
    private val savedState: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = HuntRepository(
        HuntDatabase.getDatabase(application)
    )

    private val mutableState = MutableLiveData(MemberFormState())
    val state: LiveData<MemberFormState> = mutableState

    private var started = false

    var memberId: Long = 0L
        private set

    // Keep unsaved text when the form is recreated.
    var name: String
        get() = savedState["draft_name"] ?: ""
        set(value) {
            savedState["draft_name"] = value
        }

    var email: String
        get() = savedState["draft_email"] ?: ""
        set(value) {
            savedState["draft_email"] = value
        }

    var phone: String
        get() = savedState["draft_phone"] ?: ""
        set(value) {
            savedState["draft_phone"] = value
        }

    fun load(id: Long) {
        if (started) return
        started = true
        memberId = id

        if (id < 0L) {
            mutableState.value = MemberFormState(
                error = "Invalid member ID. Close this form and try again."
            )
            return
        }

        if (id == 0L || savedState.get<Boolean>("draft_ready") == true) {
            savedState["draft_ready"] = true
            mutableState.value = MemberFormState(ready = true)
            return
        }

        viewModelScope.launch {
            try {
                val member = repository.getMember(id)
                    ?: error("This member no longer exists")

                name = member.name
                email = member.email
                phone = member.phone

                savedState["draft_ready"] = true
                mutableState.value = MemberFormState(ready = true)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.value = MemberFormState(
                    error = "Could not load this member. Close the form and try again."
                )
            }
        }
    }

    fun save() {
        val current = mutableState.value ?: return
        if (!current.ready || current.busy || current.completed) return

        // Take a snapshot of the entered text.
        val enteredName = name
        val enteredEmail = email
        val enteredPhone = phone

        mutableState.value = current.copy(
            busy = true,
            error = null,
            confirmDelete = false
        )

        viewModelScope.launch {
            try {
                val id = repository.saveMember(
                    name = enteredName,
                    email = enteredEmail,
                    phone = enteredPhone,
                    memberId = memberId
                )

                mutableState.value = MemberFormState(
                    ready = true,
                    completed = true,
                    resultId = id
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.value = MemberFormState(
                    ready = true,
                    error = "Could not save the member. Check your details and try again."
                )
            }
        }
    }

    fun askToDelete() {
        val current = mutableState.value ?: return

        if (
            memberId <= 0L ||
            !current.ready ||
            current.busy ||
            current.completed
        ) {
            return
        }

        if (!current.confirmDelete) {
            mutableState.value = current.copy(
                confirmDelete = true,
                error = null
            )
            return
        }

        mutableState.value = current.copy(
            busy = true,
            confirmDelete = false,
            error = null
        )

        viewModelScope.launch {
            try {
                // Already removed also means it is no longer in the list.
                repository.deleteMember(memberId)

                mutableState.value = MemberFormState(
                    ready = true,
                    completed = true,
                    resultId = memberId,
                    deleted = true
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.value = MemberFormState(
                    ready = true,
                    error = "Could not delete the member. Please try again."
                )
            }
        }
    }
}
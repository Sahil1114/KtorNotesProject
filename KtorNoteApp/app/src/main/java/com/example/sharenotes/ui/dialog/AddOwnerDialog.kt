package com.example.sharenotes.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.sharenotes.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_note_detail.*

class AddOwnerDialog:DialogFragment() {

    private var positiveListener : ((String) -> Unit)? =null

    fun setPositiveListener(listener: (String) -> Unit) {
        positiveListener=listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val addOwnerEditText=LayoutInflater.from(requireContext()).inflate(
            R.layout.edit_text_email,
            clNoteContainer,false
        ) as TextInputLayout

        return MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_add_person)
            .setTitle("Add owner to note")
            .setMessage("Enter an E-mail of person you want to share" +
                    "This person will able to read and edit note"
            )
            .setView(addOwnerEditText)
            .setPositiveButton("Yes"){_,_->
                val email=addOwnerEditText.findViewById<EditText>(R.id.etAddOwnerEmail).text.toString()
                positiveListener?.let { yes->
                    yes(email)
                }
            }
            .setNegativeButton("Cancel"){dialogInterface,_->
                dialogInterface.cancel()
            }
            .create()
    }
}
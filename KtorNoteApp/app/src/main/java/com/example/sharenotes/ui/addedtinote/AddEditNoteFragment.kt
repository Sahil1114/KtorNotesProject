package com.example.sharenotes.ui.addedtinote

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.sharenotes.R
import com.example.sharenotes.data.entities.Note
import com.example.sharenotes.databinding.FragmentAddEditNoteBinding
import com.example.sharenotes.ui.BaseFragment
import com.example.sharenotes.ui.dialog.ColorPickerDialogFragment
import com.example.sharenotes.utils.Constants.DEFAULT_NOTE_COLOR
import com.example.sharenotes.utils.Constants.FRAGMENT_TAG
import com.example.sharenotes.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.example.sharenotes.utils.Constants.NO_EMAIL
import com.example.sharenotes.utils.Status
import com.skydoves.colorpickerview.ColorPickerDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
@DelicateCoroutinesApi
class AddEditNoteFragment:BaseFragment(R.layout.fragment_add_edit_note) {

    val viewModel:AddEditViewModel by viewModels()

    private var currNote: Note?=null

    private var currNoteColor=DEFAULT_NOTE_COLOR

    lateinit var binding: FragmentAddEditNoteBinding

    private val args:AddEditNoteFragmentArgs by navArgs()

    @Inject
    lateinit var sharedPref:SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding= FragmentAddEditNoteBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.id.isNotEmpty()) {
            viewModel.getNoteById(args.id)
            subscribeToOwners()
        }

        if(savedInstanceState!=null){
            val colorPickerDialog=parentFragmentManager.findFragmentByTag(FRAGMENT_TAG)
                    as ColorPickerDialogFragment
            colorPickerDialog.setPositiveListener {

            }
        }

        binding.viewNoteColor.setOnClickListener {
            ColorPickerDialogFragment().apply {
                setPositiveListener {
                    changeViewNoteColor(it)
                }
            }.show(parentFragmentManager, FRAGMENT_TAG)
        }

    }

    private fun changeViewNoteColor(colorString: String){
        val drawable= ResourcesCompat.getDrawable(
             resources,
            R.drawable.circle_shape,
            null
        )
         drawable?.let {
            val wrappedDrawable= DrawableCompat.wrap(it)
            val color= Color.parseColor("#${colorString}")
            DrawableCompat.setTint(wrappedDrawable,color)
            binding.apply {
                viewNoteColor.background=wrappedDrawable
                currNoteColor=colorString
            }

        }
    }

    private fun subscribeToOwners() {
        viewModel.note.observe(viewLifecycleOwner){
            it?.getContentIfNotHandled()?.let {result->
                when(result.status){
                    Status.SUCCESS->{
                        val note=result.data!!
                        currNote=note
                        binding.apply {
                            etNoteTitle.setText(note.title)
                            etNoteContent.setText(note.content)
                            changeViewNoteColor(note.color)

                        }
                    }
                    Status.ERROR->{
                        showSnackbar(result.message?:"Notes not found")
                    }
                    Status.LOADING->{
                        /*NO-OP*/
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        saveNote()
    }

    private fun saveNote(){
        val authEmail=sharedPref.getString(KEY_LOGGED_IN_EMAIL,NO_EMAIL)?:NO_EMAIL

        val title=binding.etNoteTitle.text.toString()
        val content=binding.etNoteContent.text.toString()
        if(title.isEmpty()||content.isEmpty()){
            return
        }
        val date=System.currentTimeMillis()
        val color=currNoteColor
        val id=currNote?.id?: UUID.randomUUID().toString()
        val owners=currNote?.owners?: listOf(authEmail)

        val note=Note(
            title,
            content,
            date,
            owners,
            color,
            id=id
        )

        viewModel.insertNote(note)


    }
}
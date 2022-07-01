package com.example.sharenotes.ui.notedetail

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.sharenotes.R
import com.example.sharenotes.data.entities.Note
import com.example.sharenotes.databinding.FragmentNoteDetailBinding
import com.example.sharenotes.ui.BaseFragment
import com.example.sharenotes.ui.dialog.AddOwnerDialog
import com.example.sharenotes.utils.Constants.ADD_OWNER_DIALOG_TAG
import com.example.sharenotes.utils.Resource
import com.example.sharenotes.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon

@AndroidEntryPoint
class NoteDetailFragment:BaseFragment(R.layout.fragment_note_detail) {

    private val args:NoteDetailFragmentArgs by navArgs()

    private val noteDetailViewModel: NoteDetailViewModel by viewModels()

    private lateinit var binding: FragmentNoteDetailBinding

    private var currNote: Note?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        setHasOptionsMenu(true)
        binding= FragmentNoteDetailBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToOwner()

        binding.apply {
            fabEditNote.setOnClickListener {
                findNavController().navigate(
                    NoteDetailFragmentDirections.actionNoteDetailFragmentToAddEditNoteFragment(args.id)
                )
            }
        }

        if(savedInstanceState!=null){
            val addOwnerDialog=parentFragmentManager.findFragmentByTag(ADD_OWNER_DIALOG_TAG)
                    as AddOwnerDialog?
            addOwnerDialog?.setPositiveListener {
                addOwnerToCurrentNote(it)
            }
        }
    }

    private fun showAddOwnerDialog(){
        AddOwnerDialog().apply {
            setPositiveListener {
                addOwnerToCurrentNote(it)
            }
        }.show(parentFragmentManager,ADD_OWNER_DIALOG_TAG)
    }

    private fun addOwnerToCurrentNote(email: String) {
        currNote?.let {note->
            noteDetailViewModel.addOwnerToNote(email,note.id)
        }
    }

    private fun setMarkDown(text:String){
        val markwon=Markwon.create(requireContext())
        val markdown=markwon.toMarkdown(text)
        markwon.setParsedMarkdown(binding.tvNoteContent,markdown)
    }

    private fun subscribeToOwner(){

        noteDetailViewModel.addOwnerStatus.observe(viewLifecycleOwner){event->
            event?.getContentIfNotHandled()?.let {result->
                binding.apply {
                    when (result.status) {
                        Status.SUCCESS->{
                            addOwnerProgressBar.visibility=View.GONE
                            showSnackbar(result.data?:"Successfully add owner to note")
                        }
                        Status.LOADING->{
                            addOwnerProgressBar.visibility=View.VISIBLE
                        }
                        Status.ERROR->{
                            addOwnerProgressBar.visibility=View.GONE
                            showSnackbar(result.message?:"An unknown error occurred")
                        }
                    }
                }
            }
        }

        noteDetailViewModel.observeNoteBy(args.id).observe(viewLifecycleOwner){
            it?.let {note ->
                binding.apply {
                    tvNoteTitle.text=note.title
                    setMarkDown(note.content)
                    currNote=note
                }
            }?:showSnackbar("Note not found")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.note_detail_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miAddOwner->showAddOwnerDialog()
        }
        return super.onOptionsItemSelected(item)
    }
}
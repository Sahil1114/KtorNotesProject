package com.example.sharenotes.ui.notes

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Canvas
import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharenotes.R
import com.example.sharenotes.adapter.NoteAdapter
import com.example.sharenotes.databinding.FragmentNotesBinding
import com.example.sharenotes.ui.BaseFragment
import com.example.sharenotes.utils.Constants
import com.example.sharenotes.utils.Status
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_notes.*
import javax.inject.Inject

@AndroidEntryPoint
class NotesFragment:BaseFragment(R.layout.fragment_notes) {

    private lateinit var binding: FragmentNotesBinding

    private lateinit var notesAdapter:NoteAdapter

    @Inject
    lateinit var sharedPref: SharedPreferences

    private val viewModel:NotesViewModel by viewModels()

    private val swipingItem=MutableLiveData(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding= FragmentNotesBinding.inflate(layoutInflater,container,false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
        setUpRecyclerView()
        subscribeToObservers()
        setUpSwipeRefreshLayout()

        notesAdapter.setOnItemClickListener {
            findNavController().navigate(
                NotesFragmentDirections.actionNotesFragmentToNoteDetailFragment(it.id)
            )
        }

        binding.apply {
            fabAddNote.setOnClickListener {
                findNavController().navigate(NotesFragmentDirections.actionNotesFragmentToAddEditNoteFragment(""))
            }
        }

    }

    private fun subscribeToObservers() {
        viewModel.allNotes.observe(viewLifecycleOwner){
            it?.let {event->
                val result=event.peekContent()
                when(result.status){
                    Status.SUCCESS->{
                        notesAdapter.notes=result.data!!
                        binding.swipeRefreshLayout.isRefreshing=false
                    }
                    Status.LOADING->{
                        result.data?.let { notes->
                            notesAdapter.notes=notes
                        }
                        binding.swipeRefreshLayout.isRefreshing=true
                    }
                    Status.ERROR->{
                        event.getContentIfNotHandled()?.let {errorResources->
                            errorResources.message?.let {message->
                                showSnackbar(message)
                            }
                        }
                        result.data?.let { notes->
                            notesAdapter.notes=notes
                        }
                        binding.swipeRefreshLayout.isRefreshing=false
                    }
                }
            }
        }
        swipingItem.observe(viewLifecycleOwner){
            swipeRefreshLayout.isEnabled=!it
        }
    }

    private fun setUpRecyclerView()=binding.rvNotes.apply {
        notesAdapter= NoteAdapter()
        adapter=notesAdapter
        layoutManager=LinearLayoutManager(requireContext())
        ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(this)
    }

    private val itemTouchHelperCallBack=object :ItemTouchHelper.SimpleCallback(
        0,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ){
        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            if(actionState==ItemTouchHelper.ACTION_STATE_SWIPE){
                swipingItem.postValue(isCurrentlyActive)
            }
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean=true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val pos=viewHolder.layoutPosition
            val note=notesAdapter.notes[pos]
            viewModel.deleteNote(note.id)
            Snackbar.make(requireView(),"Note was successfully deleted",Snackbar.LENGTH_LONG).apply {
                setAction("Undo"){
                    viewModel.insertNote(note)
                    viewModel.deleteLocallyDeletedNoteID(note.id)
                }
                show()
            }
        }
    }

    private fun setUpSwipeRefreshLayout(){
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.syncAllNotes()
        }
    }

    private fun logout(){
        sharedPref.apply {
            edit().putString(Constants.KEY_LOGGED_IN_EMAIL, Constants.NO_EMAIL).apply()
            edit().putString(Constants.KEY_PASSWORD, Constants.NO_PASSWORD).apply()
        }
        val navOptions= NavOptions.Builder()
            .setPopUpTo(R.id.notesFragment,true)
            .build()

        findNavController().navigate(
            NotesFragmentDirections.actionNotesFragmentToAuthFragment(),
            navOptions
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miLogout->logout()
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_notes,menu)
    }
}
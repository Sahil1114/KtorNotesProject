package com.example.sharenotes.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.sharenotes.R
import com.example.sharenotes.data.entities.Note
import com.example.sharenotes.databinding.ItemNoteBinding
import java.text.SimpleDateFormat
import java.util.*

class NoteAdapter :RecyclerView.Adapter<NoteAdapter.NoteViewHolder>(){

    inner class NoteViewHolder(private val binding: ItemNoteBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(note:Note){
            binding.apply {
                this.tvTitle.text=note.title
                if(!note.isSynced){
                    ivSynced.setImageResource(R.drawable.ic_cross)
                    tvSynced.text="Not Synced"
                }else{
                    ivSynced.setImageResource(R.drawable.ic_check)
                    tvSynced.text="Synced"
                }

                val dateFormat=SimpleDateFormat("dd|MM|yy, HH:mm ", Locale.getDefault())
                val dateString=dateFormat.format(note.date)
                tvDate.text=dateString

                val drawable=ResourcesCompat.getDrawable(
                    itemView.resources,
                    R.drawable.circle_shape,
                    null
                )

                drawable?.let {
                    val wrappedDrawable=DrawableCompat.wrap(it)
                    val color=Color.parseColor("#${note.color}")
                    DrawableCompat.setTint(wrappedDrawable,color)
                    viewNoteColor.background=wrappedDrawable
                }

            }
        }

    }

    private val diffCallBack= object : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean
        =oldItem.id==newItem.id

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean
        =oldItem.hashCode()==newItem.hashCode()
    }

    private val differ=AsyncListDiffer(this,diffCallBack)

    private var onItemClickListener:((Note)->Unit)?=null

    var notes:List<Note>
    get() = differ.currentList
    set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder=
        NoteViewHolder(
            ItemNoteBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )


    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note=notes[position]
        holder.bind(note)
        holder.itemView.setOnClickListener {
            onItemClickListener?.let {
                it(note)
            }
        }
    }

    override fun getItemCount(): Int =notes.size

    fun setOnItemClickListener(onItemClick: (Note) -> Unit) {
        this.onItemClickListener=onItemClick
    }
}
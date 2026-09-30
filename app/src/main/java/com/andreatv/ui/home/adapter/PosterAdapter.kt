package com.andreatv.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andreatv.R
import com.andreatv.ui.home.model.Poster

class PosterAdapter(
    private val items: List<Poster>
) : RecyclerView.Adapter<PosterAdapter.PosterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PosterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poster, parent, false)
        return PosterViewHolder(view)
    }

    override fun onBindViewHolder(holder: PosterViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class PosterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val artwork: View = itemView.findViewById(R.id.poster_artwork)
        private val monogram: TextView = itemView.findViewById(R.id.poster_monogram)
        private val title: TextView = itemView.findViewById(R.id.poster_title)
        private val metadata: TextView = itemView.findViewById(R.id.poster_metadata)

        init {
            itemView.setOnFocusChangeListener { view, hasFocus ->
                val scale = if (hasFocus) 1.05f else 1f
                view.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .setDuration(120L)
                    .start()
            }
        }

        fun bind(item: Poster) {
            artwork.setBackgroundResource(item.artwork)
            monogram.text = item.title.take(1).uppercase()
            title.text = item.title
            metadata.text = item.metadata
            itemView.contentDescription = "${item.title}, ${item.metadata}"
        }
    }
}

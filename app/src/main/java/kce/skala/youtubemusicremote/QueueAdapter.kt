package kce.skala.youtubemusicremote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load

class QueueAdapter : ListAdapter<SongInfo, QueueAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tv_item_title)
        val artist: TextView = view.findViewById(R.id.tv_item_artist)
        val img: ImageView = view.findViewById(R.id.iv_item_art)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.title.text = item.title
        holder.artist.text = item.artist
        holder.img.load(item.imageSrc)
    }

    object DiffCallback : DiffUtil.ItemCallback<SongInfo>() {
        override fun areItemsTheSame(old: SongInfo, new: SongInfo) = old.title == new.title
        override fun areContentsTheSame(old: SongInfo, new: SongInfo) = old == new
    }
}
package net.capellari.julien.utils

import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView

// Classes
abstract class RecyclerHolder<T: Any>(val view: View) : RecyclerView.ViewHolder(view) {
    // Attributs
    var value: T? = null
        private set

    // Méthodes abstraites
    abstract fun onBind(value: T?)

    // Méthodes
    fun bind(value: T?) {
        this.value = value

        onBind(value)
    }
}

abstract class RecyclerAdapter<T: Any, RH: RecyclerHolder<T>> : RecyclerView.Adapter<RH>() {
    // Attributs
    abstract var items: Array<T>
    val observer = Observer<Array<T>>(::items::set)

    // Méthodes
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: RH, position: Int) {
        holder.bind(items[position])
    }
}
package net.capellari.julien.utils

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Delegate autoNotify:
 * To be used in RecyclerView.Adapter subclass, on the attribute containing the list of showed items.
 * It automatically dispatch DiffUtil's computed changes to the Adapter, when the list is changed.
 * Too do so, it only needs the items to implement the DiffItem interface
 */

// Delegate
inline fun <reified T: DiffItem<T>> autoNotify(initial: Array<T> = arrayOf()) = AutoNotifyArrayProp(initial)
inline fun <reified T: DiffItem<T>, L: List<T>> autoNotify(initial: L) = AutoNotifyListProp(initial)

// Interface
interface DiffItem<T: DiffItem<T>> {
    // Methods
    fun isSameItem(other: T): Boolean
    fun hasSameContent(other: T): Boolean
}

// Classes
class DiffItemCallback<T: DiffItem<T>>: DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(old: T, new: T): Boolean {
        return old.isSameItem(new)
    }

    override fun areContentsTheSame(old: T, new: T): Boolean {
        return old.hasSameContent(new)
    }
}

class ArrayDiffCallback<T: DiffItem<T>>(val olds: Array<T>, val news: Array<T>) : DiffUtil.Callback() {
    // Methods
    override fun getOldListSize() = olds.size
    override fun getNewListSize() = news.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int)
        = olds[oldItemPosition].isSameItem(news[newItemPosition])

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int)
        = olds[oldItemPosition].hasSameContent(news[newItemPosition])
}
class ListDiffCallback<T: DiffItem<T>>(val olds: List<T>, val news: List<T>) : DiffUtil.Callback() {
    // Methods
    override fun getOldListSize() = olds.size
    override fun getNewListSize() = news.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int)
        = olds[oldItemPosition].isSameItem(news[newItemPosition])

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int)
        = olds[oldItemPosition].hasSameContent(news[newItemPosition])
}

class AutoNotifyArrayProp<T: DiffItem<T>>(initial: Array<T>) : ReadWriteProperty<RecyclerView.Adapter<*>, Array<T>> {
    // Attributes
    private var items: Array<T> = initial

    // Methods
    override operator fun getValue(thisRef: RecyclerView.Adapter<*>, property: KProperty<*>): Array<T> {
        return items
    }

    override operator fun setValue(thisRef: RecyclerView.Adapter<*>, property: KProperty<*>, value: Array<T>) {
        val res = DiffUtil.calculateDiff(ArrayDiffCallback(items, value))
        items = value

        res.dispatchUpdatesTo(thisRef)
    }
}
class AutoNotifyListProp<T: DiffItem<T>, L: List<T>>(initial: L) : ReadWriteProperty<RecyclerView.Adapter<*>, L> {
    // Attributes
    private var items: L = initial

    // Methods
    override operator fun getValue(thisRef: RecyclerView.Adapter<*>, property: KProperty<*>): L {
        return items
    }

    override operator fun setValue(thisRef: RecyclerView.Adapter<*>, property: KProperty<*>, value: L) {
        val res = DiffUtil.calculateDiff(ListDiffCallback(items, value))
        items = value

        res.dispatchUpdatesTo(thisRef)
    }
}
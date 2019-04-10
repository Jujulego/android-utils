package net.capellari.julien.utils

import android.os.Parcel
import android.os.Parcelable

// Functions
inline fun<reified T : Parcelable> parcelableCreator(crossinline create: (Parcel) -> T): Parcelable.Creator<T> {
    return object : Parcelable.Creator<T> {
        override fun createFromParcel(source: Parcel) = create(source)
        override fun newArray(size: Int) = arrayOfNulls<T>(size)
    }
}

// Interface
interface KParcelable : Parcelable {
    override fun describeContents(): Int = 0
}
package net.capellari.julien.preference

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import kotlinx.android.synthetic.main.preference_seekbar.view.*
import net.capellari.julien.R
import net.capellari.julien.utils.parcelableCreator
import java.util.*
import kotlin.math.abs

class SeekBarPreference(context: Context, attrs: AttributeSet? = null,
                        @AttrRes styleAttr: Int = R.attr.seekBarPreferenceStyle,
                        @StyleRes styleRes: Int = 0)
        : Preference(context, attrs, styleAttr, styleRes) {

    // Companion
    companion object {
        const val TAG = "SeekBarPreference"

        // Classe
        private class SavedState : BaseSavedState {
            // Companion
            companion object {
                @JvmField
                val CREATOR = parcelableCreator { SavedState(it) }
            }

            // Attributs
            var value: Int = 0
            var min:   Int = 0
            var max:   Int = 0

            // Constructeurs
            constructor(source: Parcel): super(source) {
                value = source.readInt()
                min   = source.readInt()
                max   = source.readInt()
            }

            constructor(superState: Parcelable): super(superState)

            // Méthodes
            override fun writeToParcel(dest: Parcel, flags: Int) {
                super.writeToParcel(dest, flags)

                // Save values
                dest.writeInt(value)
                dest.writeInt(min)
                dest.writeInt(max)
            }
        }
    }

    // Attributs
    var trackingTouch = false

    private lateinit var seekBar: SeekBar
    private var valueTxt: TextView? = null

    // Propriétés
    private var _value = 0
    var value: Int get() = _value
        set(value) {
            setValueInternal(value, true)
        }

    private var _min: Int = 0
    var min: Int get() = _min
        set(min) {
            val m = if (min > max) max else min

            if (m != _min) {
                _min = m
                notifyChanged()
            }
        }

    private var _max: Int = 0
    var max: Int get() = _max
        set(max) {
            val m = if (max < min) min else max

            if (m != _max) {
                _max = m
                notifyChanged()
            }
        }

    private var _increment: Int = 0
    var increment: Int get() = _increment
        set(inc) {
            if (inc != _increment) {
                _increment = minOf(max - min, abs(inc))
                notifyChanged()
            }
        }

    var adjustable: Boolean
    var showValue: Boolean
    var format: String

    // Initialisation
    init {
        // Parse array
        context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference, styleAttr, styleRes)
            .apply {
                _min       = getInt(R.styleable.SeekBarPreference_min, 0)
                max        = getInt(R.styleable.SeekBarPreference_max, 100)
                increment  = getInt(R.styleable.SeekBarPreference_increment, 0)
                adjustable = getBoolean(R.styleable.SeekBarPreference_adjustable, true)
                showValue  = getBoolean(R.styleable.SeekBarPreference_showValue, true)
                format     = getString(R.styleable.SeekBarPreference_format) ?: "%d"
            }.recycle()
    }

    // Events
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        // React to DPAD left/right
        holder.itemView.setOnKeyListener { v, keyCode, event ->
            if (event.action != KeyEvent.ACTION_DOWN) {
                return@setOnKeyListener false
            }

            if (!adjustable && (keyCode in arrayOf(KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT))) {
                return@setOnKeyListener false
            }

            if (keyCode in arrayOf(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER)) {
                return@setOnKeyListener false
            }

            if (!this::seekBar.isInitialized) {
                Log.e(TAG, "SeekBar view is not initialized and hence cannot be adjusted.")
                return@setOnKeyListener false
            }

            return@setOnKeyListener seekBar.onKeyDown(keyCode, event)
        }

        // Text view
        valueTxt = holder.itemView.seekbar_value
        if (showValue) {
            valueTxt!!.visibility = View.VISIBLE
            valueTxt!!.text = format.format(Locale.getDefault(), value)
        } else {
            valueTxt!!.visibility = View.GONE
            valueTxt = null
        }

        // Seekbar
        seekBar = holder.itemView.seekbar
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser && ! trackingTouch) {
                    syncValueInternal(seekBar)
                }

                syncTextView(progress + min)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                trackingTouch = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                trackingTouch = false

                if (seekBar.progress + min != value) {
                    syncValueInternal(seekBar)
                }
            }
        })

        seekBar.max = max - min

        if (increment != 0) {
            seekBar.keyProgressIncrement = increment
        } else {
            _increment = seekBar.keyProgressIncrement
        }

        seekBar.progress = value - min
        seekBar.isEnabled = isEnabled
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int) = a.getInt(index, 0)

    override fun onSetInitialValue(defaultValue: Any?) {
        value = getPersistedInt((defaultValue ?: 0) as Int)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        if (isPersistent) return superState

        // save instance state
        val state = SavedState(superState)
        state.value = value
        state.min   = min
        state.max   = max

        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        // no state for us
        if (state !is SavedState) {
            return super.onRestoreInstanceState(state)
        }

        // restore instance state
        super.onRestoreInstanceState(state.superState)
        _value = state.value
        _min   = state.min
        _max   = state.max

        notifyChanged()
    }

    // Méthodes
    /**
     * Store the given value and persist it.
     */
    private fun setValueInternal(value: Int, notify: Boolean) {
        val v = minOf(maxOf(value, min), max)

        if (v != value) {
            _value = v

            syncTextView(v)
            persistInt(v)

            if (notify) {
                notifyChanged()
            }
        }
    }

    /**
     * Persist seekbar's value if callChangeListener returns true
     * otherwise set it to the stored value
     */
    private fun syncValueInternal(seekBar: SeekBar) {
        val v = min + seekBar.progress

        if (v != value) {
            if (callChangeListener(v)) {
                setValueInternal(v, false)
            } else {
                seekBar.progress = _value - min
            }
        }
    }

    /**
     * Update TextView according to the given value
     */
    private fun syncTextView(value: Int) {
        val v = minOf(maxOf(value, min), max)
        valueTxt?.text = format.format(v)
    }
}
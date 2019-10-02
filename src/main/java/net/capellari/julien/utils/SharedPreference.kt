package net.capellari.julien.utils

import android.content.Context
import android.preference.PreferenceManager
import java.lang.RuntimeException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// Delegates
fun <T : Any> sharedPreference(name: String, default: T) = SharedPreference(name) { default }
fun <T : Any> sharedPreference(name: String, default: () -> T) = SharedPreference(name, default)
fun <T : Any> sharedPreference(name: String, context: Context, default: T) = ContextSharedPreference(context, name) { default }
fun <T : Any> sharedPreference(name: String, context: Context, default: () -> T) = ContextSharedPreference(context, name, default)

// Classes
@Suppress("UNCHECKED_CAST")
abstract class BaseSharedPreference<R : Any, T : Any>(val name: String, val default: () -> T): ReadWriteProperty<R,T> {
    // Méthodes
    fun sharedPreferences(context: Context)
            = PreferenceManager.getDefaultSharedPreferences(context)

    fun get(context: Context) : T {
        return sharedPreferences(context).let {
            val d = default()

            when (d) {
                is Boolean -> it.getBoolean(name, d as Boolean) as T
                is Float   -> it.getFloat(  name, d as Float)   as T
                is Int     -> it.getInt(    name, d as Int)     as T
                is Long    -> it.getLong(   name, d as Long)    as T
                is String  -> it.getString( name, d as String)  as T
                is MutableSet<*> -> it.getStringSet(name, d as MutableSet<String>) as T

                else -> throw RuntimeException("Unsupported type : ${d::class.qualifiedName}")
            }
        }
    }

    fun set(context: Context, value: T) {
        sharedPreferences(context).edit().also {
            when (value) {
                is Boolean -> it.putBoolean(name, value as Boolean) as T
                is Float   -> it.putFloat(  name, value as Float)   as T
                is Int     -> it.putInt(    name, value as Int)     as T
                is Long    -> it.putLong(   name, value as Long)    as T
                is String  -> it.putString( name, value as String)  as T
                is MutableSet<*> -> it.putStringSet(name, value as MutableSet<String>) as T

                else -> throw RuntimeException("Unsupported type : ${value::class.qualifiedName}")
            }
        }.apply()
    }
}

class SharedPreference<T : Any>(name: String, default: () -> T) : BaseSharedPreference<Context,T>(name, default) {
    // Methods
    override fun getValue(thisRef: Context, property: KProperty<*>): T        = get(thisRef)
    override fun setValue(thisRef: Context, property: KProperty<*>, value: T) = set(thisRef, value)
}

class ContextSharedPreference<T : Any>(val context: Context, name: String, default: () -> T) : BaseSharedPreference<Any,T>(name, default) {
    // Methods
    override fun getValue(thisRef: Any, property: KProperty<*>): T        = get(context)
    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) = set(context, value)
}
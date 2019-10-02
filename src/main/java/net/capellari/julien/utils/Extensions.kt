package net.capellari.julien.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import com.google.common.io.BaseEncoding
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

// Extensions
fun Date.format(fmt: String, locale: Locale = Locale.getDefault()): String
    = SimpleDateFormat(fmt, locale).format(fmt)

fun View.snackbar(txt: String, duration: Int)
        = Snackbar.make(this, txt, duration)

fun View.snackbar(@StringRes res: Int, duration: Int)
        = Snackbar.make(this, res, duration)

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun ViewGroup.findViewsByPredicate(pred: (v: View) -> Boolean): List<View> {
    val views: MutableList<View> = mutableListOf()

    for (i in 0 until childCount) {
        val view = getChildAt(i)

        if (pred(view)) {
            views.add(view)
        }

        if (view is ViewGroup) {
            views.addAll(view.findViewsByPredicate(pred))
        }
    }

    return views
}

fun ViewGroup.findViewsByTag(tag: Any) = findViewsByPredicate { it.tag == tag }
fun ViewGroup.findViewsByTag(key: Int, tag: Any) = findViewsByPredicate { it.getTag(key) == tag }

fun Context.getSHA1Cert() : String? {
    try {
        val signatures: Array<Signature>

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            signatures = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo.signingCertificateHistory
        } else {
            @Suppress("DEPRECATION")
            signatures = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
        }

        for (signature in signatures) {
            val md = MessageDigest.getInstance("SHA-1")
            md.update(signature.toByteArray())
            return BaseEncoding.base16().encode(md.digest())
        }
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e("Context", "Error while getting SHA1 signature : ", e)
    } catch (e: NoSuchAlgorithmException) {
        Log.e("Context", "No SHA1 signature :", e)
    }

    return null
}

inline val <T : Any> KProperty0<T>.sharedPreference: String?
    get() {
        isAccessible = true
        val delegate = getDelegate()
        isAccessible = false

        return if (delegate is BaseSharedPreference<*,*>) delegate.name else null
    }
package net.capellari.julien.utils

// Fonctions
fun success(cb: () -> Unit) = Success<Unit,Unit> { cb() }
fun <SP: Any?, FP: Any?> success(cb: (SP) -> Unit) = Success<SP,FP>(cb)

// Classes
open class SuccessFail<in SP: Any?, in FP: Any?>(success: (SP) -> Unit, fail: (FP) -> Unit) {
    // Attributs
    internal val cbSuccess = success
    internal val cbFail = fail

    // Méthodes
    fun succeed(arg: SP) {
        cbSuccess(arg)
    }

    fun failed(arg: FP) {
        cbFail(arg)
    }
}

class Success<in SP: Any?, FP: Any?>(success: (SP) -> Unit): SuccessFail<SP,FP>(success, {}) {
    // Opérateurs
    operator fun invoke(arg: SP) = cbSuccess(arg)

    // Méthodes
    infix fun fail(cb: (FP) -> Unit): SuccessFail<SP,FP> {
        return SuccessFail(cbSuccess, cb)
    }
}
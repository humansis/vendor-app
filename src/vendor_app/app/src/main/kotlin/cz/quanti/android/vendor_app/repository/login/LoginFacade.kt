package cz.quanti.android.vendor_app.repository.login

import io.reactivex.Completable

interface LoginFacade {

    fun login(username: String, password: String): Completable

    fun logout()
}

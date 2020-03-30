package cz.quanti.android.vendor_app.repository.login

import cz.quanti.android.vendor_app.repository.login.dto.Salt
import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import io.reactivex.Single

interface LoginRepository {

    fun getSalt(username: String): Single<Pair<Int, Salt>>

    fun login(vendor: Vendor): Single<Pair<Int, Vendor>>
}

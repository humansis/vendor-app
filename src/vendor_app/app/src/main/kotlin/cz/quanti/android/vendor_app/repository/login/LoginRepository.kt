package cz.quanti.android.vendor_app.repository.login

import cz.quanti.android.vendor_app.repository.login.dto.api.VendorWithResponseCode
import io.reactivex.Single

interface LoginRepository {

    fun login(username: String, password: String): Single<VendorWithResponseCode>
}

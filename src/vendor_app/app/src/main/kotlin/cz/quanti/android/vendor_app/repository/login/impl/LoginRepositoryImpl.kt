package cz.quanti.android.vendor_app.repository.login.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.login.LoginRepository
import cz.quanti.android.vendor_app.repository.login.dto.api.VendorApiEntity
import cz.quanti.android.vendor_app.repository.login.dto.api.VendorWithResponseCode
import cz.quanti.android.vendor_app.utils.toVendor
import io.reactivex.Single

class LoginRepositoryImpl(private val api: VendorAPI) :
    LoginRepository {

    override fun login(username: String, password: String): Single<VendorWithResponseCode> {
        return api.postLogin(
            VendorApiEntity(
                username = username,
                password = password
            )
        ).map { response ->
            VendorWithResponseCode(
                vendor = response.body().toVendor(),
                responseCode = response.code()
            )
        }
    }
}

package cz.quanti.android.vendor_app.repository.login.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.login.LoginRepository
import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import cz.quanti.android.vendor_app.repository.login.dto.api.VendorApiEntity
import cz.quanti.android.vendor_app.repository.login.dto.api.VendorWithResponseCode
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
                vendor = convert(response.body()),
                responseCode = response.code()
            )
        }
    }

    private fun convert(vendorApiEntity: VendorApiEntity?): Vendor {
        return if (vendorApiEntity == null) {
            Vendor()
        } else {
            Vendor().apply {
                this.id = vendorApiEntity.id
                this.username = vendorApiEntity.username
                this.password = vendorApiEntity.password
                this.country = vendorApiEntity.countryISO3
                this.token = vendorApiEntity.token
            }
        }
    }
}

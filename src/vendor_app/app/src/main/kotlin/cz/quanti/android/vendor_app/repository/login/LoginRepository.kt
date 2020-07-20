package cz.quanti.android.vendor_app.repository.login

import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import cz.quanti.android.vendor_app.repository.login.dto.api.SaltWithResponseCode
import cz.quanti.android.vendor_app.repository.login.dto.api.VendorWithResponseCode
import io.reactivex.Single

interface LoginRepository {

    fun getSalt(username: String): Single<SaltWithResponseCode>

    fun login(vendor: Vendor): Single<VendorWithResponseCode>

    fun getVendor(id: Long): Single<VendorWithResponseCode>
}

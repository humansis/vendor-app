package cz.quanti.android.vendor_app.repository.login.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.login.LoginRepository
import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import cz.quanti.android.vendor_app.repository.login.dto.api.VendorApiEntity
import cz.quanti.android.vendor_app.repository.login.dto.api.VendorLocationApiEntity
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
                this.id = vendorApiEntity.userId
                this.username = vendorApiEntity.username
                this.password = vendorApiEntity.password
                this.country = getCountryFromLocation(vendorApiEntity.location)
                this.token = vendorApiEntity.token
            }
        }
    }

    private fun getCountryFromLocation(location: VendorLocationApiEntity?): String {
        location?.adm1?.let { locationAdm1 ->
            return locationAdm1.country_i_s_o3
        }

        location?.adm2?.let { locationAdm2 ->
            locationAdm2.adm1?.let { locationAdm1 ->
                return locationAdm1.country_i_s_o3
            }
        }

        location?.adm3?.let { locationAdm3 ->
            locationAdm3.adm2?.let { locationAdm2 ->
                locationAdm2.adm1?.let { locationAdm1 ->
                    return locationAdm1.country_i_s_o3
                }
            }
        }

        location?.adm4?.let { locationAdm4 ->
            locationAdm4.adm3?.let { locationAdm3 ->
                locationAdm3.adm2?.let { locationAdm2 ->
                    locationAdm2.adm1?.let { locationAdm1 ->
                        return locationAdm1.country_i_s_o3
                    }
                }
            }
        }

        return ""
    }
}

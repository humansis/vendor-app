package cz.quanti.android.vendor_app.repository.login.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.login.LoginRepository
import cz.quanti.android.vendor_app.repository.login.dto.Salt
import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import cz.quanti.android.vendor_app.repository.login.dto.api.SaltApiEntity
import cz.quanti.android.vendor_app.repository.login.dto.api.SaltWithResponseCode
import cz.quanti.android.vendor_app.repository.login.dto.api.VendorApiEntity
import cz.quanti.android.vendor_app.repository.login.dto.api.VendorLocationApiEntity
import cz.quanti.android.vendor_app.repository.login.dto.api.VendorWithResponseCode
import cz.quanti.android.vendor_app.repository.utils.exceptions.LoginException
import cz.quanti.android.vendor_app.repository.utils.exceptions.LoginExceptionState
import io.reactivex.Single
import java.io.IOException
import java.net.UnknownHostException

class LoginRepositoryImpl(private val api: VendorAPI) :
    LoginRepository {

    override fun getSalt(username: String): Single<SaltWithResponseCode> {
        return api.getSalt(username).onErrorResumeNext {
            when (it) {
                is IOException -> {
                    Single.error(LoginException(LoginExceptionState.NO_CONNECTION))
                }
                is UnknownHostException -> {
                    Single.error(LoginException(LoginExceptionState.NO_CONNECTION))
                }
                else -> {
                    throw it
                }
            }
        }.map { response ->
            SaltWithResponseCode(salt = convert(response.body()), responseCode = response.code())
        }
    }

    override fun login(vendor: Vendor): Single<VendorWithResponseCode> {
        return api.postLogin(convert(vendor)).map { response ->
            VendorWithResponseCode(
                vendor = convert(response.body()),
                responseCode = response.code()
            )
        }
    }

    private fun convert(saltApiEntity: SaltApiEntity?): Salt {
        return if (saltApiEntity == null) {
            Salt("")
        } else {
            Salt(saltApiEntity.salt)
        }
    }

    private fun convert(vendor: Vendor): VendorApiEntity {
        return VendorApiEntity().apply {
            this.id = vendor.id
            this.username = vendor.username
            this.password = vendor.password
            this.saltedPassword = vendor.saltedPassword
            this.shop = vendor.shop
            this.adress = vendor.address
            this.loggedIn = vendor.loggedIn
            this.products = vendor.products
            this.language = vendor.language
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
                this.saltedPassword = vendorApiEntity.saltedPassword
                this.shop = vendorApiEntity.shop ?: ""
                this.address = vendorApiEntity.adress
                this.loggedIn = vendorApiEntity.loggedIn
                this.products = vendorApiEntity.products
                this.country = getCountryFromLocation(vendorApiEntity.location)
                this.language = vendorApiEntity.language
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

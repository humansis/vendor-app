package cz.quanti.android.vendor_app.repository.login.impl

import cz.quanti.android.vendor_app.repository.VendorAPI
import cz.quanti.android.vendor_app.repository.login.LoginRepository
import cz.quanti.android.vendor_app.repository.login.dto.Salt
import cz.quanti.android.vendor_app.repository.login.dto.Vendor
import cz.quanti.android.vendor_app.repository.login.dto.api.SaltApiEntity
import cz.quanti.android.vendor_app.repository.login.dto.api.VendorApiEntity
import io.reactivex.Single

class LoginRepositoryImpl(private val api: VendorAPI) :
    LoginRepository {

    override fun getSalt(username: String): Single<Pair<Int, Salt>> {
        return api.getSalt(username).map { response ->
            Pair(response.code(), convert(response.body()))
        }
    }

    override fun login(vendor: Vendor): Single<Pair<Int, Vendor>> {
        return api.postLogin(convert(vendor)).map { response ->
            Pair(response.code(), convert(response.body()))
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
            this.country = vendor.country
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
                this.shop = vendorApiEntity.shop
                this.address = vendorApiEntity.adress
                this.loggedIn = vendorApiEntity.loggedIn
                this.products = vendorApiEntity.products
                this.country = vendorApiEntity.country
                this.language = vendorApiEntity.language
            }
        }
    }
}

package cz.quanti.android.vendor_app.repository.facade.impl

import com.squareup.picasso.Picasso
import cz.quanti.android.vendor_app.repository.api.repository.ApiRepository
import cz.quanti.android.vendor_app.repository.db.reposiitory.DbRepository
import cz.quanti.android.vendor_app.repository.entity.Product
import cz.quanti.android.vendor_app.repository.entity.Vendor
import cz.quanti.android.vendor_app.repository.facade.CommonFacade
import cz.quanti.android.vendor_app.utils.misc.LoginManager
import cz.quanti.android.vendor_app.utils.misc.VendorAppException
import cz.quanti.android.vendor_app.utils.misc.hashAndSaltPassword
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Response

class CommonFacadeImpl(private val apiRepo: ApiRepository, private val dbRepo: DbRepository, private val picasso: Picasso): CommonFacade {

    override fun login(username: String, password: String): Single<Response<Vendor>> {
        return apiRepo.getSalt(username).flatMap { saltResponse ->
            Single.fromCallable{
                if(saltResponse.code() != 200) {
                    throw VendorAppException("Could not obtain salt for the user.").apply {
                        apiError = true
                        apiResponseCode = saltResponse.code()
                    }
                }
                saltResponse.body()?.salt?.let {
                    var saltedPassword = hashAndSaltPassword(it, password)
                    LoginManager.user = Vendor().apply {
                        this.saltedPassword = saltedPassword
                        this.username = username
                        this.loggedIn = true
                        this.password = saltedPassword
                    }
                }
            }.flatMap {
                LoginManager.user?.let { it1 -> apiRepo.login(it1) }
            }
        }
    }

    override fun reloadProductFromServer(): Completable {
        return apiRepo.getProducts().flatMapCompletable { response ->
            if (response.code() == 200) {
                actualizeDatabase(response.body())
            } else {
                throw VendorAppException("Could not get products from server.").apply {
                    apiError = true
                    apiResponseCode = response.code()
                }
            }
        }
    }

    override fun getProducts(): Single<List<Product>> {
        return dbRepo.getProducts()
    }

    private fun actualizeDatabase(products: List<Product>?): Completable {
        return if (products == null) {
            throw VendorAppException("Products returned from server were empty.")
        } else {
            dbRepo.deleteProducts().andThen(
                Observable.fromIterable(products).flatMapCompletable { product ->
                    dbRepo.saveProduct(product).andThen(Completable.fromCallable { picasso.load(product.image) })
                })
        }
    }


}

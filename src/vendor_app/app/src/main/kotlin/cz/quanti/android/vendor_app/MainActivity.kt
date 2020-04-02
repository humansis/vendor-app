package cz.quanti.android.vendor_app

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import cz.quanti.android.vendor_app.main.authorization.viewmodel.LoginViewModel
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import io.reactivex.schedulers.Schedulers
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class MainActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel by viewModel()
    private val vendorViewModel: VendorViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar_actions, menu)

        menu?.findItem(R.id.logoutButton)?.setOnMenuItemClickListener {
            AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle(getString(R.string.are_you_sure_dialog_title))
                .setMessage(getString(R.string.logout_dialog_message))
                .setPositiveButton(
                    android.R.string.yes
                ) { _, _ ->
                    loginViewModel.logout()
                    findNavController(R.id.main_nav_host).popBackStack(R.id.loginFragment, false)
                }
                .setNegativeButton(android.R.string.no, null)
                .show()

            true
        }

        menu?.findItem(R.id.syncButton)?.setOnMenuItemClickListener {
            vendorViewModel.synchronizeWithServer().subscribeOn(Schedulers.io()).subscribe(
                {
                },
                {
                    Log.e(it)
                }
            )
            true
        }

        return super.onCreateOptionsMenu(menu)
    }
}

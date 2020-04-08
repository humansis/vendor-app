package cz.quanti.android.vendor_app

import android.os.Bundle
import android.view.Menu
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import cz.quanti.android.vendor_app.main.authorization.viewmodel.LoginViewModel
import cz.quanti.android.vendor_app.main.vendor.viewmodel.VendorViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
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

        val syncButton = menu?.findItem(R.id.syncButton)?.actionView as ImageView
        syncButton.setImageResource(R.drawable.sync)
        syncButton.setOnClickListener { view ->
            val animation = RotateAnimation(0f, 360f, view.width / 2f, view.height / 2f)
            animation.duration = 2500
            animation.repeatCount = Animation.INFINITE
            view.startAnimation(animation)

            vendorViewModel.synchronizeWithServer().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(
                {
                    animation.repeatCount = 0
                },
                { e ->
                    view.animation.repeatCount = 0
                    Log.e(e)
                }
            )
        }


        return super.onCreateOptionsMenu(menu)
    }
}

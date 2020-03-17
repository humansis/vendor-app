package cz.quanti.android.vendor_app

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import cz.quanti.android.vendor_app.main.authorization.viewmodel.LoginViewModel


class MainActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar_actions, menu)

        menu?.findItem(R.id.logoffButton)?.setOnMenuItemClickListener {
            loginViewModel.logoff()
            true
        }

        return super.onCreateOptionsMenu(menu)
    }
}

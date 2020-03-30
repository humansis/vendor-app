package cz.quanti.android.vendor_app.main.authorization.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cz.quanti.android.vendor_app.App
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.authorization.viewmodel.LoginViewModel
import cz.quanti.android.vendor_app.utils.CurrentVendor
import cz.quanti.android.vendor_app.utils.VendorAppException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_login.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class LoginFragment : Fragment() {

    private val vm: LoginViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).supportActionBar?.hide()
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferences = (activity?.application as App).preferences
        CurrentVendor.preferences = preferences

        if (CurrentVendor.isLoggedIn()) {
            findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToVendorFragment("")
            )
        } else {
            logoImageView.clipToOutline = true
            loginButton.isEnabled = true
            loginButton.setOnClickListener {
                loginButton.isEnabled = false

                vm.login(usernameEditText.text.toString(), passwordEditText.text.toString())
                    .subscribeOn(
                        Schedulers.io()
                    ).observeOn(AndroidSchedulers.mainThread()).subscribe(
                        {
                            findNavController().navigate(
                                LoginFragmentDirections.actionLoginFragmentToVendorFragment("")
                            )
                        },
                        {
                            Log.e(it) // TODO the error might be caused by internet connection or other causes
                            usernameEditText.error = getString(R.string.wrong_password)
                            passwordEditText.error = getString(R.string.wrong_password)
                            loginButton.isEnabled = true
                            //TODO just for debugging purposes
                            Toast.makeText(
                                context,
                                it.message + " " + (it as VendorAppException).apiResponseCode,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
            }
        }
    }
}

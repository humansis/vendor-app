package cz.quanti.android.vendor_app.main.authorization.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.authorization.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.fragment_login.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment: Fragment() {

    val vm: LoginViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as MainActivity).supportActionBar?.hide()
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        logoImageView.clipToOutline = true
        loginButton.setOnClickListener{

            if(vm.login(usernameEditText.text.toString(), passwordEditText.text.toString() )) {
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToVendorFragment())
            } else {
                // TODO
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }
}

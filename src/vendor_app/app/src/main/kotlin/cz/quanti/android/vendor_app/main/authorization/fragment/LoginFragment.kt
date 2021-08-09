package cz.quanti.android.vendor_app.main.authorization.fragment

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.BuildConfig
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.databinding.FragmentLoginBinding
import cz.quanti.android.vendor_app.main.authorization.viewmodel.LoginViewModel
import cz.quanti.android.vendor_app.utils.ApiEnvironments
import cz.quanti.android.vendor_app.utils.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class LoginFragment : Fragment() {

    private val vm: LoginViewModel by viewModel()
    private var disposable: Disposable? = null

    private var activityCallback: ActivityCallback? = null

    private lateinit var loginBinding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityCallback = activity as ActivityCallback
        activityCallback?.setToolbarVisible(false)
        loginBinding = FragmentLoginBinding.inflate(inflater, container, false)
        return loginBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginBinding.versionTextView.text = getString(R.string.version, BuildConfig.VERSION_NAME)

        if (BuildConfig.DEBUG) {
            loginBinding.settingsImageView.visibility = View.VISIBLE
            loginBinding.envTextView.visibility = View.VISIBLE
            var defaultEnv = ApiEnvironments.DEV
            val savedEnv = vm.getSavedApiHost()
            savedEnv?.let {
                defaultEnv = savedEnv
            }
            loginBinding.envTextView.text = defaultEnv.name
            vm.setApiHost(defaultEnv)

            loginBinding.settingsImageView.setOnClickListener {
                val contextThemeWrapper =
                    ContextThemeWrapper(requireContext(), R.style.PopupMenuTheme)
                val popup = PopupMenu(contextThemeWrapper, loginBinding.settingsImageView)
                popup.inflate(R.menu.api_urls_menu)
                popup.menu.add(0, ApiEnvironments.FRONT.id, 0, "FRONT API")
                popup.menu.add(0, ApiEnvironments.DEMO.id, 0, "DEMO API")
                popup.menu.add(0, ApiEnvironments.STAGE.id, 0, "STAGE API")
                popup.menu.add(0, ApiEnvironments.DEV.id, 0, "DEV API")
                popup.menu.add(0, ApiEnvironments.TEST.id, 0, "TEST API")
                popup.setOnMenuItemClickListener { item ->
                    val env = ApiEnvironments.values().find { it.id == item?.itemId }
                    env?.let {
                        vm.setApiHost(it)
                        loginBinding.envTextView.text = it.name
                        vm.saveApiHost(it)
                    }
                    true
                }
                popup.show()
            }
        } else {
            loginBinding.settingsImageView.visibility = View.INVISIBLE
            loginBinding.envTextView.visibility = View.INVISIBLE
        }

        if (vm.isVendorLoggedIn()) {
            if(vm.getCurrentVendorName().equals(BuildConfig.DEMO_ACCOUNT, true)) {
                vm.setApiHost(ApiEnvironments.STAGE)
                vm.saveApiHost(ApiEnvironments.STAGE)
            }
            findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToVendorFragment()
            )
        } else {
            loginBinding.logoImageView.clipToOutline = true
            loginBinding.loginButton.isEnabled = true
            loginBinding.loginButton.setOnClickListener {
                if (loginBinding.usernameEditText.text.toString().isNotEmpty() && loginBinding.passwordEditText.text.toString().isNotEmpty()) {

                    if (loginBinding.usernameEditText.text.toString().equals(BuildConfig.DEMO_ACCOUNT, true)) {
                        vm.setApiHost(ApiEnvironments.STAGE)
                        vm.saveApiHost(ApiEnvironments.STAGE)
                    }

                    loginBinding.loginButton.isEnabled = false
                    loginBinding.loginButton.visibility = View.INVISIBLE
                    loginBinding.loadingImageView.visibility = View.VISIBLE

                    val animation = RotateAnimation(
                        0f,
                        360f,
                        loginBinding.loadingImageView.width / 2f,
                        loginBinding.loadingImageView.height / 2f
                    )
                    animation.duration = Constants.SYNCING_BUTTON_ANIMATION_DURATION_IN_MS
                    animation.repeatCount = Animation.INFINITE
                    loginBinding.loadingImageView.startAnimation(animation)

                    disposable?.dispose()
                    disposable =
                        vm.login(loginBinding.usernameEditText.text.toString(), loginBinding.passwordEditText.text.toString())
                            .subscribeOn(
                                Schedulers.io()
                            ).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                {
                                    vm.onLogin(requireActivity() as (ActivityCallback))
                                    loginBinding.loadingImageView.animation.repeatCount = 0
                                    loginBinding.usernameEditText.error = null
                                    loginBinding.passwordEditText.error = null
                                    findNavController().navigate(
                                        LoginFragmentDirections.actionLoginFragmentToVendorFragment()
                                    )
                                },
                                {
                                    loginBinding.loadingImageView.clearAnimation()
                                    loginBinding.loadingImageView.visibility = View.INVISIBLE
                                    loginBinding.loginButton.visibility = View.VISIBLE
                                    loginBinding.loginButton.isEnabled = true
                                    Log.e(TAG, it)
                                    if (vm.isNetworkConnected().value == true) {
                                        loginBinding.usernameEditText.error = getString(R.string.wrong_password)
                                        loginBinding.passwordEditText.error = getString(R.string.wrong_password)
                                    } else {
                                        loginBinding.usernameEditText.error = null
                                        loginBinding.passwordEditText.error = null
                                        Toast.makeText(
                                            context,
                                            getString(R.string.no_internet_connection),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            )
                }
            }
        }
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }

    companion object {
        private val TAG = LoginFragment::class.java.simpleName
    }
}

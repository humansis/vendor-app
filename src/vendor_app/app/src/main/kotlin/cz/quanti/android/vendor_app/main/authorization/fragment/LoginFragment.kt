package cz.quanti.android.vendor_app.main.authorization.fragment

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cz.quanti.android.vendor_app.ActivityCallback
import cz.quanti.android.vendor_app.BuildConfig
import cz.quanti.android.vendor_app.MainViewModel
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.databinding.FragmentLoginBinding
import cz.quanti.android.vendor_app.main.authorization.viewmodel.LoginViewModel
import cz.quanti.android.vendor_app.repository.utils.exceptions.LoginException
import cz.quanti.android.vendor_app.repository.utils.exceptions.LoginExceptionState
import cz.quanti.android.vendor_app.utils.ApiEnvironment
import cz.quanti.android.vendor_app.utils.Constants
import cz.quanti.android.vendor_app.utils.SendLogDialogFragment
import cz.quanti.android.vendor_app.utils.hideKeyboard
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import quanti.com.kotlinlog.Log

class LoginFragment : Fragment() {

    private val mainVM: MainViewModel by sharedViewModel()
    private val vm: LoginViewModel by sharedViewModel()
    private var disposable: Disposable? = null

    private lateinit var activityCallback: ActivityCallback

    private lateinit var loginBinding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityCallback = requireActivity() as ActivityCallback
        activityCallback.setToolbarVisible(false)
        loginBinding = FragmentLoginBinding.inflate(inflater, container, false)
        return loginBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.navigationBarColor = android.R.attr.navigationBarColor

        loginBinding.versionTextView.text = getString(R.string.version, BuildConfig.VERSION_NAME)

        loginBinding.settingsImageView.setOnClickListener {
            Log.d(TAG, "Environment menu opened.")
            val contextThemeWrapper =
                ContextThemeWrapper(requireContext(), R.style.PopupMenuTheme)
            val popup = PopupMenu(contextThemeWrapper, loginBinding.settingsImageView)
            popup.inflate(R.menu.api_urls_menu)
            val environments = ApiEnvironment.createEnvironments(requireContext())
            environments.forEach {
                popup.menu.add(0, it.id, 0, it.title)
            }

            popup.setOnMenuItemClickListener { item ->
                environments.find { it.id == item?.itemId }?.let { env ->
                    setEnvironment(env)
                }
                true
            }
            popup.show()
        }

        val defaultEnv = if (BuildConfig.DEBUG) {
            loginBinding.settingsImageView.visibility = View.VISIBLE
            loginBinding.envTextView.visibility = View.VISIBLE

            vm.getApiHost() ?: ApiEnvironment.Stage
        } else {
            loginBinding.settingsImageView.visibility = View.INVISIBLE
            loginBinding.envTextView.visibility = View.INVISIBLE
            loginBinding.versionTextView.setOnLongClickListener {
                loginBinding.settingsImageView.visibility = View.VISIBLE
                loginBinding.envTextView.visibility = View.VISIBLE
                return@setOnLongClickListener true
            }

            ApiEnvironment.Front
        }

        setEnvironment(defaultEnv)

        loginBinding.logoImageView.setOnLongClickListener {
            SendLogDialogFragment.newInstance(
                sendEmailAddress = getString(R.string.send_email_address),
                title = getString(R.string.logs_dialog_title),
                message = getString(R.string.logs_dialog_message),
                emailButtonText = getString(R.string.logs_dialog_email_button),
                dialogTheme = R.style.DialogTheme
            ).show(requireActivity().supportFragmentManager, "TAG")
            // TODO inside this method in kotlinlogger there is a method getZipOfFiles() that automatically deletes all logs older than 4 days
            return@setOnLongClickListener true
        }

        loginBinding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginBinding.loginButton.performClick()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        if (vm.isVendorLoggedIn()) {
            if (vm.getCurrentVendorName().equals(BuildConfig.DEMO_ACCOUNT, true)) {
                vm.setApiHost(ApiEnvironment.Stage)
            }
            findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToProductsFragment()
            )
        } else {
            loginBinding.logoImageView.clipToOutline = true
            loginBinding.loginButton.isEnabled = true
            loginBinding.loginButton.setOnClickListener {
                Log.d(TAG, "Login button clicked.")
                hideKeyboard()
                if (loginBinding.usernameEditText.text.toString()
                        .isNotEmpty() && loginBinding.passwordEditText.text.toString().isNotEmpty()
                ) {
                    if (loginBinding.usernameEditText.text.toString()
                            .equals(BuildConfig.DEMO_ACCOUNT, true)
                    ) {
                        vm.setApiHost(ApiEnvironment.Stage)
                    }

                    loginBinding.loginButton.isEnabled = false
                    loginBinding.loginButton.visibility = View.INVISIBLE
                    loginBinding.loadingImageView.visibility = View.VISIBLE
                    loginBinding.usernameEditText.error = null
                    loginBinding.passwordEditText.error = null
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
                        vm.login(
                            loginBinding.usernameEditText.text.toString(),
                            loginBinding.passwordEditText.text.toString()
                        )
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                loginBinding.loadingImageView.animation.repeatCount = 0
                                loginBinding.usernameEditText.error = null
                                loginBinding.passwordEditText.error = null
                                findNavController().navigate(
                                    LoginFragmentDirections.actionLoginFragmentToProductsFragment()
                                )
                                vm.onLogin(requireActivity() as ActivityCallback)
                            }, {
                                loginBinding.loadingImageView.clearAnimation()
                                loginBinding.loadingImageView.visibility = View.INVISIBLE
                                loginBinding.loginButton.visibility = View.VISIBLE
                                loginBinding.loginButton.isEnabled = true
                                if (it is LoginException) {
                                    Log.e(TAG, it.state.toString())
                                    when (it.state) {
                                        LoginExceptionState.NO_CONNECTION -> {
                                            mainVM.setToastMessage(
                                                if (mainVM.isNetworkConnected().value == true) {
                                                    getString(R.string.error_service_unavailable)
                                                } else {
                                                    getString(R.string.no_internet_connection)
                                                }
                                            )
                                        }
                                        LoginExceptionState.NO_COUNTRY -> {
                                            mainVM.setToastMessage(getString(R.string.no_location_data))
                                        }
                                        LoginExceptionState.INVALID_USER_OR_PASSWORD -> {
                                            loginBinding.usernameEditText.error =
                                                getString(R.string.wrong_password)
                                            loginBinding.passwordEditText.error =
                                                getString(R.string.wrong_password)
                                        }
                                    }
                                } else {
                                    Log.e(TAG, it)
                                    loginBinding.usernameEditText.error = null
                                    loginBinding.passwordEditText.error = null
                                    mainVM.setToastMessage(getString(R.string.no_internet_connection))
                                }
                            })
                }
            }
        }
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }

    private fun setEnvironment(env: ApiEnvironment) {
        vm.setApiHost(env)
        loginBinding.envTextView.text = env.title
    }

    companion object {
        private val TAG = LoginFragment::class.java.simpleName
    }
}

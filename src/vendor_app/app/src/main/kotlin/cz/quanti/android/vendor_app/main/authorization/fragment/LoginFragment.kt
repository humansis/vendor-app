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
import cz.quanti.android.vendor_app.utils.ApiEnvironments
import cz.quanti.android.vendor_app.utils.Constants
import cz.quanti.android.vendor_app.utils.SendLogDialogFragment
import cz.quanti.android.vendor_app.utils.hideKeyboard
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
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
            popup.menu.add(0, ApiEnvironments.FRONT.id, 0, "FRONT API")
            popup.menu.add(0, ApiEnvironments.DEMO.id, 0, "DEMO API")
            popup.menu.add(0, ApiEnvironments.STAGE.id, 0, "STAGE API")
            popup.menu.add(0, ApiEnvironments.DEV1.id, 0, "DEV1 API")
            popup.menu.add(0, ApiEnvironments.DEV2.id, 0, "DEV2 API")
            popup.menu.add(0, ApiEnvironments.DEV3.id, 0, "DEV3 API")
            popup.menu.add(0, ApiEnvironments.TEST.id, 0, "TEST API")
            popup.menu.add(0, ApiEnvironments.LOCAL.id, 0, "LOCAL API")
            popup.menu.add(0, ApiEnvironments.CUSTOM.id, 0, "CUSTOM API")

            popup.setOnMenuItemClickListener { item ->
                ApiEnvironments.values().find { it.id == item?.itemId }?.let { env ->
                    if (env == ApiEnvironments.CUSTOM) {
                        setCustomEnvironment(env)
                    } else {
                        setEnvironment(env)
                    }
                }
                true
            }
            popup.show()
        }

        val defaultEnv = if (BuildConfig.DEBUG) {
            loginBinding.settingsImageView.visibility = View.VISIBLE
            loginBinding.envTextView.visibility = View.VISIBLE

            vm.getApiHost() ?: ApiEnvironments.STAGE
        } else {
            loginBinding.settingsImageView.visibility = View.INVISIBLE
            loginBinding.envTextView.visibility = View.INVISIBLE
            loginBinding.versionTextView.setOnLongClickListener {
                loginBinding.settingsImageView.visibility = View.VISIBLE
                loginBinding.envTextView.visibility = View.VISIBLE
                return@setOnLongClickListener true
            }

            ApiEnvironments.FRONT
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
                vm.setApiHost(ApiEnvironments.STAGE)
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
                        vm.setApiHost(ApiEnvironments.STAGE)
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

    private fun setCustomEnvironment(env: ApiEnvironments) {
        setEnvironment(
            try {
                val fis = File(
                    requireContext().getExternalFilesDir(null)?.absolutePath,
                    "apiconfig.txt"
                ).inputStream()
                val bufferedReader = BufferedReader(InputStreamReader(fis, "UTF-8"))
                val line = bufferedReader.readLine()
                if (line.isNullOrBlank()) {
                    throw java.lang.Exception("Custom Api host could not be read.")
                } else {
                    env.apply {
                        customUrl = line
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, e)
                // does not require string resource, since it only occurs in debug builds.
                mainVM.setToastMessage("Custom Api host could not be read.")
                // fallback to default
                ApiEnvironments.STAGE
            }
        )
    }

    private fun setEnvironment(env: ApiEnvironments) {
        vm.setApiHost(env)
        loginBinding.envTextView.text = env.name
    }

    companion object {
        private val TAG = LoginFragment::class.java.simpleName
    }
}

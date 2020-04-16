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
import cz.quanti.android.vendor_app.App
import cz.quanti.android.vendor_app.BuildConfig
import cz.quanti.android.vendor_app.MainActivity
import cz.quanti.android.vendor_app.R
import cz.quanti.android.vendor_app.main.authorization.viewmodel.LoginViewModel
import cz.quanti.android.vendor_app.utils.ApiEnvironments
import cz.quanti.android.vendor_app.utils.ApiManager
import cz.quanti.android.vendor_app.utils.CurrentVendor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_login.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import quanti.com.kotlinlog.Log

class LoginFragment : Fragment() {

    private val vm: LoginViewModel by viewModel()
    private var disposable: Disposable? = null

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

        if (BuildConfig.DEBUG) {
            settingsImageView.visibility = View.VISIBLE
            envTextView.visibility = View.VISIBLE
            var defaultEnv = ApiEnvironments.DEV
            val savedEnv = CurrentVendor.url
            savedEnv?.let {
                defaultEnv = savedEnv
            }
            envTextView.text = defaultEnv.name
            initApi(defaultEnv.getUrl())

            settingsImageView.setOnClickListener {
                val contextThemeWrapper =
                    ContextThemeWrapper(requireContext(), R.style.PopupMenuTheme)
                val popup = PopupMenu(contextThemeWrapper, settingsImageView)
                popup.inflate(R.menu.api_urls_menu)
                popup.menu.add(0, ApiEnvironments.FRONT.id, 0, "FRONT API")
                popup.menu.add(0, ApiEnvironments.DEMO.id, 0, "DEMO API")
                popup.menu.add(0, ApiEnvironments.STAGE.id, 0, "STAGE API")
                popup.menu.add(0, ApiEnvironments.DEV.id, 0, "DEV API")
                popup.menu.add(0, ApiEnvironments.TEST.id, 0, "TEST API")
                popup.setOnMenuItemClickListener { item ->
                    val env = ApiEnvironments.values().find { it.id == item?.itemId }
                    env?.let {
                        initApi(it.getUrl())
                        envTextView.text = it.name
                        CurrentVendor.url = it
                    }
                    true
                }
                popup.show()
            }
        } else {
            settingsImageView.visibility = View.INVISIBLE
            envTextView.visibility = View.INVISIBLE
        }

        if (CurrentVendor.isLoggedIn()) {
            if (vm.isFirstTimeLoading()) {
                vm.setIsNotFirstTimeLoading()
                findNavController().navigate(
                    LoginFragmentDirections.actionLoginFragmentToVendorFragment()
                )
            } else {
                // hardware back button was probably pressed, exit the application
                requireActivity().finishAffinity()
            }
        } else {
            logoImageView.clipToOutline = true
            loginButton.isEnabled = true
            loginButton.setOnClickListener {
                loginButton.isEnabled = false
                loginButton.visibility = View.INVISIBLE
                loadingImageView.visibility = View.VISIBLE

                val animation = RotateAnimation(
                    0f,
                    360f,
                    loadingImageView.width / 2f,
                    loadingImageView.height / 2f
                )
                animation.duration = 2500
                animation.repeatCount = Animation.INFINITE
                loadingImageView.startAnimation(animation)

                disposable =
                    vm.login(usernameEditText.text.toString(), passwordEditText.text.toString())
                    .subscribeOn(
                        Schedulers.io()
                    ).observeOn(AndroidSchedulers.mainThread()).subscribe(
                        {
                            loadingImageView.animation.repeatCount = 0
                            findNavController().navigate(
                                LoginFragmentDirections.actionLoginFragmentToVendorFragment()
                            )
                        },
                        {
                            loadingImageView.clearAnimation()
                            loadingImageView.visibility = View.INVISIBLE
                            loginButton.visibility = View.VISIBLE
                            loginButton.isEnabled = true
                            Log.e(it)
                            if ((activity as MainActivity).isNetworkAvailable()) {
                                usernameEditText.error = getString(R.string.wrong_password)
                                passwordEditText.error = getString(R.string.wrong_password)
                            } else {
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

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    private fun initApi(url: String) {
        ApiManager.changeUrl(url)
    }
}

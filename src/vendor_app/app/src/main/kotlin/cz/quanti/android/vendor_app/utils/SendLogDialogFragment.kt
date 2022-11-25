package cz.quanti.android.vendor_app.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import cz.quanti.android.vendor_app.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import quanti.com.kotlinlog.Log
import quanti.com.kotlinlog.R
import quanti.com.kotlinlog.utils.copyLogsTOSDCard
import quanti.com.kotlinlog.utils.getFormattedFileNameDayNow
import quanti.com.kotlinlog.utils.getUriForFile
import quanti.com.kotlinlog.utils.getZipOfLogs
import quanti.com.kotlinlog.utils.hasFileWritePermission
import java.io.File


/**
 * Created by Trnka Vladislav on 20.06.2017.
 *
 * Dialog that shows user options to save or send logs
 */

class SendLogDialogFragment : DialogFragment() {

    private val mainVM: MainViewModel by sharedViewModel()

    // TODO temporary hotfix, delete after implementing fixed version of kotlinlogger

    companion object {
        const val MESSAGE = "send_message"
        const val TITLE = "send_title"
        const val EMAIL_BUTTON_TEXT = "email_button"
        const val FILE_BUTTON_TEXT = "file_button"
        const val SEND_EMAIL_ADDRESSES = "send_address"
        const val DIALOG_THEME = "dialog_theme"
        private val TAG = SendLogDialogFragment::class.java.simpleName

        @JvmOverloads
        @JvmStatic
        fun newInstance(
            sendEmailAddress: String,
            message: String = "Would you like to send logs by email or save them to SD card?",
            title: String = "Send logs",
            emailButtonText: String = "Email",
            fileButtonText: String = "Save",
            dialogTheme: Int? = null
        ) = newInstance(
            arrayOf(sendEmailAddress),
            message,
            title,
            emailButtonText,
            fileButtonText,
            dialogTheme
        )

        @JvmOverloads
        @JvmStatic
        fun newInstance(
            sendEmailAddress: Array<String>,
            message: String = "Would you like to send logs by email or save them to SD card?",
            title: String = "Send logs",
            emailButtonText: String = "Email",
            fileButtonText: String = "Save",
            dialogTheme: Int? = null
        ): SendLogDialogFragment {
            val myFragment = SendLogDialogFragment()

            val args = Bundle()
            args.putString(MESSAGE, message)
            args.putString(TITLE, title)
            args.putString(EMAIL_BUTTON_TEXT, emailButtonText)
            args.putString(FILE_BUTTON_TEXT, fileButtonText)
            args.putStringArray(SEND_EMAIL_ADDRESSES, sendEmailAddress)
            if (dialogTheme != null) {
                args.putInt(DIALOG_THEME, dialogTheme)
            }

            myFragment.arguments = args

            return myFragment
        }
    }

    private var zipFile: Deferred<File>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        zipFile = CoroutineScope(Dispatchers.IO).async {
            getZipOfLogs(requireActivity().applicationContext, 48)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val hasFilePermission = requireActivity().applicationContext.hasFileWritePermission()

        return AlertDialog
            .Builder(requireContext(), requireArguments().getInt(DIALOG_THEME))
            .apply {
                setMessage(requireArguments().getString(MESSAGE))
                setTitle(requireArguments().getString(TITLE))
                setPositiveButton(
                    requireArguments().getString(EMAIL_BUTTON_TEXT),
                    this@SendLogDialogFragment::positiveButtonClick
                )

                if (hasFilePermission) {
                    setNeutralButton(
                        requireArguments().getString(FILE_BUTTON_TEXT),
                        this@SendLogDialogFragment::neutralButtonClick
                    )
                }
            }.create()
    }

    /**
     * On positive button click
     * Create zip of all logs and open email client to send
     */
    @Suppress("UNUSED_PARAMETER")
    private fun positiveButtonClick(dialog: DialogInterface, which: Int) =
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "Share logs clicked.")

            val appContext = this@SendLogDialogFragment.requireContext().applicationContext

            val addresses = requireArguments().getStringArray(SEND_EMAIL_ADDRESSES)
            val subject =
                getString(R.string.logs_email_subject) + " " + getFormattedFileNameDayNow()
            val bodyText = getString(R.string.logs_email_text)

            // await non block's current thread
            val zipFileUri = zipFile?.await()?.getUriForFile(appContext)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822" // email
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                putExtra(Intent.EXTRA_EMAIL, addresses)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, bodyText)
                putExtra(Intent.EXTRA_STREAM, zipFileUri)
            }

            val resInfoList: List<ResolveInfo> = requireContext().packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                requireContext().grantUriPermission(
                    packageName,
                    zipFileUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            try {
                startActivity(Intent.createChooser(intent, "Send mail..."))
            } catch (ex: android.content.ActivityNotFoundException) {
                mainVM.setToastMessage(getString(R.string.logs_email_no_client_installed))
            }
        }

    /**
     * On neutral button click
     * Copy ZIP of all logs to sd card
     */
    @Suppress("UNUSED_PARAMETER")
    private fun neutralButtonClick(dialog: DialogInterface, which: Int) =
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "Save file button clicked.")

            val file = zipFile!!.await().copyLogsTOSDCard(requireContext())

            mainVM.setToastMessage("File successfully copied" + "\n" + file.absolutePath)
        }
}

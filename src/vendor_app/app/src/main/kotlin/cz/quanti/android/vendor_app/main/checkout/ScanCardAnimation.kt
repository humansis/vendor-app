package cz.quanti.android.vendor_app.main.checkout

import android.view.View
import cz.quanti.android.vendor_app.databinding.LayoutScanCardAnimationBinding

class ScanCardAnimation(
    private val scanCardBinding: LayoutScanCardAnimationBinding
) {

    fun startScanCardAnimation(afterError: Boolean) {
        scanCardBinding.root.visibility = View.VISIBLE

        scanCardBinding.card.visibility = View.VISIBLE
        scanCardBinding.phone.visibility = View.VISIBLE
        scanCardBinding.cross.visibility = View.INVISIBLE
        scanCardBinding.spinner.visibility = View.INVISIBLE
        scanCardBinding.check.visibility = View.INVISIBLE
            // TODO doplnit animovani
    }

    fun stopScanCardAnimation() {
        scanCardBinding.root.visibility = View.GONE
    }

}

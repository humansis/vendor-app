package cz.quanti.android.vendor_app.main.checkout

import android.animation.ObjectAnimator
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnPause
import androidx.core.animation.doOnRepeat
import androidx.core.animation.doOnResume
import androidx.core.animation.doOnStart
import cz.quanti.android.vendor_app.databinding.LayoutScanCardAnimationBinding

class ScanCardAnimation(
    private val scanCardBinding: LayoutScanCardAnimationBinding
) {
    private var regularAnimation = ObjectAnimator.ofFloat(scanCardBinding.card, "translationX", -180f)
        .apply {
            var cardAttached = true

            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            doOnStart {
                cardAttached = true
            }
            doOnPause {
                if (cardAttached) {
                    scanCardBinding.spinner.visibility = View.VISIBLE
                }
            }
            doOnResume {
                if (cardAttached) {
                    scanCardBinding.spinner.visibility = View.INVISIBLE
                    scanCardBinding.check.visibility = View.VISIBLE
                } else {
                    scanCardBinding.check.visibility = View.INVISIBLE
                }
            }
            doOnRepeat {
                pause()
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        resume()
                        cardAttached = !cardAttached
                    },
                    if (cardAttached) 1500 else 500
                )
            }
            doOnEnd {
                scanCardBinding.card.clearAnimation()
                scanCardBinding.card.animate().translationX(0F).translationY(0F).setDuration(0).start()
            }
        }

    private var preserveBalanceAnimation = ObjectAnimator.ofFloat(scanCardBinding.card, "translationX", -180f)
        .apply {
            var cardAttached = true

            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            doOnStart {
                cardAttached = true
                scanCardBinding.cross.visibility = View.VISIBLE
            }
            doOnPause {
                if (cardAttached) {
                    scanCardBinding.cross.visibility = View.INVISIBLE
                    scanCardBinding.spinner.visibility = View.VISIBLE
                }
            }
            doOnResume {
                if (cardAttached) {
                    scanCardBinding.spinner.visibility = View.INVISIBLE
                    scanCardBinding.check.visibility = View.VISIBLE
                } else {
                    scanCardBinding.check.visibility = View.INVISIBLE
                    scanCardBinding.cross.visibility = View.VISIBLE
                }
            }
            doOnRepeat {
                pause()
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        resume()
                        cardAttached = !cardAttached
                    },
                    if (cardAttached) 1500 else 500
                )
            }
            doOnEnd {
                scanCardBinding.card.clearAnimation()
                scanCardBinding.card.animate().translationX(0F).translationY(0F).setDuration(0).start()
            }
        }

    fun startScanCardAnimation(preserveBalance: Boolean) {
        scanCardBinding.root.visibility = View.VISIBLE

        scanCardBinding.cross.visibility = View.INVISIBLE
        scanCardBinding.spinner.visibility = View.INVISIBLE
        scanCardBinding.check.visibility = View.INVISIBLE

        if (preserveBalance) {
            preserveBalanceAnimation.start()
        } else {
            regularAnimation.start()
        }
    }

    fun stopScanCardAnimation() {
        regularAnimation.end()
        preserveBalanceAnimation.end()
        scanCardBinding.root.visibility = View.GONE
    }
}

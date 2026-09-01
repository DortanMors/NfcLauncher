package `in`.vfom.nfclauncher.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.nfc.cardemulation.CardEmulation
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import `in`.vfom.nfclauncher.R

fun startNfcSettings(context: Context) {
    try {
        when {
            paymentAppIntents()
                .any { startIfAvailable(intent = it, context = context) } -> Unit
            startIfAvailable(
                intent = Intent(Settings.ACTION_NFC_SETTINGS),
                context = context,
            ) -> showToast(
                messageRes = R.string.nfc_payment_settings_unavailable,
                context = context,
            )

            else -> showToast(
                messageRes = R.string.nfc_settings_unavailable,
                context = context,
            )
        }
    } finally {
        (context as? Activity)?.finish()
        @Suppress("DEPRECATION")
        (context as? Activity)?.overridePendingTransition(0, 0)
    }
}

fun startIfAvailable(intent: Intent, context: Context): Boolean =
    try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }

fun showToast(messageRes: Int, context: Context) {
    Toast.makeText(context, messageRes, Toast.LENGTH_LONG).show()
}

/** Экраны выбора платёжного приложения для текущей версии Android, в порядке предпочтения. */
private fun paymentAppIntents(): List<Intent> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        listOf(
            Intent(CardEmulation.ACTION_CHANGE_DEFAULT)
                .putExtra(CardEmulation.EXTRA_CATEGORY, CardEmulation.CATEGORY_PAYMENT),
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
        )
    } else {
        listOf(Intent(Settings.ACTION_NFC_PAYMENT_SETTINGS))
    }

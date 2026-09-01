package `in`.vfom.nfclauncher

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.nfc.cardemulation.CardEmulation
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast

/**
 * Прозрачный "ярлык": сразу открывает системный экран выбора NFC-платёжного приложения
 * и закрывается сам, минуя все вложенные меню настроек.
 *
 * Экран переехал между версиями Android, поэтому целевой интент выбирается по версии ОС:
 *
 * - Android <= 14 - [Settings.ACTION_NFC_PAYMENT_SETTINGS], экран
 *   "Бесконтактные платежи -> Оплата по умолчанию".
 * - Android 15+ - выбор платёжного приложения стал ролью Wallet
 *   ("Приложения по умолчанию -> Кошелёк"), её экран живёт в PermissionController.
 *   Напрямую (`android.intent.action.MANAGE_DEFAULT_APP`) он недоступен обычному
 *   приложению - требует системного разрешения MANAGE_ROLE_HOLDERS. Публичный вход туда -
 *   [CardEmulation.ACTION_CHANGE_DEFAULT] без [CardEmulation.EXTRA_SERVICE_COMPONENT]:
 *   системный трамплин ChangeDefaultCardEmulationActivity сам открывает экран роли.
 *
 * Проверять доступность старого интента резолвом бесполезно: на Android 17 (Pixel) alias
 * `Settings$PaymentSettingsActivity` в прошивке всё ещё объявлен, но фрагмент за ним удалён,
 * поэтому интент резолвится, [ActivityNotFoundException] не бросается, а системные настройки
 * падают с ClassNotFoundException. Единственный надёжный признак - версия ОС.
 */
class NfcPaySettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            when {
                paymentAppIntents().any(::startIfAvailable) -> Unit
                startIfAvailable(Intent(Settings.ACTION_NFC_SETTINGS)) ->
                    showToast(R.string.nfc_payment_settings_unavailable)
                else -> showToast(R.string.nfc_settings_unavailable)
            }
        } finally {
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
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

    private fun startIfAvailable(intent: Intent): Boolean =
        try {
            startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }

    private fun showToast(messageRes: Int) {
        Toast.makeText(this, messageRes, Toast.LENGTH_LONG).show()
    }
}

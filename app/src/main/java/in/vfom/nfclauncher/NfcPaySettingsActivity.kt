package `in`.vfom.nfclauncher

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.nfc.cardemulation.CardEmulation
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import `in`.vfom.nfclauncher.utils.startNfcSettings

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

        startNfcSettings(this@NfcPaySettingsActivity)
    }
}

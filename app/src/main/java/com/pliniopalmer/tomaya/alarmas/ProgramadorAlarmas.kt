package com.pliniopalmer.tomaya.alarmas

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.pliniopalmer.tomaya.data.Toma
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

const val EXTRA_TOMA_ID = "tomaId"

@Singleton
class ProgramadorAlarmas @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun programar(toma: Toma) {
        val pi = pendingIntent(toma.id)
        val exactasPermitidas =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        try {
            if (exactasPermitidas) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, toma.programadaMillis, pi
                )
            } else {
                // Sin el permiso de alarmas exactas, mejor una alarma aproximada que ninguna.
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, toma.programadaMillis, pi
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, toma.programadaMillis, pi)
        } catch (_: IllegalStateException) {
            // Límite de alarmas simultáneas del sistema; sincronizarAlarmas() lo evita.
        }
    }

    fun cancelar(toma: Toma) = cancelarPorId(toma.id)

    fun cancelarPorId(tomaId: Long) {
        alarmManager.cancel(pendingIntent(tomaId))
    }

    private fun pendingIntent(tomaId: Long): PendingIntent {
        val intent = Intent(context, AlarmaReceiver::class.java).putExtra(EXTRA_TOMA_ID, tomaId)
        return PendingIntent.getBroadcast(
            context,
            tomaId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}

package com.pliniopalmer.tomaya.alarmas

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pliniopalmer.tomaya.MainActivity
import com.pliniopalmer.tomaya.R
import com.pliniopalmer.tomaya.TomaYaApp
import com.pliniopalmer.tomaya.data.TomaYaRepository
import com.pliniopalmer.tomaya.ui.formatoDosis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Ejecuta trabajo suspendido en un receiver sin que el sistema lo mate antes de terminar. */
private fun BroadcastReceiver.enSegundoPlano(bloque: suspend () -> Unit) {
    val pendiente = goAsync()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            bloque()
        } finally {
            pendiente.finish()
        }
    }
}

/** Suena cuando llega la hora de una toma: muestra la notificación con la acción "Tomado". */
@AndroidEntryPoint
class AlarmaReceiver : BroadcastReceiver() {

    @Inject lateinit var repositorio: TomaYaRepository

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val tomaId = intent.getLongExtra(EXTRA_TOMA_ID, -1L)
        if (tomaId == -1L) return

        enSegundoPlano {
            // Al sonar una alarma queda un hueco libre para la siguiente toma en cola.
            repositorio.sincronizarAlarmas()
            val info = repositorio.infoToma(tomaId) ?: return@enSegundoPlano

            val abrirApp = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val tomado = PendingIntent.getBroadcast(
                context,
                tomaId.toInt(),
                Intent(context, TomadoReceiver::class.java).putExtra(EXTRA_TOMA_ID, tomaId),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            val notificacion = NotificationCompat.Builder(context, TomaYaApp.CANAL_TOMAS)
                .setSmallIcon(R.drawable.ic_notificacion)
                .setContentTitle("¡TomaYa! ${info.nombre}")
                .setContentText(formatoDosis(info.cantidad, info.unidad))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(abrirApp)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_notificacion, "Tomado", tomado)
                .build()

            try {
                NotificationManagerCompat.from(context).notify(tomaId.toInt(), notificacion)
            } catch (_: SecurityException) {
                // El usuario no concedió el permiso de notificaciones.
            }
        }
    }
}

/** Acción "Tomado" de la notificación. */
@AndroidEntryPoint
class TomadoReceiver : BroadcastReceiver() {

    @Inject lateinit var repositorio: TomaYaRepository

    override fun onReceive(context: Context, intent: Intent) {
        val tomaId = intent.getLongExtra(EXTRA_TOMA_ID, -1L)
        if (tomaId == -1L) return

        enSegundoPlano {
            repositorio.marcarTomada(tomaId)
            NotificationManagerCompat.from(context).cancel(tomaId.toInt())
        }
    }
}

/** Las alarmas no sobreviven a un reinicio del teléfono: se reprograman al arrancar. */
@AndroidEntryPoint
class ReinicioReceiver : BroadcastReceiver() {

    @Inject lateinit var repositorio: TomaYaRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        enSegundoPlano { repositorio.sincronizarAlarmas() }
    }
}

package com.pliniopalmer.tomaya

import android.Manifest
import android.app.AlarmManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.pliniopalmer.tomaya.ui.TomaYaNavegacion
import com.pliniopalmer.tomaya.ui.theme.TomaYaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val pedirNotificaciones =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            comprobarAlarmasExactas()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TomaYaTheme {
                TomaYaNavegacion()
            }
        }
        if (savedInstanceState == null) pedirPermisos()
    }

    private fun pedirPermisos() {
        val faltaNotificaciones = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (faltaNotificaciones) {
            pedirNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            comprobarAlarmasExactas()
        }
    }

    private fun comprobarAlarmasExactas() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        if (getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) return
        try {
            startActivity(
                Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:$packageName"),
                )
            )
        } catch (_: ActivityNotFoundException) {
            // Algunos dispositivos no tienen esta pantalla de ajustes.
        }
    }
}

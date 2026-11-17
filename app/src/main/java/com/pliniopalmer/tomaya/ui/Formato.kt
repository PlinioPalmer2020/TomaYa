package com.pliniopalmer.tomaya.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 5.0 → "5", 2.5 → "2.5". */
fun formatoCantidad(cantidad: Double): String =
    if (cantidad % 1.0 == 0.0) cantidad.toLong().toString() else cantidad.toString()

private fun formatear(patron: String, millis: Long): String =
    SimpleDateFormat(patron, Locale.getDefault()).format(Date(millis))

fun formatoFechaHora(millis: Long): String = formatear("dd/MM/yyyy hh:mm a", millis)
fun formatoFecha(millis: Long): String = formatear("dd/MM/yyyy", millis)
fun formatoToma(millis: Long): String = formatear("EEE dd/MM hh:mm a", millis)
fun formatoHora(millis: Long): String = formatear("hh:mm a", millis)

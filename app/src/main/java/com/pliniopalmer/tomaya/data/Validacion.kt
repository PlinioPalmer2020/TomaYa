package com.pliniopalmer.tomaya.data

const val MAX_DIAS = 365
const val MAX_LARGO_NOMBRE = 60
const val MAX_LARGO_UNIDAD = 20
const val MAX_CANTIDAD = 100_000.0

/**
 * total = (dias × 24) ÷ intervaloHoras, redondeado hacia arriba para contar la última
 * toma cuando el intervalo no divide exacto (cada 5 h por 1 día: 0, 5, 10, 15 y 20 h).
 * Devuelve 0 si los datos no son válidos.
 */
fun totalTomas(intervaloHoras: Int, dias: Int): Int =
    if (intervaloHoras <= 0 || dias <= 0) 0
    else ((dias.toLong() * 24L + intervaloHoras - 1) / intervaloHoras).toInt()

/** Acepta coma o punto decimal. Devuelve null si no es un número finito. */
fun parsearCantidad(texto: String): Double? =
    texto.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }

/** Mensaje de error de la cantidad, o null si es válida. */
fun errorCantidad(cantidad: Double?): String? = when {
    cantidad == null -> "Escribe un número"
    cantidad <= 0.0 -> "Debe ser mayor que 0"
    cantidad > MAX_CANTIDAD -> "Cantidad demasiado grande"
    else -> null
}

/** Mensaje de error de los días, o null si son válidos. */
fun errorDias(dias: Int?): String? = when {
    dias == null -> "Escribe un número"
    dias <= 0 -> "Debe ser mayor que 0"
    dias > MAX_DIAS -> "Máximo $MAX_DIAS días"
    else -> null
}

/**
 * Mensaje de error del intervalo, o null si es válido. Un intervalo mayor que la
 * duración del tratamiento no tiene sentido (no habría segunda toma), así que se rechaza.
 */
fun errorIntervalo(intervaloHoras: Int?, dias: Int?): String? = when {
    intervaloHoras == null -> "Escribe un número"
    intervaloHoras <= 0 -> "Debe ser mayor que 0"
    dias != null && errorDias(dias) == null && intervaloHoras > dias * 24 ->
        "No puede superar la duración (${dias * 24} h)"
    else -> null
}

/** Validación completa de un tratamiento; la usa el repositorio como última defensa. */
fun Tratamiento.esValido(): Boolean =
    nombre.isNotBlank() && nombre.length <= MAX_LARGO_NOMBRE &&
        unidad.isNotBlank() && unidad.length <= MAX_LARGO_UNIDAD &&
        errorCantidad(cantidad) == null &&
        errorDias(dias) == null &&
        errorIntervalo(intervaloHoras, dias) == null &&
        totalTomas(intervaloHoras, dias) >= 1

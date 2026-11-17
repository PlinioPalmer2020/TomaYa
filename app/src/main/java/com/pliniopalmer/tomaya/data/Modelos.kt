package com.pliniopalmer.tomaya.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tratamientos")
data class Tratamiento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val cantidad: Double,
    val unidad: String,
    val intervaloHoras: Int,
    val dias: Int,
    val inicioMillis: Long,
    val archivado: Boolean = false,
    val archivadoMillis: Long? = null,
)

@Entity(
    tableName = "tomas",
    foreignKeys = [
        ForeignKey(
            entity = Tratamiento::class,
            parentColumns = ["id"],
            childColumns = ["tratamientoId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("tratamientoId")],
)
data class Toma(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tratamientoId: Long,
    val programadaMillis: Long,
    /** null significa pendiente. */
    val tomadaMillis: Long? = null,
)

/** Datos mínimos para mostrar la notificación de una toma. */
data class InfoToma(
    val nombre: String,
    val cantidad: Double,
    val unidad: String,
)

enum class EstadoToma { TOMADA, PENDIENTE, PERDIDA }

fun Toma.estado(ahora: Long): EstadoToma = when {
    tomadaMillis != null -> EstadoToma.TOMADA
    programadaMillis > ahora -> EstadoToma.PENDIENTE
    else -> EstadoToma.PERDIDA
}

private const val HORA_MILLIS = 60L * 60L * 1000L

/**
 * Genera todas las tomas de un tratamiento: el total sale de [totalTomas] y la
 * toma i se programa en inicioMillis + i × intervalo.
 */
fun generarTomas(
    tratamientoId: Long,
    inicioMillis: Long,
    intervaloHoras: Int,
    dias: Int,
): List<Toma> {
    val total = totalTomas(intervaloHoras, dias)
    return List(total) { i ->
        Toma(
            tratamientoId = tratamientoId,
            programadaMillis = inicioMillis + i.toLong() * intervaloHoras * HORA_MILLIS,
        )
    }
}

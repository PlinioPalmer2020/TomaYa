package com.pliniopalmer.tomaya.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TomaYaDao {

    @Insert
    suspend fun insertarTratamiento(t: Tratamiento): Long

    @Insert
    suspend fun insertarTomas(tomas: List<Toma>): List<Long>

    @Update
    suspend fun actualizarTratamiento(t: Tratamiento)

    @Query("SELECT * FROM tratamientos WHERE archivado = 0 ORDER BY id DESC")
    fun tratamientosActivos(): Flow<List<Tratamiento>>

    @Query("SELECT * FROM tratamientos WHERE archivado = 1 ORDER BY archivadoMillis DESC")
    fun tratamientosArchivados(): Flow<List<Tratamiento>>

    @Query("SELECT * FROM tratamientos WHERE id = :id")
    fun tratamiento(id: Long): Flow<Tratamiento?>

    @Query("SELECT * FROM tratamientos WHERE id = :id")
    suspend fun tratamientoUnaVez(id: Long): Tratamiento?

    @Query("SELECT * FROM tomas WHERE tratamientoId = :id ORDER BY programadaMillis")
    fun tomasDe(id: Long): Flow<List<Toma>>

    @Query(
        "SELECT * FROM tomas WHERE tratamientoId = :id " +
            "AND tomadaMillis IS NULL AND programadaMillis > :ahora"
    )
    suspend fun pendientesDe(id: Long, ahora: Long): List<Toma>

    /** Todas las tomas futuras sin tomar de tratamientos no archivados. */
    @Query(
        "SELECT tomas.* FROM tomas " +
            "INNER JOIN tratamientos ON tratamientos.id = tomas.tratamientoId " +
            "WHERE tomas.tomadaMillis IS NULL AND tomas.programadaMillis > :ahora " +
            "AND tratamientos.archivado = 0 ORDER BY tomas.programadaMillis"
    )
    suspend fun tomasPendientes(ahora: Long): List<Toma>

    @Query("UPDATE tomas SET tomadaMillis = :cuando WHERE id = :tomaId")
    suspend fun marcarTomada(tomaId: Long, cuando: Long)

    @Query("UPDATE tratamientos SET archivado = 1, archivadoMillis = :cuando WHERE id = :id")
    suspend fun archivar(id: Long, cuando: Long)

    @Query("UPDATE tratamientos SET archivado = 0, archivadoMillis = NULL WHERE id = :id")
    suspend fun desarchivar(id: Long)

    @Query(
        "DELETE FROM tomas WHERE tratamientoId = :id " +
            "AND tomadaMillis IS NULL AND programadaMillis > :ahora"
    )
    suspend fun borrarPendientesFuturas(id: Long, ahora: Long)

    @Query(
        "SELECT tratamientos.nombre AS nombre, tratamientos.cantidad AS cantidad, " +
            "tratamientos.unidad AS unidad FROM tomas " +
            "INNER JOIN tratamientos ON tratamientos.id = tomas.tratamientoId " +
            "WHERE tomas.id = :tomaId"
    )
    suspend fun infoToma(tomaId: Long): InfoToma?
}

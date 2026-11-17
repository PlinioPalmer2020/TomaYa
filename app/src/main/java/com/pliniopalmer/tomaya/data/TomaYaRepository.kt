package com.pliniopalmer.tomaya.data

import androidx.room.withTransaction
import com.pliniopalmer.tomaya.alarmas.ProgramadorAlarmas
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android admite como máximo 500 alarmas simultáneas por app y lanza una excepción al
 * superarlas. Solo se mantienen programadas las tomas más próximas; el resto entra
 * a medida que van sonando las anteriores.
 */
const val MAX_ALARMAS_PROGRAMADAS = 300

@Singleton
class TomaYaRepository @Inject constructor(
    private val db: TomaYaDatabase,
    private val dao: TomaYaDao,
    private val alarmas: ProgramadorAlarmas,
) {
    val activos: Flow<List<Tratamiento>> = dao.tratamientosActivos()
    val archivados: Flow<List<Tratamiento>> = dao.tratamientosArchivados()

    fun tratamiento(id: Long): Flow<Tratamiento?> = dao.tratamiento(id)
    fun tomasDe(id: Long): Flow<List<Toma>> = dao.tomasDe(id)
    suspend fun tratamientoUnaVez(id: Long): Tratamiento? = dao.tratamientoUnaVez(id)
    suspend fun infoToma(tomaId: Long): InfoToma? = dao.infoToma(tomaId)

    /** Crea el tratamiento con todas sus tomas; solo las futuras llevan alarma. */
    suspend fun crear(nuevo: Tratamiento): Long {
        require(nuevo.esValido()) { "Tratamiento no válido" }
        val id = db.withTransaction {
            val id = dao.insertarTratamiento(nuevo.copy(id = 0))
            dao.insertarTomas(generarTomas(id, nuevo.inicioMillis, nuevo.intervaloHoras, nuevo.dias))
            id
        }
        sincronizarAlarmas()
        return id
    }

    /**
     * Si solo cambian nombre, cantidad o unidad, no se tocan las tomas. Si cambia el
     * horario, se reemplazan las tomas pendientes futuras y se conservan las pasadas.
     */
    suspend fun editar(original: Tratamiento, editado: Tratamiento) {
        require(editado.esValido()) { "Tratamiento no válido" }
        val cambiaHorario = original.intervaloHoras != editado.intervaloHoras ||
            original.dias != editado.dias ||
            original.inicioMillis != editado.inicioMillis

        if (!cambiaHorario) {
            dao.actualizarTratamiento(editado)
            return
        }

        val ahora = System.currentTimeMillis()
        dao.pendientesDe(editado.id, ahora).forEach(alarmas::cancelar)
        db.withTransaction {
            dao.actualizarTratamiento(editado)
            dao.borrarPendientesFuturas(editado.id, ahora)
            dao.insertarTomas(
                generarTomas(editado.id, editado.inicioMillis, editado.intervaloHoras, editado.dias)
                    .filter { it.programadaMillis > ahora }
            )
        }
        // Un tratamiento archivado no entra en la sincronización hasta que se desarchive.
        sincronizarAlarmas()
    }

    suspend fun archivar(id: Long) {
        val ahora = System.currentTimeMillis()
        dao.pendientesDe(id, ahora).forEach(alarmas::cancelar)
        dao.archivar(id, ahora)
        sincronizarAlarmas()
    }

    suspend fun desarchivar(id: Long) {
        dao.desarchivar(id)
        sincronizarAlarmas()
    }

    suspend fun marcarTomada(tomaId: Long) {
        dao.marcarTomada(tomaId, System.currentTimeMillis())
        alarmas.cancelarPorId(tomaId)
    }

    /**
     * Deja programadas las [MAX_ALARMAS_PROGRAMADAS] tomas pendientes más próximas de
     * los tratamientos activos y cancela las demás. Se llama tras cada cambio, cada vez
     * que suena una alarma y al reiniciar el teléfono (las alarmas no sobreviven).
     */
    suspend fun sincronizarAlarmas() {
        val pendientes = dao.tomasPendientes(System.currentTimeMillis())
        pendientes.drop(MAX_ALARMAS_PROGRAMADAS).forEach(alarmas::cancelar)
        pendientes.take(MAX_ALARMAS_PROGRAMADAS).forEach(alarmas::programar)
    }
}

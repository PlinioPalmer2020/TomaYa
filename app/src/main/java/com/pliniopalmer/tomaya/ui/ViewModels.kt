package com.pliniopalmer.tomaya.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pliniopalmer.tomaya.data.Toma
import com.pliniopalmer.tomaya.data.TomaYaRepository
import com.pliniopalmer.tomaya.data.Tratamiento
import com.pliniopalmer.tomaya.data.errorCantidad
import com.pliniopalmer.tomaya.data.errorDias
import com.pliniopalmer.tomaya.data.errorIntervalo
import com.pliniopalmer.tomaya.data.normalizarUnidad
import com.pliniopalmer.tomaya.data.parsearCantidad
import com.pliniopalmer.tomaya.data.totalTomas
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TomaYaViewModel @Inject constructor(
    private val repositorio: TomaYaRepository,
) : ViewModel() {

    val activos: StateFlow<List<Tratamiento>> = repositorio.activos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivados: StateFlow<List<Tratamiento>> = repositorio.archivados
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun archivar(id: Long) = viewModelScope.launch { repositorio.archivar(id) }
    fun desarchivar(id: Long) = viewModelScope.launch { repositorio.desarchivar(id) }
}

@HiltViewModel
class FormularioViewModel @Inject constructor(
    private val repositorio: TomaYaRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /** null al crear; el id del tratamiento al editar. */
    val idEdicion: Long? = savedStateHandle.get<Long>("id")

    var original: Tratamiento? by mutableStateOf(null)
        private set

    var nombre by mutableStateOf("")
    var cantidad by mutableStateOf("")
    var unidad by mutableStateOf("")
    var intervaloHoras by mutableStateOf("")
    var dias by mutableStateOf("")
    var inicioMillis by mutableLongStateOf(ahoraAlMinuto())

    private var guardando by mutableStateOf(false)

    init {
        if (idEdicion != null) {
            viewModelScope.launch {
                repositorio.tratamientoUnaVez(idEdicion)?.let { t ->
                    original = t
                    nombre = t.nombre
                    cantidad = formatoCantidad(t.cantidad)
                    unidad = normalizarUnidad(t.unidad)
                    intervaloHoras = t.intervaloHoras.toString()
                    dias = t.dias.toString()
                    inicioMillis = t.inicioMillis
                }
            }
        }
    }

    private val cantidadNumero get() = parsearCantidad(cantidad)
    private val intervaloNumero get() = intervaloHoras.trim().toIntOrNull()
    private val diasNumero get() = dias.trim().toIntOrNull()

    // Un campo vacío todavía no muestra error, pero tampoco deja guardar.
    val errorDeCantidad: String?
        get() = if (cantidad.isBlank()) null else errorCantidad(cantidadNumero)
    val errorDeIntervalo: String?
        get() = if (intervaloHoras.isBlank()) null else errorIntervalo(intervaloNumero, diasNumero)
    val errorDeDias: String?
        get() = if (dias.isBlank()) null else errorDias(diasNumero)

    /** Cuántas tomas generará el tratamiento, o null si el horario aún no es válido. */
    val tomasPrevistas: Int?
        get() {
            val intervalo = intervaloNumero ?: return null
            val d = diasNumero ?: return null
            if (errorDias(d) != null || errorIntervalo(intervalo, d) != null) return null
            return totalTomas(intervalo, d)
        }

    val valido: Boolean
        get() = !guardando &&
            (idEdicion == null || original != null) &&
            nombre.isNotBlank() &&
            unidad.isNotBlank() &&
            errorCantidad(cantidadNumero) == null &&
            (tomasPrevistas ?: 0) >= 1

    fun guardar(alTerminar: () -> Unit) {
        if (!valido) return
        val base = original
        val tratamiento = Tratamiento(
            id = base?.id ?: 0,
            nombre = nombre.trim(),
            cantidad = cantidadNumero!!,
            unidad = unidad.trim(),
            intervaloHoras = intervaloNumero!!,
            dias = diasNumero!!,
            inicioMillis = inicioMillis,
            archivado = base?.archivado ?: false,
            archivadoMillis = base?.archivadoMillis,
        )
        guardando = true
        viewModelScope.launch {
            if (base == null) repositorio.crear(tratamiento) else repositorio.editar(base, tratamiento)
            alTerminar()
        }
    }

    private fun ahoraAlMinuto(): Long = System.currentTimeMillis() / 60_000L * 60_000L
}

@HiltViewModel
class HistorialViewModel @Inject constructor(
    private val repositorio: TomaYaRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val id: Long = checkNotNull(savedStateHandle.get<Long>("id"))

    val tratamiento: StateFlow<Tratamiento?> = repositorio.tratamiento(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val tomas: StateFlow<List<Toma>> = repositorio.tomasDe(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun marcar(tomaId: Long) = viewModelScope.launch { repositorio.marcarTomada(tomaId) }
}

package com.pliniopalmer.tomaya.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pliniopalmer.tomaya.data.EstadoToma
import com.pliniopalmer.tomaya.data.estado
import kotlinx.coroutines.delay

private val VerdeTomada = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel,
    alVolver: () -> Unit,
    alEditar: (Long) -> Unit,
) {
    val tratamiento by viewModel.tratamiento.collectAsStateWithLifecycle()
    val tomas by viewModel.tomas.collectAsStateWithLifecycle()

    // El estado depende de la hora actual: se refresca para que una toma pase sola
    // de "Pendiente" a "No marcada" con la pantalla abierta.
    var ahora by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            ahora = System.currentTimeMillis()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tratamiento?.nombre.orEmpty()) },
                navigationIcon = { BotonAtras(alVolver) },
                actions = {
                    IconButton(onClick = { alEditar(viewModel.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            val tomadas = tomas.count { it.tomadaMillis != null }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "$tomadas de ${tomas.size} tomas",
                    style = MaterialTheme.typography.titleMedium,
                )
                LinearProgressIndicator(
                    progress = { if (tomas.isEmpty()) 0f else tomadas.toFloat() / tomas.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }
            HorizontalDivider()

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tomas, key = { it.id }) { toma ->
                    val estado = toma.estado(ahora)
                    ListItem(
                        leadingContent = {
                            when (estado) {
                                EstadoToma.TOMADA -> Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = VerdeTomada,
                                )
                                EstadoToma.PENDIENTE -> Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                )
                                EstadoToma.PERDIDA -> Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        },
                        headlineContent = { Text(formatoToma(toma.programadaMillis)) },
                        supportingContent = {
                            when (estado) {
                                EstadoToma.TOMADA -> Text(
                                    "Tomada a las ${formatoHora(toma.tomadaMillis ?: 0L)}",
                                    color = VerdeTomada,
                                )
                                EstadoToma.PENDIENTE -> Text("Pendiente")
                                EstadoToma.PERDIDA -> Text(
                                    "No marcada",
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        },
                        trailingContent = {
                            if (estado != EstadoToma.TOMADA) {
                                TextButton(onClick = { viewModel.marcar(toma.id) }) {
                                    Text("Tomado")
                                }
                            }
                        },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

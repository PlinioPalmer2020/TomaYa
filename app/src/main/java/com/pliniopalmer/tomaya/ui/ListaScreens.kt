package com.pliniopalmer.tomaya.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pliniopalmer.tomaya.data.Tratamiento

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaScreen(
    viewModel: TomaYaViewModel,
    alAbrirHistorial: (Long) -> Unit,
    alCrear: () -> Unit,
    alAbrirArchivados: () -> Unit,
) {
    val activos by viewModel.activos.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TomaYa") },
                actions = {
                    IconButton(onClick = alAbrirArchivados) {
                        Icon(Icons.Default.Inventory2, contentDescription = "Archivados")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = alCrear) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo tratamiento")
            }
        },
    ) { padding ->
        if (activos.isEmpty()) {
            MensajeVacio("No tienes tratamientos activos", padding)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = padding,
            ) {
                items(activos, key = { it.id }) { t ->
                    ListItem(
                        modifier = Modifier.clickable { alAbrirHistorial(t.id) },
                        headlineContent = { Text(t.nombre) },
                        supportingContent = { Text(resumen(t)) },
                        trailingContent = {
                            IconButton(onClick = { viewModel.archivar(t.id) }) {
                                Icon(Icons.Default.Archive, contentDescription = "Archivar")
                            }
                        },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivadosScreen(
    viewModel: TomaYaViewModel,
    alAbrirHistorial: (Long) -> Unit,
    alVolver: () -> Unit,
) {
    val archivados by viewModel.archivados.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Archivados") },
                navigationIcon = { BotonAtras(alVolver) },
            )
        },
    ) { padding ->
        if (archivados.isEmpty()) {
            MensajeVacio("No hay tratamientos archivados", padding)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = padding,
            ) {
                items(archivados, key = { it.id }) { t ->
                    ListItem(
                        modifier = Modifier.clickable { alAbrirHistorial(t.id) },
                        headlineContent = { Text(t.nombre) },
                        supportingContent = {
                            Text("Archivado el ${formatoFecha(t.archivadoMillis ?: 0L)}")
                        },
                        trailingContent = {
                            IconButton(onClick = { viewModel.desarchivar(t.id) }) {
                                Icon(Icons.Default.Unarchive, contentDescription = "Desarchivar")
                            }
                        },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun BotonAtras(alVolver: () -> Unit) {
    IconButton(onClick = alVolver) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
    }
}

@Composable
private fun MensajeVacio(texto: String, padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private fun resumen(t: Tratamiento): String =
    "${formatoDosis(t.cantidad, t.unidad)} cada ${t.intervaloHoras} h por ${t.dias} días"

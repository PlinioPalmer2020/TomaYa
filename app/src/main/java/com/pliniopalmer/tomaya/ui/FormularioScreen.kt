package com.pliniopalmer.tomaya.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pliniopalmer.tomaya.data.MAX_LARGO_NOMBRE
import com.pliniopalmer.tomaya.data.MAX_LARGO_UNIDAD
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    viewModel: FormularioViewModel,
    alVolver: () -> Unit,
) {
    val context = LocalContext.current

    // Primero la fecha y, al confirmarla, la hora.
    fun elegirInicio() {
        val cal = Calendar.getInstance().apply { timeInMillis = viewModel.inicioMillis }
        DatePickerDialog(
            context,
            { _, anio, mes, dia ->
                TimePickerDialog(
                    context,
                    { _, hora, minuto ->
                        cal.set(anio, mes, dia, hora, minuto, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        viewModel.inicioMillis = cal.timeInMillis
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    false,
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (viewModel.idEdicion == null) "Nuevo tratamiento" else "Editar tratamiento"
                    )
                },
                navigationIcon = { BotonAtras(alVolver) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = viewModel.nombre,
                onValueChange = { viewModel.nombre = it.take(MAX_LARGO_NOMBRE) },
                label = { Text("Medicamento") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = viewModel.cantidad,
                    onValueChange = { viewModel.cantidad = filtrarDecimal(it) },
                    isError = viewModel.errorDeCantidad != null,
                    supportingText = { viewModel.errorDeCantidad?.let { Text(it) } },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = viewModel.unidad,
                    onValueChange = { viewModel.unidad = it.take(MAX_LARGO_UNIDAD) },
                    label = { Text("Unidad") },
                    placeholder = { Text("cc, pastilla, gotas") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = viewModel.intervaloHoras,
                    onValueChange = { viewModel.intervaloHoras = it.filter(Char::isDigit).take(4) },
                    isError = viewModel.errorDeIntervalo != null,
                    supportingText = { viewModel.errorDeIntervalo?.let { Text(it) } },
                    label = { Text("Cada (horas)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = viewModel.dias,
                    onValueChange = { viewModel.dias = it.filter(Char::isDigit).take(3) },
                    isError = viewModel.errorDeDias != null,
                    supportingText = { viewModel.errorDeDias?.let { Text(it) } },
                    label = { Text("Días") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }

            OutlinedButton(
                onClick = ::elegirInicio,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null)
                Text(
                    text = "Inicio: ${formatoFechaHora(viewModel.inicioMillis)}",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            viewModel.tomasPrevistas?.let { total ->
                Text(
                    text = if (total == 1) "Se programará 1 toma" else "Se programarán $total tomas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Button(
                onClick = { viewModel.guardar(alVolver) },
                enabled = viewModel.valido,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Guardar")
            }
        }
    }
}

/** Deja solo dígitos y un único separador decimal (coma o punto). */
private fun filtrarDecimal(texto: String): String {
    var hayPunto = false
    return texto.filter { c ->
        when {
            c.isDigit() -> true
            (c == '.' || c == ',') && !hayPunto -> { hayPunto = true; true }
            else -> false
        }
    }.take(9)
}

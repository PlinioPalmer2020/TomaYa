package com.pliniopalmer.tomaya.ui

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun TomaYaNavegacion() {
    val nav = rememberNavController()
    val argumentoId = listOf(navArgument("id") { type = NavType.LongType })

    NavHost(navController = nav, startDestination = "lista") {
        composable("lista") {
            ListaScreen(
                viewModel = hiltViewModel(),
                alAbrirHistorial = { id -> nav.navigate("historial/$id") },
                alCrear = { nav.navigate("nuevo") },
                alAbrirArchivados = { nav.navigate("archivados") },
            )
        }
        composable("nuevo") {
            FormularioScreen(viewModel = hiltViewModel(), alVolver = { nav.popBackStack() })
        }
        composable("editar/{id}", arguments = argumentoId) {
            FormularioScreen(viewModel = hiltViewModel(), alVolver = { nav.popBackStack() })
        }
        composable("historial/{id}", arguments = argumentoId) {
            HistorialScreen(
                viewModel = hiltViewModel(),
                alVolver = { nav.popBackStack() },
                alEditar = { id -> nav.navigate("editar/$id") },
            )
        }
        composable("archivados") {
            ArchivadosScreen(
                viewModel = hiltViewModel(),
                alAbrirHistorial = { id -> nav.navigate("historial/$id") },
                alVolver = { nav.popBackStack() },
            )
        }
    }
}

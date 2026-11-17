package com.pliniopalmer.tomaya

import com.pliniopalmer.tomaya.data.EstadoToma
import com.pliniopalmer.tomaya.data.Toma
import com.pliniopalmer.tomaya.data.estado
import com.pliniopalmer.tomaya.data.generarTomas
import com.pliniopalmer.tomaya.data.Tratamiento
import com.pliniopalmer.tomaya.data.errorCantidad
import com.pliniopalmer.tomaya.data.errorDias
import com.pliniopalmer.tomaya.data.errorIntervalo
import com.pliniopalmer.tomaya.data.esValido
import com.pliniopalmer.tomaya.data.parsearCantidad
import com.pliniopalmer.tomaya.data.totalTomas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculoTomasTest {

    private val hora = 3_600_000L

    @Test
    fun `cada 8 horas por 7 dias da 21 tomas`() {
        assertEquals(21, generarTomas(1, 0, intervaloHoras = 8, dias = 7).size)
    }

    @Test
    fun `cada 12 horas por 5 dias da 10 tomas`() {
        assertEquals(10, generarTomas(1, 0, intervaloHoras = 12, dias = 5).size)
    }

    @Test
    fun `la toma i se programa en inicio mas i por intervalo`() {
        val inicio = 1_000_000L
        val tomas = generarTomas(7, inicio, intervaloHoras = 8, dias = 1)
        assertEquals(listOf(inicio, inicio + 8 * hora, inicio + 16 * hora), tomas.map { it.programadaMillis })
        assertEquals(setOf(7L), tomas.map { it.tratamientoId }.toSet())
    }

    @Test
    fun `no desborda con tratamientos largos`() {
        val tomas = generarTomas(1, 0, intervaloHoras = 1, dias = 365)
        assertEquals(8759 * hora, tomas.last().programadaMillis)
    }

    @Test
    fun `un intervalo mayor que la duracion se rechaza`() {
        assertNotNull(errorIntervalo(48, 1))
        assertNull(errorIntervalo(24, 1))
        assertNull(errorIntervalo(48, 2))
        assertFalse(tratamiento(intervaloHoras = 48, dias = 1).esValido())
        assertTrue(tratamiento(intervaloHoras = 24, dias = 1).esValido())
    }

    @Test
    fun `ceros negativos y excesos se rechazan`() {
        assertNotNull(errorIntervalo(0, 7))
        assertNotNull(errorIntervalo(-8, 7))
        assertNotNull(errorIntervalo(null, 7))
        assertNotNull(errorDias(0))
        assertNotNull(errorDias(366))
        assertNull(errorDias(365))
        assertNotNull(errorCantidad(0.0))
        assertNotNull(errorCantidad(-1.0))
        assertNotNull(errorCantidad(null))
        assertNull(errorCantidad(0.5))
        assertFalse(tratamiento(nombre = "  ").esValido())
        assertFalse(tratamiento(unidad = "").esValido())
    }

    @Test
    fun `la cantidad acepta coma y rechaza valores no finitos`() {
        assertEquals(2.5, parsearCantidad("2,5")!!, 0.0)
        assertEquals(5.0, parsearCantidad(" 5 ")!!, 0.0)
        assertNull(parsearCantidad("Infinity"))
        assertNull(parsearCantidad("NaN"))
        assertNull(parsearCantidad("abc"))
        assertNull(parsearCantidad(""))
    }

    @Test
    fun `dias enormes no desbordan el total`() {
        assertEquals(Int.MAX_VALUE.toLong() * 24 / 1000 + 1, totalTomas(1000, Int.MAX_VALUE).toLong())
    }

    @Test
    fun `si el intervalo no divide exacto se cuenta la ultima toma`() {
        assertEquals(5, totalTomas(intervaloHoras = 5, dias = 1))
        assertEquals(4, totalTomas(intervaloHoras = 7, dias = 1))
        assertEquals(1, totalTomas(intervaloHoras = 24, dias = 1))
        assertEquals(0, totalTomas(intervaloHoras = 0, dias = 1))
        val tomas = generarTomas(1, 0, intervaloHoras = 5, dias = 1)
        assertEquals(20 * hora, tomas.last().programadaMillis)
    }

    private fun tratamiento(
        nombre: String = "Amoxicilina",
        unidad: String = "cc",
        intervaloHoras: Int = 8,
        dias: Int = 7,
    ) = Tratamiento(
        nombre = nombre, cantidad = 5.0, unidad = unidad,
        intervaloHoras = intervaloHoras, dias = dias, inicioMillis = 0,
    )

    @Test
    fun `estado de una toma`() {
        val ahora = 10_000L
        assertEquals(EstadoToma.TOMADA, Toma(1, 1, 5_000, tomadaMillis = 6_000).estado(ahora))
        assertEquals(EstadoToma.PENDIENTE, Toma(1, 1, 20_000).estado(ahora))
        assertEquals(EstadoToma.PERDIDA, Toma(1, 1, 5_000).estado(ahora))
    }
}
